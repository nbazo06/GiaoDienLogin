from flask import Blueprint, request, jsonify
import re
import logging
import random
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from database import get_db_connection

forgot_password_bp = Blueprint('forgot-password', __name__, url_prefix='/api')

def send_otp_email(email, otp):
    """Gửi mã OTP qua email"""
    smtp_server = "smtp.gmail.com"
    smtp_port = 587
    sender_email = "admin@gmail.com"
    sender_password = "" // app key

    # Tạo message
    message = MIMEMultipart()
    message["From"] = sender_email
    message["To"] = email
    message["Subject"] = "Mã xác nhận đặt lại mật khẩu"

    # Nội dung email
    body = f"""
    Xin chào,
    
    Mã xác nhận của bạn là: {otp}
    
    Vui lòng không chia sẻ mã này với người khác.
    
    Trân trọng,
    Your App Team
    """
    message.attach(MIMEText(body, "plain"))

    try:
        # Kết nối SMTP server
        server = smtplib.SMTP(smtp_server, smtp_port)
        server.starttls()
        server.login(sender_email, sender_password)
        
        # Gửi email
        server.send_message(message)
        return True
    except Exception as e:
        logging.error(f"Error sending email: {str(e)}")
        return False
    finally:
        server.quit()

@forgot_password_bp.route('/forgot-password', methods=['POST'])
def forgot_password():
    try:
        data = request.get_json()
        if not data:
            return jsonify({
                'success': False,
                'message': 'Dữ liệu không hợp lệ'
            }), 400

        email = data.get('email', '').strip()

        # Kiểm tra email trống
        if not email:
            return jsonify({
                'success': False,
                'message': 'Vui lòng nhập Email'
            }), 400

        # Kiểm tra email trong database
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM Users WHERE email = ?', (email,))
        user = cursor.fetchone()

        if user is None:
            return jsonify({
                'success': False,
                'message': 'Email không tồn tại'
            }), 404

        # Tạo mã OTP ngẫu nhiên 6 số
        otp = ''.join([str(random.randint(0, 9)) for _ in range(6)])
        
        # Lưu OTP vào database
        cursor.execute('''
            INSERT OR REPLACE INTO EmailVerification (Email, OTP)
            VALUES (?, ?)
        ''', (email, otp))
        conn.commit()

        # Gửi OTP qua email
        if send_otp_email(email, otp):
            return jsonify({
                'success': True,
                'message': 'Mã xác nhận đã được gửi đến email của bạn',
                'email': email
            }), 200
        else:
            return jsonify({
                'success': False,
                'message': 'Không thể gửi mã xác nhận, vui lòng thử lại sau'
            }), 500

    except Exception as e:
        logging.error(f"Forgot password error: {str(e)}")
        return jsonify({
            'success': False,
            'message': 'Đã có lỗi xảy ra, vui lòng thử lại sau'
        }), 500
    finally:
        if 'conn' in locals():
            conn.close()
