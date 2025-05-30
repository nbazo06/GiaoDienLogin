from flask import Flask, jsonify, request
import sqlite3
import os

app = Flask(__name__)

# Đường dẫn tới database
DB_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'database')
DB_PATH = os.path.join(DB_DIR, 'login_database.db')

def get_db_connection():
    """Tạo và trả về kết nối đến database"""
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

@app.route('/')
def home():
    return "Welcome to GiaoDienChinh backend!"

# Example endpoint
@app.route('/api/data', methods=['GET'])
def get_data():
    data = {"message": "This is sample data from GiaoDienChinh backend."}
    return jsonify(data)

# Endpoint để thêm giao dịch
@app.route('/api/transactions', methods=['POST'])
def add_transaction():
    data = request.get_json()
    account_id = data.get('account_id')
    transaction_type = data.get('transaction_type')  # 'spend' or 'earn'
    amount = data.get('amount')
    category_id = data.get('category_id')
    note = data.get('note', '')

    # Kiểm tra các trường bắt buộc
    if not all([account_id, transaction_type in ('spend', 'earn'), amount is not None, category_id]):
        return jsonify({'success': False, 'message': 'Missing or invalid fields'}), 400

    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('''
            INSERT INTO Transactions (AccountID, Transaction_type, Amount, CategoryID, Note)
            VALUES (?, ?, ?, ?, ?)
        ''', (account_id, transaction_type, amount, category_id, note))
        conn.commit()
        transaction_id = cursor.lastrowid
        conn.close()
        return jsonify({'success': True, 'transaction_id': transaction_id}), 201
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Endpoint để cập nhật giao dịch
@app.route('/api/transactions/<int:transaction_id>', methods=['PUT'])
def update_transaction(transaction_id):
    data = request.get_json()
    fields = []
    values = []

    # Các cột có thể cập nhật
    for field in ['transaction_type', 'amount', 'category_id', 'note']:
        if field in data:
            # Chuyển đổi tên trường sang tên cột trong database
            db_field = 'Transaction_type' if field == 'transaction_type' else field.capitalize()
            fields.append(f"{db_field} = ?")
            values.append(data[field])

    if not fields:
        return jsonify({'success': False, 'message': 'No fields to update'}), 400

    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        sql = f"UPDATE Transactions SET {', '.join(fields)} WHERE TransactionID = ?"
        values.append(transaction_id)
        cursor.execute(sql, values)
        conn.commit()
        conn.close()
        return jsonify({'success': True}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Endpoint để xóa giao dịch
@app.route('/api/transactions/<int:transaction_id>', methods=['DELETE'])
def delete_transaction(transaction_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('DELETE FROM Transactions WHERE TransactionID = ?', (transaction_id,))
        conn.commit()
        conn.close()
        return jsonify({'success': True}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)