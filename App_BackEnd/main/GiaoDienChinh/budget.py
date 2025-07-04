from flask import Blueprint, request, jsonify
import sqlite3
import os
from datetime import datetime

import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from database import get_db_connection

budget_bp = Blueprint('budget', __name__, url_prefix='/api')

@budget_bp.route('/budgets', methods=['POST'])
def create_budget():
    """Tạo ngân sách mới cho một danh mục"""
    data = request.get_json()
    user_id = data.get('UserID')
    category_id = data.get('CategoryID')
    budget_limit = data.get('Budget_limit')
    wallet_id = data.get('WalletID')
    start_date = data.get('Start_date', datetime.now().strftime('%Y-%m-%d'))
    end_date = data.get('End_date')

    # Validate required fields
    if not all([user_id, category_id, wallet_id, budget_limit]):
        return jsonify({
            'success': False,
            'message': 'Thiếu thông tin bắt buộc (user_id, category_id, wallet_id, budget_limit)'
        }), 400

    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Create new budget
        cursor.execute('''
            INSERT INTO Budget (
                UserID, CategoryID, WalletID, Budget_limit, Start_date, End_date
            )
            VALUES (?, ?, ?, ?, ?, ?)
        ''', (user_id, category_id, wallet_id, budget_limit, start_date, end_date))

        budget_id = cursor.lastrowid
        conn.commit()
        conn.close()

        return jsonify({
            'success': True,
            'message': 'Tạo ngân sách thành công',
            'budget_id': budget_id
        }), 201

    except Exception as e:
        print(f"[ERROR] Create budget: {e}")
        return jsonify({
            'success': False,
            'message': str(e)
        }), 500

@budget_bp.route('/budgets', methods=['GET'])
def get_budgets():
    """Lấy danh sách ngân sách của người dùng"""
    user_id = request.args.get('user_id')
    if not user_id:
        return jsonify({
            'success': False,
            'message': 'Thiếu user_id'
        }), 400

    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        cursor.execute('''
        SELECT 
            b.BudgetID,
            b.CategoryID,
            b.Budget_limit,
            b.Start_date,
            b.End_date,
            b.WalletID,
            c.Category_name,
            i.Icon_path
        FROM Budget b
        JOIN Category c ON b.CategoryID = c.CategoryID
        JOIN Icon i ON c.IconID = i.IconID
        WHERE b.UserID = ?
        ORDER BY b.Created_at DESC
    ''', (user_id,))
        budgets = [dict(row) for row in cursor.fetchall()]
        conn.close()
        return jsonify({
            'success': True,
            'budgets': budgets
        }), 200

    except Exception as e:
        print(f"[ERROR] Get budgets: {e}")
        return jsonify({
            'success': False,
            'message': str(e)
        }), 500

@budget_bp.route('/budgets/<int:budget_id>', methods=['GET'])
def get_budget_detail(budget_id):
    """Lấy chi tiết một ngân sách"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Get budget details with account and category info
        cursor.execute('''
            SELECT 
                b.*,
                c.Category_name,
                w.Name,
                COALESCE(SUM(t.Amount), 0) as spent_amount,
                COUNT(DISTINCT t.TransactionID) as transaction_count
            FROM Budget b
            JOIN Category c ON b.CategoryID = c.CategoryID
            JOIN Wallet w ON b.WalletID = w.WalletID
            LEFT JOIN Transactions t ON 
                b.CategoryID = t.CategoryID AND
                b.WalletID = t.WalletID AND
                t.Transaction_date >= b.Start_date AND
                (t.Transaction_date <= b.End_date OR b.End_date IS NULL)
            WHERE b.BudgetID = ?
            GROUP BY b.BudgetID
        ''', (budget_id,))

        row = cursor.fetchone()
        if not row:
            return jsonify({
                'success': False,
                'message': 'Không tìm thấy ngân sách'
            }), 404

        budget = dict(row)
        spent = float(budget['spent_amount'] or 0)
        limit = float(budget['Budget_limit'])
        budget['remaining_amount'] = limit - spent
        budget['progress_percentage'] = (spent / limit * 100) if limit > 0 else 0
        budget['is_exceeded'] = spent > limit

        # Get recent transactions
        cursor.execute('''
            SELECT 
                t.TransactionID,
                t.Transaction_date,
                t.Amount,
                t.Transaction_type,
                t.Note
            FROM Transactions t
            WHERE 
                t.CategoryID = ? AND
                t.WalletID = ? AND
                t.Transaction_date >= ? AND
                (t.Transaction_date <= ? OR ? IS NULL)
            ORDER BY t.Transaction_date DESC
            LIMIT 5
        ''', (budget['CategoryID'], budget['WalletID'], 
              budget['Start_date'], budget['End_date'], 
              budget['End_date']))

        budget['recent_transactions'] = [dict(row) for row in cursor.fetchall()]

        conn.close()
        return jsonify({
            'success': True,
            'budget': budget
        }), 200

    except Exception as e:
        print(f"[ERROR] Get budget detail: {e}")
        return jsonify({
            'success': False,
            'message': str(e)
        }), 500

@budget_bp.route('/api/budgets/<int:budget_id>', methods=['PUT'])
def update_budget(budget_id):
    """Cập nhật thông tin ngân sách"""
    data = request.get_json()
    
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Check if budget exists
        cursor.execute('''
            SELECT UserID, CategoryID, WalletID 
            FROM Budget 
            WHERE BudgetID = ?
        ''', (budget_id,))
        existing_budget = cursor.fetchone()
        if not existing_budget:
            return jsonify({
                'success': False,
                'message': 'Không tìm thấy ngân sách'
            }), 404

        # Build update query
        update_fields = []
        params = []

        if 'budget_limit' in data:
            try:
                limit = float(data['budget_limit'])
                if limit <= 0:
                    return jsonify({
                        'success': False,
                        'message': 'Hạn mức ngân sách phải là số dương'
                    }), 400
                update_fields.append('Budget_limit = ?')
                params.append(limit)
            except ValueError:
                return jsonify({
                    'success': False,
                    'message': 'Hạn mức ngân sách không hợp lệ'
                }), 400

        if 'repeat_type' in data:
            repeat_type = data['repeat_type']
            valid_repeat_types = ['none', 'daily', 'weekly', 'monthly', 'yearly']
            if repeat_type not in valid_repeat_types:
                return jsonify({
                    'success': False,
                    'message': f'Repeat_type phải là một trong các giá trị: {", ".join(valid_repeat_types)}'
                }), 400
            update_fields.append('Repeat_type = ?')
            params.append(repeat_type)

        if 'end_date' in data:
            update_fields.append('End_date = ?')
            params.append(data['end_date'])

        if not update_fields:
            return jsonify({
                'success': False,
                'message': 'Không có thông tin cần cập nhật'
            }), 400

        # Add updated_at
        update_fields.append('Updated_at = CURRENT_TIMESTAMP')
        
        # Add budget_id to params
        params.append(budget_id)

        # Execute update
        query = f'''
            UPDATE Budget 
            SET {', '.join(update_fields)}
            WHERE BudgetID = ?
        '''
        cursor.execute(query, params)
        conn.commit()
        conn.close()

        return jsonify({
            'success': True,
            'message': 'Cập nhật ngân sách thành công'
        }), 200

    except Exception as e:
        print(f"[ERROR] Update budget: {e}")
        return jsonify({
            'success': False,
            'message': str(e)
        }), 500

@budget_bp.route('/api/budgets/<int:budget_id>', methods=['DELETE'])
def delete_budget(budget_id):
    """Xóa một ngân sách"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Check if budget exists
        cursor.execute('''
            SELECT BudgetID FROM Budget WHERE BudgetID = ?
        ''', (budget_id,))
        if not cursor.fetchone():
            return jsonify({
                'success': False,
                'message': 'Không tìm thấy ngân sách'
            }), 404

        # Delete budget
        cursor.execute('DELETE FROM Budget WHERE BudgetID = ?', (budget_id,))
        conn.commit()
        conn.close()

        return jsonify({
            'success': True,
            'message': 'Xóa ngân sách thành công'
        }), 200

    except Exception as e:
        print(f"[ERROR] Delete budget: {e}")
        return jsonify({
            'success': False,
            'message': str(e)
        }), 500

