from flask import Blueprint, request, jsonify
import sqlite3
import os
from datetime import datetime

import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from database import get_db_connection

category_bp = Blueprint('categories', __name__, url_prefix='/api')


# Tạo category mới
@category_bp.route('/categories', methods=['POST'])
def create_category():
    data = request.json
    user_id = data.get('UserID')
    category_name = data.get('Category_name')
    category_type = data.get('Category_type')
    category_icon = data.get('IconID')

    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        
        # Thêm Category mới
        cursor.execute('''
            INSERT INTO Category (UserID, Category_name, Category_type, IconID)
            VALUES (?, ?, ?, ?, ?)
        ''', (user_id, category_name, category_type, category_icon))

        conn.commit()
        return jsonify({'success': True, 'message': 'Category created successfully'}), 201
    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500
    finally:
        if 'conn' in locals():
            conn.close()

# Lấy tất cả category của user
@category_bp.route('/categories', methods=['GET'])
def get_categories():
    user_id = request.args.get('user_id')
    
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('''
            SELECT 
                c.CategoryID,
                c.Category_name,
                c.Category_type,
                i.Icon_path as IconID
            FROM Category c
            JOIN Icon i ON c.IconID = i.IconID
            WHERE c.UserID = ?
        ''', (user_id,))
        categories = [dict(row) for row in cursor.fetchall()]
        conn.close()
        print(f"Retrieved {len(categories)} categories for user {user_id}")
        return jsonify({'success': True, 'categories': categories}), 200
    except Exception as e:
        print(f"Error retrieving categories: {str(e)}")
        return jsonify({'success': False, 'message': str(e)}), 500

# Xem chi tiết category
@category_bp.route('/categories/<int:category_id>', methods=['GET'])
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
@category_bp.route('/categories/<int:category_id>', methods=['PUT'])
def update_category(category_id):
    data = request.get_json()
    fields = []
    values = []
    if 'category_name' in data:
        # Lấy user_id của category này
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('SELECT UserID FROM Category WHERE CategoryID = ?', (category_id,))
        row = cursor.fetchone()
        if not row:
            conn.close()
            return jsonify({'success': False, 'message': 'Category không tồn tại'}), 404
        user_id = row['UserID']
        # Kiểm tra trùng tên với các category khác của user này
        cursor.execute('SELECT 1 FROM Category WHERE Category_name = ? AND UserID = ? AND CategoryID != ?', (data['category_name'], user_id, category_id))
        if cursor.fetchone():
            conn.close()
            return jsonify({'success': False, 'message': 'Tên category đã tồn tại'}), 400
        conn.close()
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
@category_bp.route('/categories/<int:category_id>', methods=['DELETE'])
def delete_category(category_id):
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        # Kiểm tra category có tồn tại không
        cursor.execute('SELECT 1 FROM Category WHERE CategoryID = ?', (category_id,))
        if cursor.fetchone() is None:
            conn.close()
            return jsonify({'success': False, 'message': 'Category không tồn tại'}), 404

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
