from flask import Blueprint, request, jsonify
import bcrypt
import os
import re
import logging
from database import get_db_connection

login_bp = Blueprint('login', __name__, url_prefix='/api')

def validate_email(email):
    """Kiểm tra định dạng email"""
    pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
    return re.match(pattern, email) is not None

@login_bp.route('/login', methods=['POST'])
def login():
    try:
        data = request.get_json()
        if not data:
            logging.error("No JSON data received")
            return jsonify({
                'success': False,
                'message': 'Dữ liệu không hợp lệ'
            }), 400

        email = data.get('email', '').strip()
        password = data.get('password', '').strip()

        # Kiểm tra trường bắt buộc
        if not email or not password:
            missing_fields = []
            if not email: missing_fields.append("Email")
            if not password: missing_fields.append("Mật khẩu")
            
            message = f"Vui lòng nhập {', '.join(missing_fields)}"
            logging.error(f"Missing required fields: {message}")
            return jsonify({
                'success': False,
                'message': message
            }), 400

        # Kiểm tra định dạng email
        if not validate_email(email):
            logging.error(f"Invalid email format: {email}")
            return jsonify({
                'success': False,
                'message': 'Email không đúng định dạng (ví dụ: example@email.com)'
            }), 400

        conn = get_db_connection()
        cursor = conn.cursor()

        # Kiểm tra email tồn tại
        cursor.execute('SELECT * FROM users WHERE email = ?', (email,))
        user = cursor.fetchone()

        if user is None:
            logging.error(f"Email not found: {email}")
            return jsonify({
                'success': False,
                'message': 'Email không tồn tại'
            }), 401

        # Kiểm tra mật khẩu
        stored_password = user['password']
        if bcrypt.checkpw(password.encode('utf-8'), stored_password):
            logging.info(f"User logged in successfully: {email}")
            return jsonify({
                'success': True,
                'message': 'Đăng nhập thành công',
                'user': {
                    'id': user['id'],
                    'email': user['email']
                }
            }), 200
        else:
            logging.error(f"Invalid password for user: {email}")
            return jsonify({
                'success': False,
                'message': 'Mật khẩu không chính xác'
            }), 401

    except Exception as e:
        logging.error(f"Login error: {str(e)}")
        return jsonify({
            'success': False,
            'message': 'Đã có lỗi xảy ra, vui lòng thử lại sau'
        }), 500
    finally:
        if 'conn' in locals():
            conn.close() 