@budget_bp.route('/api/budgets/overview', methods=['GET'])
def get_budget_overview():
    """Lấy tổng quan về tình hình ngân sách"""
    user_id = request.args.get('user_id')
    wallet_id = request.args.get('wallet_id')

    if not user_id:
        return jsonify({
            'success': False,
            'message': 'Thiếu user_id'
        }), 400

    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Get active budgets summary
        query = '''
            SELECT 
                COUNT(DISTINCT b.BudgetID) as total_budgets,
                COUNT(DISTINCT CASE WHEN COALESCE(spent.amount, 0) > b.Budget_limit 
                    THEN b.BudgetID END) as exceeded_budgets,
                SUM(b.Budget_limit) as total_budget_limit,
                COALESCE(SUM(spent.amount), 0) as total_spent
            FROM Budget b
            LEFT JOIN (
                SELECT 
                    b2.BudgetID,
                    SUM(t.Amount) as amount
                FROM Budget b2
                LEFT JOIN Transactions t ON 
                    b2.CategoryID = t.CategoryID AND
                    b2.WalletID = t.WalletID AND
                    t.Transaction_date >= b2.Start_date AND
                    (t.Transaction_date <= b2.End_date OR b2.End_date IS NULL)
                WHERE b2.UserID = ?
                GROUP BY b2.BudgetID
            ) spent ON b.BudgetID = spent.BudgetID
            WHERE b.UserID = ? 
            AND (b.End_date IS NULL OR b.End_date >= date('now'))
        '''
        params = [user_id, user_id]

        if wallet_id:
            query = query.replace('WHERE b.UserID = ?', 'WHERE b.UserID = ? AND b.WalletID = ?')
            params = [user_id, user_id, wallet_id]

        cursor.execute(query, params)
        overview = dict(cursor.fetchone())

        # Get budgets by category
        cursor.execute('''
            SELECT 
                c.Category_name,
                a.Account_name,
                b.Budget_limit,
                COALESCE(SUM(t.Amount), 0) as spent_amount
            FROM Budget b
            JOIN Category c ON b.CategoryID = c.CategoryID
            JOIN Account a ON b.WalletID = a.WalletID
            LEFT JOIN Transactions t ON 
                b.CategoryID = t.CategoryID AND
                b.WalletID = t.WalletID AND
                t.Transaction_date >= b.Start_date AND
                (t.Transaction_date <= b.End_date OR b.End_date IS NULL)
            WHERE b.UserID = ? AND 
                (b.End_date IS NULL OR b.End_date >= date('now'))
            GROUP BY b.BudgetID
            ORDER BY (COALESCE(SUM(t.Amount), 0) / b.Budget_limit) DESC
        ''', (user_id,))

        overview['category_breakdown'] = [dict(row) for row in cursor.fetchall()]

        conn.close()
        return jsonify({
            'success': True,
            'overview': overview
        }), 200

    except Exception as e:
        print(f"[ERROR] Get budget overview: {e}")
        return jsonify({
            'success': False,
            'message': str(e)
        }), 500
