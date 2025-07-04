from flask import Blueprint, request, jsonify
from .register import validate_password
import bcrypt
import logging
import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from database import get_db_connection

new_password_bp = Blueprint('new-password', __name__, url_prefix='/api')
@new_password_bp.route('/new-password', methods=['POST'])
def new_password():
    try:
        data = request.get_json()
        email = data.get('email', '').strip()
        password = data.get('newPassword', '').strip()
        rePassword = data.get('reNewPassword', '').strip()

        is_valid_password, password_error = validate_password(password)
        if not is_valid_password:
            return jsonify({'success': False, 'message': password_error}), 400

        if password != rePassword:
            return jsonify({'success': False, 'message': 'Mật khẩu xác nhận không khớp'}), 400

        # Mã hóa mật khẩu mới
        hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')
        
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('UPDATE Users SET Password = ? WHERE Email = ?', (hashed_password, email))
        conn.commit()

        return jsonify({'success': True, 'message': 'Mật khẩu đã được cập nhật thành công'}), 200

    except Exception as e:
        logging.error(f"New password error: {str(e)}")
        return jsonify({'success': False, 'message': 'Đã có lỗi xảy ra, vui lòng thử lại sau'}), 500
    finally:
        if 'conn' in locals():
            conn.close()