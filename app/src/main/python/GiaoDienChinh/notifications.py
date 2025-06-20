from flask import Blueprint, jsonify
from database import get_db_connection

notifications_bp = Blueprint('notifications', __name__, url_prefix='/api/notifications')

@notifications_bp.route('/<account_id>', methods=['GET'])
def get_notifications(account_id):
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        # Lấy danh sách thông báo theo AccountID (có thể JOIN thêm nếu muốn mở rộng)
        cursor.execute('''
            SELECT NotificationID, Title, Message, Type, Is_read, Sent_at
            FROM Notification
            WHERE AccountID = ?
            ORDER BY Sent_at DESC
        ''', (account_id,))
        
        rows = cursor.fetchall()

        # Chuyển sang dạng list[dict] để trả về JSON
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
            "account_id": account_id,
            "notifications": notifications
        })

    except Exception as e:
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500

    finally:
        conn.close()
