from flask import Blueprint, request, jsonify
import sqlite3
from datetime import datetime
import os

import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from database import get_db_connection

transactions_bp = Blueprint('transactions', __name__, url_prefix='/api')

# Tạo transaction mới
@transactions_bp.route('/transactions', methods=['POST'])
def create_transaction():
    data = request.get_json()
    user_id = data.get('UserID')
    transaction_type = data.get('Transaction_type')  # "Income" hoặc "Expense"
    amount = data.get('Amount')
    category_id = data.get('CategoryID')
    transaction_date = data.get('Transaction_date')
    wallet_id = data.get('WalletID', '')
    note = data.get('Note', '')
    now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

    if not all([user_id, transaction_type, amount, category_id]):
        return jsonify({'success': False, 'message': 'Thiếu thông tin bắt buộc'}), 400


    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('''
            INSERT INTO Transactions (UserID, Transaction_type, Amount, CategoryID, Transaction_date, WalletID, Created_at, Updated_at, Note)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (user_id, transaction_type, amount, category_id, transaction_date, wallet_id, now, now, note))
        conn.commit()
        transaction_id = cursor.lastrowid
        conn.close()
        return jsonify({'success': True, 'transaction_id': transaction_id}), 201
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Lấy tất cả transaction (có thể lọc theo wallet_id, category_id, type, date)
@transactions_bp.route('/transactions', methods=['GET'])
def get_transactions():
    user_id = request.args.get('user_id')
    
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        
        # Sử dụng JOIN để lấy thêm Category_name và Category_icon từ bảng Category
        cursor.execute('''
            SELECT 
                t.Transaction_date,
                t.Amount,
                t.Transaction_type,
                t.WalletID,
                c.Category_name,
                c.Category_icon
            FROM Transactions t
            JOIN Category c ON t.CategoryID = c.CategoryID
            WHERE t.UserID = ?
            ORDER BY t.Transaction_date DESC
        ''', (user_id,))
        
        transactions = [dict(row) for row in cursor.fetchall()]
        conn.close()
        
        return jsonify({'success': True, 'transactions': transactions}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Xem chi tiết transaction
@transactions_bp.route('/api/transactions/<int:transaction_id>', methods=['GET'])
def get_transaction_detail(transaction_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM Transactions WHERE TransactionID = ?', (transaction_id,))
        row = cursor.fetchone()
        conn.close()
        if row:
            return jsonify({'success': True, 'transaction': dict(row)}), 200
        else:
            return jsonify({'success': False, 'message': 'Không tìm thấy transaction'}), 404
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

@transactions_bp.route('/api/transactions/<transaction_id>', methods=['PUT'])
def update_transaction(transaction_id):
    data = request.get_json()

    # Lấy dữ liệu từ request
    transaction_type = data['Transaction_type']
    amount = data['Amount']
    category_id = data['CategoryID']
    transaction_date = data['Transaction_date']
    wallet_id = data['WalletID']
    note = data['Note']
    now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

    # Danh sách cột cần cập nhật
    fields = [
        "Amount = ?",
        "Transaction_type = ?",
        "CategoryID = ?",
        "Transaction_date = ?",
        "WalletID = ?",
        "Note = ?",
        "Updated_at = ?"
    ]

    values = [
        amount,
        transaction_type,
        category_id,
        transaction_date,
        wallet_id,
        note,
        now,
        transaction_id  # WHERE TransactionID = ?
    ]

    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        sql = f"UPDATE Transactions SET {', '.join(fields)} WHERE TransactionID = ?"
        cursor.execute(sql, values)
        conn.commit()
        conn.close()

        return jsonify({'success': True}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Xóa transaction
@transactions_bp.route('/api/transactions/<int:transaction_id>', methods=['DELETE'])
def delete_transaction(transaction_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        # Kiểm tra transaction có tồn tại không
        cursor.execute('SELECT 1 FROM Transactions WHERE TransactionID = ?', (transaction_id,))
        if cursor.fetchone() is None:
            conn.close()
            return jsonify({'success': False, 'message': 'Transaction không tồn tại'}), 404

        cursor.execute('DELETE FROM Transactions WHERE TransactionID = ?', (transaction_id,))
        conn.commit()
        conn.close()
        return jsonify({'success': True}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500