from flask import Blueprint, request, jsonify
import bcrypt
import logging
import re
from database import get_db_connection

register_bp = Blueprint('register', __name__, url_prefix='/api')

def validate_email(email):
    """Kiểm tra định dạng email"""
    pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
    return re.match(pattern, email) is not None

def validate_password(password):
    """Kiểm tra độ dài và định dạng mật khẩu"""
    if len(password) < 6:
        return False, "Mật khẩu phải có ít nhất 6 ký tự"
    if not re.search(r'[A-Za-z]', password):
        return False, "Mật khẩu phải chứa ít nhất một chữ cái"
    if not re.search(r'\d', password):
        return False, "Mật khẩu phải chứa ít nhất một số"
    return True, ""

@register_bp.route('/register', methods=['POST'])
def register():
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
        rePassword = data.get('rePassword', '').strip()

        # Kiểm tra trường bắt buộc
        if not email or not password or not rePassword:
            missing_fields = []
            if not email: missing_fields.append("Email")
            if not password: missing_fields.append("Mật khẩu")
            if not rePassword: missing_fields.append("Xác nhận mật khẩu")
            
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

        # Kiểm tra email đã tồn tại
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM users WHERE email = ?', (email,))
        existing_user = cursor.fetchone()
        
        if existing_user:
            logging.error(f"Email already exists: {email}")
            return jsonify({
                'success': False,
                'message': 'Email đã được sử dụng'
            }), 400

        # Kiểm tra mật khẩu
        is_valid_password, password_error = validate_password(password)
        if not is_valid_password:
            logging.error(f"Invalid password: {password_error}")
            return jsonify({
                'success': False,
                'message': password_error
            }), 400

        # Kiểm tra mật khẩu xác nhận
        if password != rePassword:
            logging.error("Password confirmation does not match")
            return jsonify({
                'success': False,
                'message': 'Mật khẩu xác nhận không khớp'
            }), 400

        # Mã hóa mật khẩu
        hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
        
        # Lưu người dùng mới
        cursor.execute('INSERT INTO users (email, password) VALUES (?, ?)',
                      (email, hashed_password))
        conn.commit()
        
        return jsonify({
            'success': True,
            'message': 'Đăng ký thành công'
        }), 201

    except Exception as e:
        logging.error(f"Registration error: {str(e)}")
        return jsonify({
            'success': False,
            'message': 'Đã có lỗi xảy ra, vui lòng thử lại sau'
        }), 500

    finally:
        if 'conn' in locals():
            conn.close() 