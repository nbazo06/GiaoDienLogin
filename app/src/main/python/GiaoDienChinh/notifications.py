from flask import Blueprint, jsonify
from database import get_db_connection

notifications_bp = Blueprint('notifications', __name__, url_prefix='/api/notifications')

@notifications_bp.route('/<user_id>', methods=['GET'])
def get_notifications(user_id):
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        # Lấy danh sách thông báo theo UserID (có thể JOIN thêm nếu muốn mở rộng)
        cursor.execute('''
            SELECT NotificationID, Title, Message, Type, Is_read, Sent_at
            FROM Notification
            WHERE UserID = ?
            ORDER BY Sent_at DESC
        ''', (user_id,))
        
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
