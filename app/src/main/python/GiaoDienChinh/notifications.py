from flask import Blueprint, jsonify
from database import get_db_connection
from datetime import datetime, timedelta

notifications_bp = Blueprint('notifications', __name__, url_prefix='/api/notifications')

@notifications_bp.route('/<user_id>', methods=['GET'])
def get_notifications(user_id):
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        # ==== 1. XÓA THÔNG BÁO CŨ HƠN 30 NGÀY ====
        thirty_days_ago = (datetime.now() - timedelta(days=30)).strftime("%Y-%m-%d %H:%M:%S")
        cursor.execute('''
            DELETE FROM Notification
            WHERE Sent_at < ?
        ''', (thirty_days_ago,))
        conn.commit()

        # ==== 2. GỬI CẢNH BÁO NGÂN SÁCH NẾU VƯỢT 80% ====
        cursor.execute('''
            SELECT b.BudgetID, b.Budget_limit, c.Category_name,
                   IFNULL(SUM(t.Amount), 0) AS total_spent
            FROM Budget b
            JOIN Category c ON b.CategoryID = c.CategoryID AND b.UserID = c.UserID
            LEFT JOIN Transactions t ON t.CategoryID = b.CategoryID AND t.AccountID = b.AccountID
            WHERE b.AccountID = ?
              AND b.Start_date <= DATE('now')
              AND (b.End_date IS NULL OR b.End_date >= DATE('now'))
            GROUP BY b.BudgetID
        ''', (account_id,))
        
        budgets = cursor.fetchall()

        for budget in budgets:
            percent_spent = (budget["total_spent"] / budget["Budget_limit"]) * 100 if budget["Budget_limit"] > 0 else 0
            if percent_spent >= 80:
                # Kiểm tra đã có thông báo tương tự trong ngày
                cursor.execute('''
                    SELECT COUNT(*) FROM Notification
                    WHERE AccountID = ?
                      AND Message LIKE ?
                      AND DATE(Sent_at) = DATE('now')
                ''', (account_id, f'%{budget["Category_name"]}%'))
                
                already_exists = cursor.fetchone()[0]
                if already_exists == 0:
                    # Lấy UserID từ Account
                    cursor.execute('SELECT UserID FROM Account WHERE AccountID = ?', (account_id,))
                    user_row = cursor.fetchone()
                    if user_row:
                        user_id = user_row["UserID"]

                        # Gửi thông báo
                        message = f"Ngân sách cho {budget['Category_name']} đã vượt {percent_spent:.0f}%"
                        cursor.execute('''
                            INSERT INTO Notification (AccountID, Title, Message, Type, Is_read, Sent_at, UserID)
                            VALUES (?, ?, ?, ?, 0, CURRENT_TIMESTAMP, ?)
                        ''', (
                            account_id,
                            "Cảnh báo ngân sách",
                            message,
                            "Cảnh báo",
                            user_id
                        ))
                        conn.commit()

        # ==== 3. TRẢ VỀ DANH SÁCH THÔNG BÁO ====
        cursor.execute('''
            SELECT NotificationID, Title, Message, Type, Is_read, Sent_at
            FROM Notification
            WHERE UserID = ?
            ORDER BY Sent_at DESC
        ''', (user_id,))
        
        rows = cursor.fetchall()

        notifications = []
        for row in rows:
            notifications.append({
                "notification_id": row["NotificationID"],
                "title": row["Title"],
                "message": row["Message"],
                "type": row["Type"],
                "is_read": bool(row["Is_read"]),
                "sent_at": row["Sent_at"]
            })

        return jsonify({
            "status": "success",
            "user_id": user_id,
            "notifications": notifications
        })

    except Exception as e:
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500

    finally:
        conn.close()
