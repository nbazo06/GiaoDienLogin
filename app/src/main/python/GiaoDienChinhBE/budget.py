from flask import Blueprint, request, jsonify
import sqlite3
import os
from datetime import datetime

# Get relative path from current file location
current_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.dirname(os.path.dirname(os.path.dirname(current_dir)))
DB_PATH = os.path.join(project_root, 'database', 'login_database.db')

# Create database directory if it doesn't exist
os.makedirs(os.path.dirname(DB_PATH), exist_ok=True)

budget_bp = Blueprint('budget', __name__)

def get_db_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

@budget_bp.route('/api/budgets', methods=['POST'])
def create_budget():
    """Tạo ngân sách mới cho một danh mục"""
    data = request.get_json()
    user_id = data.get('user_id')
    category_id = data.get('category_id')
    account_id = data.get('account_id')
    budget_limit = data.get('budget_limit')
    repeat_type = data.get('repeat_type', 'none')
    start_date = data.get('start_date', datetime.now().strftime('%Y-%m-%d'))
    end_date = data.get('end_date')

    # Validate required fields
    if not all([user_id, category_id, account_id, budget_limit]):
        return jsonify({
            'success': False,
            'message': 'Thiếu thông tin bắt buộc (user_id, category_id, account_id, budget_limit)'
        }), 400

    # Validate repeat_type
    valid_repeat_types = ['none', 'daily', 'weekly', 'monthly', 'yearly']
    if repeat_type not in valid_repeat_types:
        return jsonify({
            'success': False,
            'message': f'Repeat_type phải là một trong các giá trị: {", ".join(valid_repeat_types)}'
        }), 400

    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Check if category exists and belongs to user
        cursor.execute('''
            SELECT CategoryID FROM Category 
            WHERE CategoryID = ? AND UserID = ?
        ''', (category_id, user_id))
        if not cursor.fetchone():
            return jsonify({
                'success': False,
                'message': 'Danh mục không tồn tại hoặc không thuộc về người dùng này'
            }), 404

        # Check if account exists and belongs to user
        cursor.execute('''
            SELECT AccountID FROM Account 
            WHERE AccountID = ? AND UserID = ?
        ''', (account_id, user_id))
        if not cursor.fetchone():
            return jsonify({
                'success': False,
                'message': 'Tài khoản không tồn tại hoặc không thuộc về người dùng này'
            }), 404

        # Check if budget already exists for this category and account
        cursor.execute('''
            SELECT BudgetID FROM Budget 
            WHERE CategoryID = ? AND AccountID = ? AND UserID = ? 
            AND (
                (? BETWEEN Start_date AND End_date)
                OR (? BETWEEN Start_date AND End_date)
                OR (Start_date BETWEEN ? AND ?)
                OR (End_date IS NULL AND Start_date <= ?)
            )
        ''', (category_id, account_id, user_id, start_date, end_date, 
              start_date, end_date, end_date))
        
        if cursor.fetchone():
            return jsonify({
                'success': False,
                'message': 'Đã tồn tại ngân sách cho danh mục và tài khoản này trong khoảng thời gian đã chọn'
            }), 400

        # Create new budget
        cursor.execute('''
            INSERT INTO Budget (
                UserID, CategoryID, AccountID, Budget_limit, 
                Repeat_type, Start_date, End_date
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
        ''', (user_id, category_id, account_id, budget_limit, 
              repeat_type, start_date, end_date))

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

@budget_bp.route('/api/budgets', methods=['GET'])
def get_budgets():
    """Lấy danh sách ngân sách của người dùng"""
    user_id = request.args.get('user_id')
    category_id = request.args.get('category_id')
    account_id = request.args.get('account_id')
    active_only = request.args.get('active_only', 'true').lower() == 'true'

    if not user_id:
        return jsonify({
            'success': False,
            'message': 'Thiếu user_id'
        }), 400

    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        query = '''
            SELECT 
                b.*,
                c.Category_name,
                a.Account_name,
                COALESCE(SUM(t.Amount), 0) as spent_amount
            FROM Budget b
            JOIN Category c ON b.CategoryID = c.CategoryID
            JOIN Account a ON b.AccountID = a.AccountID
            LEFT JOIN Transactions t ON 
                b.CategoryID = t.CategoryID AND
                b.AccountID = t.AccountID AND
                t.Transaction_date >= b.Start_date AND
                (t.Transaction_date <= b.End_date OR b.End_date IS NULL)
            WHERE b.UserID = ?
        '''
        params = [user_id]

        if category_id:
            query += ' AND b.CategoryID = ?'
            params.append(category_id)

        if account_id:
            query += ' AND b.AccountID = ?'
            params.append(account_id)

        if active_only:
            query += ''' 
                AND (
                    b.End_date IS NULL OR 
                    b.End_date >= date('now')
                )
            '''

        query += ' GROUP BY b.BudgetID'
        query += ' ORDER BY b.Created_at DESC'

        cursor.execute(query, params)
        rows = cursor.fetchall()
        
        budgets = []
        for row in rows:
            budget = dict(row)
            spent = float(budget['spent_amount'] or 0)
            limit = float(budget['Budget_limit'])
            budget['remaining_amount'] = limit - spent
            budget['progress_percentage'] = (spent / limit * 100) if limit > 0 else 0
            budget['is_exceeded'] = spent > limit
            budgets.append(budget)

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

@budget_bp.route('/api/budgets/<int:budget_id>', methods=['GET'])
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
                a.Account_name,
                COALESCE(SUM(t.Amount), 0) as spent_amount,
                COUNT(DISTINCT t.TransactionID) as transaction_count
            FROM Budget b
            JOIN Category c ON b.CategoryID = c.CategoryID
            JOIN Account a ON b.AccountID = a.AccountID
            LEFT JOIN Transactions t ON 
                b.CategoryID = t.CategoryID AND
                b.AccountID = t.AccountID AND
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
                t.AccountID = ? AND
                t.Transaction_date >= ? AND
                (t.Transaction_date <= ? OR ? IS NULL)
            ORDER BY t.Transaction_date DESC
            LIMIT 5
        ''', (budget['CategoryID'], budget['AccountID'], 
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
            SELECT UserID, CategoryID, AccountID 
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
    account_id = request.args.get('account_id')

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
                    b2.AccountID = t.AccountID AND
                    t.Transaction_date >= b2.Start_date AND
                    (t.Transaction_date <= b2.End_date OR b2.End_date IS NULL)
                WHERE b2.UserID = ?
                GROUP BY b2.BudgetID
            ) spent ON b.BudgetID = spent.BudgetID
            WHERE b.UserID = ? 
            AND (b.End_date IS NULL OR b.End_date >= date('now'))
        '''
        params = [user_id, user_id]

        if account_id:
            query = query.replace('WHERE b.UserID = ?', 'WHERE b.UserID = ? AND b.AccountID = ?')
            params = [user_id, user_id, account_id]

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
            JOIN Account a ON b.AccountID = a.AccountID
            LEFT JOIN Transactions t ON 
                b.CategoryID = t.CategoryID AND
                b.AccountID = t.AccountID AND
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
