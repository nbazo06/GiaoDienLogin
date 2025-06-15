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
    account_id = data.get('account_id')
    transaction_type = data.get('transaction_type')  # "Income" hoặc "Expense"
    amount = data.get('amount')
    category_id = data.get('CategoryID')
    transaction_date = data.get('transaction_date') or datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    money_source = data.get('money_source', '')
    note = data.get('note', '')
    now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

    if not all([account_id, transaction_type, amount, category_id]):
        return jsonify({'success': False, 'message': 'Thiếu thông tin bắt buộc'}), 400


    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('''
            INSERT INTO Transactions (AccountID, Transaction_type, Amount, CategoryID, Transaction_date, Money_source, Created_at, Updated_at, Note)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (account_id, transaction_type, amount, category_id, transaction_date, money_source, now, now, note))
        conn.commit()
        transaction_id = cursor.lastrowid
        conn.close()
        return jsonify({'success': True, 'transaction_id': transaction_id}), 201
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Lấy tất cả transaction (có thể lọc theo account_id, category_id, type, date)
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
                c.Category_name,
                c.Category_icon
            FROM Transactions t
            JOIN Category c ON t.CategoryID = c.CategoryID
            WHERE t.AccountID = ?
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

# Cập nhật transaction
@transactions_bp.route('/api/transactions/<int:transaction_id>', methods=['PUT'])
def update_transaction(transaction_id):
    data = request.get_json()
    fields = []
    values = []
    for field in ['account_id', 'transaction_type', 'amount', 'category_id', 'transaction_date', 'note']:
        if field in data:
            db_field = {
                'account_id': 'AccountID',
                'transaction_type': 'Transaction_type',
                'amount': 'Amount',
                'category_id': 'CategoryID',
                'transaction_date': 'Transaction_date',
                'note': 'Note'
            }[field]
            fields.append(f"{db_field} = ?")
            values.append(data[field])
    if not fields:
        return jsonify({'success': False, 'message': 'Không có trường nào để cập nhật'}), 400
    values.append(datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
    values.append(transaction_id)
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        # Kiểm tra transaction có tồn tại không
        cursor.execute('SELECT 1 FROM Transactions WHERE TransactionID = ?', (transaction_id,))
        if cursor.fetchone() is None:
            conn.close()
            return jsonify({'success': False, 'message': 'Transaction không tồn tại'}), 404

        sql = f"UPDATE Transactions SET {', '.join(fields)}, Updated_at = ? WHERE TransactionID = ?"
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