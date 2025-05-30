from flask import Flask, jsonify, request

app = Flask(__name__)

@app.route('/')
def home():
    return "Welcome to GiaoDienChinh backend!"

# Example endpoint
@app.route('/api/data', methods=['GET'])
def get_data():
    # Replace with your actual data logic
    data = {"message": "This is sample data from GiaoDienChinh backend."}
    return jsonify(data)

# Expense management endpoints
@app.route('/api/transactions', methods=['POST'])
def add_transaction():
    data = request.get_json()
    user_id = data.get('user_id')
    type_ = data.get('type')  # 'spend' or 'earn'
    amount = data.get('amount')
    description = data.get('description', '')
    if not user_id or type_ not in ('spend', 'earn') or amount is None:
        return jsonify({'success': False, 'message': 'Missing or invalid fields'}), 400
    try:
        import sqlite3, os
        DB_PATH = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), 'database', 'login_database.db')
        conn = sqlite3.connect(DB_PATH)
        cursor = conn.cursor()
        cursor.execute('INSERT INTO transactions (user_id, type, amount, description) VALUES (?, ?, ?, ?)',
                       (user_id, type_, amount, description))
        conn.commit()
        transaction_id = cursor.lastrowid
        conn.close()
        return jsonify({'success': True, 'transaction_id': transaction_id}), 201
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

@app.route('/api/transactions/<int:transaction_id>', methods=['PUT'])
def update_transaction(transaction_id):
    data = request.get_json()
    fields = []
    values = []
    for field in ['type', 'amount', 'description']:
        if field in data:
            fields.append(f"{field} = ?")
            values.append(data[field])
    if not fields:
        return jsonify({'success': False, 'message': 'No fields to update'}), 400
    try:
        import sqlite3, os
        DB_PATH = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), 'database', 'login_database.db')
        conn = sqlite3.connect(DB_PATH)
        cursor = conn.cursor()
        sql = f"UPDATE transactions SET {', '.join(fields)} WHERE id = ?"
        values.append(transaction_id)
        cursor.execute(sql, values)
        conn.commit()
        conn.close()
        return jsonify({'success': True}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

@app.route('/api/transactions/<int:transaction_id>', methods=['DELETE'])
def delete_transaction(transaction_id):
    try:
        import sqlite3, os
        DB_PATH = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), 'database', 'login_database.db')
        conn = sqlite3.connect(DB_PATH)
        cursor = conn.cursor()
        cursor.execute('DELETE FROM transactions WHERE id = ?', (transaction_id,))
        conn.commit()
        conn.close()
        return jsonify({'success': True}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)
