from flask import Blueprint, request, jsonify
import sqlite3
import os
from datetime import datetime

# Đường dẫn tuyệt đối tới file database
DB_PATH = r"C:\Users\ADMIN\StudioProjects\GiaoDienLogin\app\src\main\database\login_database.db"

account_bp = Blueprint('account', __name__)

def get_db_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

# Tạo tài khoản mới
@account_bp.route('/api/accounts', methods=['POST'])
def create_account():
    data = request.get_json()
    account_type = data.get('account_type')  # 'Tiền mặt' hoặc 'Ngân hàng'
    account_name = data.get('account_name')
    balance = data.get('balance', 0)
    user_id = data.get('user_id')
    now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

    if account_type not in ('Tiền mặt', 'Ngân hàng'):
        return jsonify({'success': False, 'message': 'Loại tài khoản không hợp lệ'}), 400
    if not all([account_name, user_id]):
        return jsonify({'success': False, 'message': 'Thiếu thông tin bắt buộc'}), 400
    if float(balance) < 0:
        return jsonify({'success': False, 'message': 'Số dư không được âm'}), 400

    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('''
            INSERT INTO Account (Account_type, Account_name, Balance, UserID, Created_at, Updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
        ''', (account_type, account_name, balance, user_id, now, now))
        conn.commit()
        account_id = cursor.lastrowid
        conn.close()
        return jsonify({'success': True, 'account_id': account_id}), 201
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Lấy danh sách tài khoản theo user
@account_bp.route('/api/accounts', methods=['GET'])
def get_accounts():
    user_id = request.args.get('user_id')
    if not user_id:
        return jsonify({'success': False, 'message': 'Thiếu user_id'}), 400
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM Account WHERE UserID = ?', (user_id,))
        accounts = [dict(row) for row in cursor.fetchall()]
        conn.close()
        return jsonify({'success': True, 'accounts': accounts}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Xem chi tiết tài khoản
@account_bp.route('/api/accounts/<int:account_id>', methods=['GET'])
def get_account_detail(account_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM Account WHERE AccountID = ?', (account_id,))
        row = cursor.fetchone()
        conn.close()
        if row:
            return jsonify({'success': True, 'account': dict(row)}), 200
        else:
            return jsonify({'success': False, 'message': 'Không tìm thấy tài khoản'}), 404
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Cập nhật tài khoản
@account_bp.route('/api/accounts/<int:account_id>', methods=['PUT'])
def update_account(account_id):
    data = request.get_json()
    fields = []
    values = []
    for field in ['account_type', 'account_name', 'balance']:
        if field in data:
            if field == 'balance' and float(data[field]) < 0:
                return jsonify({'success': False, 'message': 'Số dư không được âm'}), 400
            db_field = 'Account_type' if field == 'account_type' else field.capitalize()
            fields.append(f"{db_field} = ?")
            values.append(data[field])
    if not fields:
        return jsonify({'success': False, 'message': 'Không có trường nào để cập nhật'}), 400
    values.append(datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
    values.append(account_id)
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        sql = f"UPDATE Account SET {', '.join(fields)}, Updated_at = ? WHERE AccountID = ?"
        cursor.execute(sql, values)
        conn.commit()
        conn.close()
        return jsonify({'success': True}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Xóa tài khoản
@account_bp.route('/api/accounts/<int:account_id>', methods=['DELETE'])
def delete_account(account_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        # Kiểm tra xem có transaction nào liên kết không
        cursor.execute('SELECT COUNT(*) FROM Transactions WHERE AccountID = ?', (account_id,))
        count = cursor.fetchone()[0]
        if count > 0:
            conn.close()
            return jsonify({'success': False, 'message': 'Không thể xóa tài khoản vì còn giao dịch liên kết'}), 400
        cursor.execute('DELETE FROM Account WHERE AccountID = ?', (account_id,))
        conn.commit()
        conn.close()
        return jsonify({'success': True}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500
