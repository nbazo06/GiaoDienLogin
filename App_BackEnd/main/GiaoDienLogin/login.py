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
            
@login_bp.route('/register-icon', methods=['POST'])
def register_icon():
    try:
        data = request.get_json()
        icons = data.get('icons', [])

        conn = get_db_connection()
        cursor = conn.cursor()

        # Kiểm tra xem bảng Icon đã có dữ liệu chưa
        cursor.execute('SELECT COUNT(*) FROM Icon')
        icon_count = cursor.fetchone()[0]

        if icon_count == 0 and icons:
            # Insert từng icon vào bảng Icon
            for icon in icons:
                icon_name = icon.get('icon_name')
                icon_path = icon.get('icon_path')
                if icon_name and icon_path is not None:
                    cursor.execute(
                        'INSERT INTO Icon (Icon_name, Icon_path) VALUES (?, ?)',
                        (icon_name, icon_path)
                    )
            conn.commit()
            return jsonify({'success': True, 'message': 'Đã insert icon thành công'}), 201
        else:
            return jsonify({'success': True, 'message': 'Bảng Icon đã có dữ liệu, không insert thêm'}), 200

    except Exception as e:
        import traceback
        traceback.print_exc()
        return jsonify({'success': False, 'message': f'Lỗi khi insert icon: {str(e)}'}), 500
    finally:
        if 'conn' in locals():
            conn.close()