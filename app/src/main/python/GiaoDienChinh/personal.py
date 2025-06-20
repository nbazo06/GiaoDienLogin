from flask import Blueprint, request, jsonify
import sqlite3
import os
from datetime import datetime

import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from database import get_db_connection

account_bp = Blueprint('personal', __name__, url_prefix='/api')

# Lấy danh sách tài khoản theo user
@account_bp.route('/personal', methods=['GET'])
def get_accounts():
    user_id = request.args.get('user_id')
    
    if not user_id:
        return jsonify({'success': False, 'message': 'Thiếu user_id'}), 400

    try:
        conn = get_db_connection()
        conn.row_factory = sqlite3.Row
        cursor = conn.cursor()

        cursor.execute('SELECT Email FROM Users WHERE UserID = ?', (user_id,))
        row = cursor.fetchone()

        conn.close()

        if row:
            return jsonify({'success': True, 'email': row['Email']}), 200
        else:
            return jsonify({'success': False, 'message': 'Không tìm thấy user'}), 404

    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500