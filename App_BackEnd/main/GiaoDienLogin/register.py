from flask import Blueprint, request, jsonify
import bcrypt
import logging
import re

import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from database import get_db_connection

register_bp = Blueprint('register', __name__, url_prefix='/api')

def validate_email(email):
    pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
    return re.match(pattern, email) is not None

def validate_password(password):
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
            return jsonify({'success': False, 'message': 'Dữ liệu không hợp lệ'}), 400

        email = data.get('email', '').strip()
        password = data.get('password', '').strip()
        rePassword = data.get('rePassword', '').strip()

        # Kiểm tra trường bắt buộc
        if not email or not password or not rePassword:
            return jsonify({'success': False, 'message': 'Vui lòng nhập đầy đủ Email, Mật khẩu, Xác nhận mật khẩu'}), 400

        if not validate_email(email):
            return jsonify({'success': False, 'message': 'Email không đúng định dạng'}), 400

        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM Users WHERE Email = ?', (email,))
        if cursor.fetchone():
            return jsonify({'success': False, 'message': 'Email đã được sử dụng'}), 400

        is_valid_password, password_error = validate_password(password)
        if not is_valid_password:
            return jsonify({'success': False, 'message': password_error}), 400

        if password != rePassword:
            return jsonify({'success': False, 'message': 'Mật khẩu xác nhận không khớp'}), 400

        hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')

        cursor.execute(
            'INSERT INTO Users (Email, Password) VALUES (?, ?)',
            (email, hashed_password)
        )
        user_id = cursor.lastrowid
        
        # Insert default categories
        default_categories = [
            ('Ăn uống', 'expense', 21),
            ('Giải trí', 'expense', 19),
            ('Hóa đơn', 'expense', 8),
            ('Chợ, siêu thị', 'expense', 20),
            ('Di chuyển', 'expense', 18),
            ('Tiền chuyển đi', 'expense', 2530),
            ('Thuê nhà', 'expense', 22),
            ('Sửa chữa, bảo trì', 'expense', 24),
            ('Y tế', 'expense', 17),
            
            ('Học Bổng', 'income', 25),
            ('Lương', 'income', 29),
            ('Tiền chuyển đến', 'income', 29),
            ('Thưởng', 'income', 12),
            ('Tiền lãi', 'income', 16),
            
            ('Khác', 'income', 10),
            ('Khác', 'expense', 10)
        ]
        for category_name, category_type, category_icon in default_categories:
            cursor.execute('''
                INSERT INTO Category (UserID, Category_name, Category_type, IconID)
                VALUES (?, ?, ?, ?)
            ''', (user_id, category_name, category_type, int(category_icon)))

        # Insert default wallets
        default_wallets = [
            (user_id, 'Ví tiền mặt', 0, 'cash', '2130968585'),
            (user_id, 'Ví ngân hàng', 0, 'bank', '2130968578')
        ]
        
        for user_id, wallet_name, balance, account_type, account_icon in default_wallets:
            cursor.execute('''
                INSERT INTO Wallet (UserID, Name, Balance, Type, Icon)
                VALUES (?, ?, ?, ?, ?)
            ''', (user_id, wallet_name, balance, account_type, account_icon))
        
        conn.commit()
        return jsonify({
            'success': True, 
            'message': 'Đăng ký thành công',
            'user_id': user_id
        }), 201

    except Exception as e:
        logging.error(f"Register error: {str(e)}")
        return jsonify({'success': False, 'message': 'Đã có lỗi xảy ra, vui lòng thử lại sau'}), 500
    finally:
        if 'conn' in locals():
            conn.close()