from flask import Blueprint, request, jsonify
import sqlite3
import os
from datetime import datetime

import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from database import get_db_connection

wallet_bp = Blueprint('wallet', __name__, url_prefix='/api')

# Tạo tài khoản mới
@wallet_bp.route('/wallet', methods=['POST'])
def create_wallet():
    data = request.get_json()
    wallet_type = data.get('Type')  # 'Tiền mặt' hoặc 'Ngân hàng'
    wallet_name = data.get('Name')
    wallet_icon = data.get('Icon')  # Mặc định là icon tiền mặt
    balance = data.get('Balance', 0)
    user_id = data.get('UserID')

    # Kiểm tra loại tài khoản
    if wallet_type not in ('Tiền mặt', 'Ngân hàng'):
        return jsonify({'success': False, 'message': 'Loại tài khoản không hợp lệ'}), 400
    
    # Kiểm tra thông tin bắt buộc
    if not all([wallet_name, user_id]):
        return jsonify({'success': False, 'message': 'Thiếu thông tin bắt buộc'}), 400

    # Kiểm tra số dư
    try:
        balance = float(balance)
        if balance < 0:
            return jsonify({'success': False, 'message': 'Số dư không được âm'}), 400
    except ValueError:
        return jsonify({'success': False, 'message': 'Số dư không hợp lệ'}), 400

    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Kiểm tra trùng tên tài khoản cho cùng user
        cursor.execute('SELECT 1 FROM Wallet WHERE Name = ? AND UserID = ?', (wallet_name, user_id))
        if cursor.fetchone():
            conn.close()
            return jsonify({'success': False, 'message': 'Tên tài khoản đã tồn tại'}), 400

        cursor.execute('''
            INSERT INTO Wallet (UserID, Name, Balance, Type, Icon)
            VALUES (?, ?, ?, ?, ?)
        ''', (user_id, wallet_name, balance, wallet_type, wallet_icon))
        conn.commit()
        wallet_id = cursor.lastrowid
        conn.close()
        return jsonify({'success': True, 'wallet_id': wallet_id}), 201
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Xem danh sách tài khoản (ví) của user
@wallet_bp.route('/wallets', methods=['GET'])
def get_wallets():
    user_id = request.args.get('user_id')
    try:
        conn = get_db_connection()
        conn.row_factory = sqlite3.Row  # Ensure rows are dict-like
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM Wallet WHERE UserID = ?', (user_id,))
        rows = cursor.fetchall()
        conn.close()
        wallets = []
        for row in rows:
            wallet = {
                'WalletID': row['WalletID'],
                'Name': row['Name'],
                'Type': row['Type'],
                'Balance': row['Balance'],
                'Icon': row['Icon'],
                'Created_at': row['Created_at'],
                'Updated_at': row['Updated_at']
            }
            wallets.append(wallet)
        return jsonify({'success': True, 'wallets': wallets}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Cập nhật tài khoản
@wallet_bp.route('/wallet/<int:wallet_id>', methods=['PUT'])
def update_wallet(wallet_id):
    data = request.get_json()
    
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        
        # Kiểm tra tài khoản tồn tại
        cursor.execute('SELECT UserID FROM Wallet WHERE AccountID = ?', (wallet_id,))
        row = cursor.fetchone()
        if not row:
            conn.close()
            return jsonify({'success': False, 'message': 'Tài khoản không tồn tại'}), 404
        
        user_id = row['UserID']
        fields = []
        values = []
        
        for field in ['wallet_type', 'wallet_name', 'balance']:
            if field in data:
                # Kiểm tra loại tài khoản
                if field == 'wallet_type' and data[field] not in ('Tiền mặt', 'Ngân hàng'):
                    conn.close()
                    return jsonify({'success': False, 'message': 'Loại tài khoản không hợp lệ'}), 400
                
                # Kiểm tra số dư
                if field == 'balance':
                    try:
                        balance = float(data[field])
                        if balance < 0:
                            conn.close()
                            return jsonify({'success': False, 'message': 'Số dư không được âm'}), 400
                    except ValueError:
                        conn.close()
                        return jsonify({'success': False, 'message': 'Số dư không hợp lệ'}), 400
                
                # Kiểm tra trùng tên khi đổi tên
                if field == 'wallet_name':
                    cursor.execute('SELECT 1 FROM Wallet WHERE Account_name = ? AND UserID = ? AND AccountID != ?', 
                                 (data[field], user_id, wallet_id))
                    if cursor.fetchone():
                        conn.close()
                        return jsonify({'success': False, 'message': 'Tên tài khoản đã tồn tại'}), 400
                
                db_field = 'Account_type' if field == 'wallet_type' else field.capitalize()
                fields.append(f"{db_field} = ?")
                values.append(data[field])

        if not fields:
            conn.close()
            return jsonify({'success': False, 'message': 'Không có trường nào để cập nhật'}), 400

        values.append(datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
        values.append(wallet_id)
        
        sql = f"UPDATE Wallet SET {', '.join(fields)}, Updated_at = ? WHERE AccountID = ?"
        cursor.execute(sql, values)
        conn.commit()
        conn.close()
        return jsonify({'success': True}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Xóa tài khoản
@wallet_bp.route('/wallet/<int:wallet_id>', methods=['DELETE'])
def delete_wallet(wallet_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        # Kiểm tra xem có transaction nào liên kết không
        cursor.execute('SELECT COUNT(*) FROM Transactions WHERE AccountID = ?', (wallet_id,))
        count = cursor.fetchone()[0]
        if count > 0:
            conn.close()
            return jsonify({'success': False, 'message': 'Không thể xóa tài khoản vì còn giao dịch liên kết'}), 400
        cursor.execute('DELETE FROM Wallet WHERE AccountID = ?', (wallet_id,))
        conn.commit()
        conn.close()
        return jsonify({'success': True}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500
