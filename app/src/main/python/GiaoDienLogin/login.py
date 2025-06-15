from flask import Blueprint, request, jsonify
import bcrypt
import logging
import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from database import get_db_connection

login_bp = Blueprint('login', __name__, url_prefix='/api')

@login_bp.route('/login', methods=['POST'])
def login():
    try:
        data = request.get_json()
        email = data.get('email', '').strip()
        password = data.get('password', '').strip()

        if not email or not password:
            return jsonify({'success': False, 'message': 'Vui lòng nhập Email và Mật khẩu'}), 400

        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM Users WHERE Email = ?', (email,))
        user = cursor.fetchone()

        if user is None:
            return jsonify({'success': False, 'message': 'Email không tồn tại'}), 401

        stored_password = user['Password']  # Chữ P hoa
        # Nếu lưu là string, phải encode lại khi check
        if bcrypt.checkpw(password.encode('utf-8'), stored_password.encode('utf-8')):
            return jsonify({'success': True, 'user_id': user['UserID'], 'message': 'Đăng nhập thành công'}), 200
        else:
            return jsonify({'success': False, 'message': 'Mật khẩu không chính xác'}), 401

    except Exception as e:
        logging.error(f"Login error: {str(e)}")
        return jsonify({'success': False, 'message': 'Đã có lỗi xảy ra, vui lòng thử lại sau'}), 500
    finally:
        if 'conn' in locals():
            conn.close()