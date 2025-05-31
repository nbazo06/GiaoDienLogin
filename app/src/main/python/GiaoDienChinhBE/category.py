from flask import Blueprint, request, jsonify
import sqlite3
from datetime import datetime

DB_PATH = r"C:\Users\ADMIN\StudioProjects\GiaoDienLogin\app\src\main\database\login_database.db"

category_bp = Blueprint('category', __name__)

def get_db_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

# Tạo category mới
@category_bp.route('/api/categories', methods=['POST'])
def create_category():
    data = request.get_json()
    category_name = data.get('category_name')
    user_id = data.get('user_id')
    now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

    if not all([category_name, user_id]):
        return jsonify({'success': False, 'message': 'Thiếu thông tin bắt buộc'}), 400

    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('''
            INSERT INTO Category (Category_name, UserID, Created_at, Updated_at)
            VALUES (?, ?, ?, ?)
        ''', (category_name, user_id, now, now))
        conn.commit()
        category_id = cursor.lastrowid
        conn.close()
        return jsonify({'success': True, 'category_id': category_id}), 201
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Lấy tất cả category của user
@category_bp.route('/api/categories', methods=['GET'])
def get_categories():
    user_id = request.args.get('user_id')
    if not user_id:
        return jsonify({'success': False, 'message': 'Thiếu user_id'}), 400
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM Category WHERE UserID = ?', (user_id,))
        categories = [dict(row) for row in cursor.fetchall()]
        conn.close()
        return jsonify({'success': True, 'categories': categories}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Xem chi tiết category
@category_bp.route('/api/categories/<int:category_id>', methods=['GET'])
def get_category_detail(category_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM Category WHERE CategoryID = ?', (category_id,))
        row = cursor.fetchone()
        conn.close()
        if row:
            return jsonify({'success': True, 'category': dict(row)}), 200
        else:
            return jsonify({'success': False, 'message': 'Không tìm thấy category'}), 404
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Cập nhật category
@category_bp.route('/api/categories/<int:category_id>', methods=['PUT'])
def update_category(category_id):
    data = request.get_json()
    fields = []
    values = []
    if 'category_name' in data:
        fields.append("Category_name = ?")
        values.append(data['category_name'])
    if not fields:
        return jsonify({'success': False, 'message': 'Không có trường nào để cập nhật'}), 400
    values.append(datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
    values.append(category_id)
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        sql = f"UPDATE Category SET {', '.join(fields)}, Updated_at = ? WHERE CategoryID = ?"
        cursor.execute(sql, values)
        conn.commit()
        conn.close()
        return jsonify({'success': True}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500

# Xóa category
@category_bp.route('/api/categories/<int:category_id>', methods=['DELETE'])
def delete_category(category_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        # Kiểm tra xem có transaction nào liên kết không
        cursor.execute('SELECT COUNT(*) FROM Transactions WHERE CategoryID = ?', (category_id,))
        count = cursor.fetchone()[0]
        if count > 0:
            conn.close()
            return jsonify({'success': False, 'message': 'Không thể xóa category vì còn giao dịch liên kết'}), 400
        cursor.execute('DELETE FROM Category WHERE CategoryID = ?', (category_id,))
        conn.commit()
        conn.close()
        return jsonify({'success': True}), 200
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500
