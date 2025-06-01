from flask import Flask, jsonify, request
import sqlite3
import os
from account import account_bp
from category import category_bp
from report import report_bp

app = Flask(__name__)
app.register_blueprint(account_bp)
app.register_blueprint(category_bp)
app.register_blueprint(report_bp)

# Đường dẫn tới database
DB_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), 'database')
DB_PATH = os.path.join(DB_DIR, 'login_database.db')

def get_db_connection():
    print(f"[DEBUG] DB_PATH: {DB_PATH}")  # Thêm dòng này
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
    print("[LOG] Received POST /api/transactions")  # Log nhận request
    data = request.get_json()
    print(f"[LOG] Payload: {data}")  # Log nội dung request
    account_id = data.get('account_id')
    transaction_type = data.get('transaction_type')  # 'spend' or 'earn'
    amount = data.get('amount')
    category_id = data.get('category_id')
    note = data.get('note', '')

    # Kiểm tra các trường bắt buộc
    if not all([account_id, transaction_type in ('spend', 'earn'), amount is not None, category_id]):
        print("[LOG] Missing or invalid fields")
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
        print(f"[LOG] Inserted transaction_id: {transaction_id}")
        return jsonify({'success': True, 'transaction_id': transaction_id}), 201
    except Exception as e:
        print(f"[ERROR] {e}")
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

@app.route('/api/transactions/summary-by-date', methods=['GET'])
def summarize_transactions_by_date():
    print("[LOG] Received GET /api/transactions/summary-by-date")
    account_id = request.args.get('account_id')
    date = request.args.get('date')  # expected format: 'YYYY-MM-DD'
    category_id = request.args.get('category_id')  # optional

    if not account_id or not date:
        print("[LOG] Missing account_id or date")
        return jsonify({'success': False, 'message': 'Missing account_id or date'}), 400

    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        if category_id:
            print(f"[SUMMARY] account_id={account_id}, date={date}, category_id={category_id}")
            cursor.execute('''
                SELECT c.Category_name, t.Transaction_type, SUM(t.Amount) as Total
                FROM Transactions t
                JOIN Category c ON t.CategoryID = c.CategoryID
                WHERE t.AccountID = ? AND t.Transaction_date = ? AND t.CategoryID = ?
                GROUP BY t.CategoryID, t.Transaction_type
            ''', (account_id, date, category_id))
        else:
            print(f"[SUMMARY] account_id={account_id}, date={date}")
            cursor.execute('''
                SELECT c.Category_name, t.Transaction_type, SUM(t.Amount) as Total
                FROM Transactions t
                JOIN Category c ON t.CategoryID = c.CategoryID
                WHERE t.AccountID = ? AND t.Transaction_date = ?
                GROUP BY t.CategoryID, t.Transaction_type
            ''', (account_id, date))
        rows = cursor.fetchall()
        print(f"[SUMMARY] rows fetched: {len(rows)}")
        conn.close()

        summary = [
            {
                'category': row['Category_name'],
                'type': row['Transaction_type'],
                'total_amount': row['Total']
            } for row in rows
        ]
        print(f"[SUMMARY] summary: {summary}")
        return jsonify({'success': True, 'date': date, 'summary': summary}), 200
    except Exception as e:
        print(f"[SUMMARY][ERROR] {e}")
        return jsonify({'success': False, 'message': str(e)}), 500


if __name__ == '__main__':
    app.run(debug=True, host='127.0.0.1', port=5000)