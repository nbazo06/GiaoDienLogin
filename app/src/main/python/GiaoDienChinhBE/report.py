from flask import Blueprint, request, jsonify
import sqlite3
import os
import matplotlib.pyplot as plt
import io
import base64
import datetime

# Đường dẫn tới database
DB_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'database')
DB_PATH = r"C:\Users\ADMIN\StudioProjects\GiaoDienLogin\app\src\main\database\login_database.db"

report_bp = Blueprint('report', __name__)

def get_db_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def get_week_range(year_week):
    """Trả về tuple (from_date, to_date) dạng dd/mm/yyyy cho tuần ISO (YYYY-WW)"""
    year, week = map(int, year_week.split('-'))
    first_day = datetime.date.fromisocalendar(year, week, 1)
    last_day = first_day + datetime.timedelta(days=6)
    return first_day.strftime('%d/%m/%Y'), last_day.strftime('%d/%m/%Y')

# API: Tổng hợp thu chi theo ngày/tuần/tháng/năm
@report_bp.route('/api/report/summary', methods=['GET'])
def report_summary():
    account_id = request.args.get('account_id')
    mode = request.args.get('mode', 'day')  # day, week, month, year
    value = request.args.get('value')
    category_id = request.args.get('category_id')

    if not account_id or not value:
        return jsonify({'success': False, 'message': 'Thiếu account_id hoặc value'}), 400

    date_filters = {
        'day': "t.Transaction_date = ?",
        'week': "strftime('%Y-%W', t.Transaction_date) = ?",
        'month': "strftime('%Y-%m', t.Transaction_date) = ?",
        'year': "strftime('%Y', t.Transaction_date) = ?"
    }

    if mode not in date_filters:
        return jsonify({'success': False, 'message': 'Giá trị mode không hợp lệ'}), 400

    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        params = [account_id, value]
        filter_clause = f"{date_filters[mode]}"

        if category_id:
            filter_clause += " AND t.CategoryID = ?"
            params.append(category_id)

        try:
            cursor.execute(f'''
                SELECT c.Category_name, t.Transaction_type, SUM(t.Amount) as Total
                FROM Transactions t
                JOIN Category c ON t.CategoryID = c.CategoryID
                WHERE t.AccountID = ? AND {filter_clause}
                GROUP BY t.CategoryID, t.Transaction_type
            ''', params)
            summary = [
                {
                    'category': row['Category_name'],
                    'type': row['Transaction_type'],
                    'total_amount': row['Total']
                } for row in cursor.fetchall()
            ]
        finally:
            conn.close()
        result = {'success': True, 'mode': mode, 'value': value, 'summary': summary}
        if mode == 'week':
            from_date, to_date = get_week_range(value)
            result['week_range'] = {'from': from_date, 'to': to_date}
        if not summary:
            result['message'] = 'No data found.'
        return jsonify(result), 200
    except Exception as e:
        # Có thể log lỗi chi tiết hơn ở đây nếu cần
        return jsonify({'success': False, 'message': str(e)}), 500

# API: Vẽ biểu đồ thu chi
@report_bp.route('/api/report/chart', methods=['GET'])
def report_chart():
    account_id = request.args.get('account_id')
    mode = request.args.get('mode', 'month')  # day, week, month, year
    value = request.args.get('value')
    chart_type = request.args.get('chart_type', 'bar')  # bar, pie

    if not account_id or not value:
        return jsonify({'success': False, 'message': 'Thiếu account_id hoặc value'}), 400

    groupings = {
        'week': ('t.Transaction_date', "strftime('%Y-%W', t.Transaction_date) = ?"),
        'month': ('t.Transaction_date', "strftime('%Y-%m', t.Transaction_date) = ?"),
        'year': ("strftime('%Y-%m', t.Transaction_date)", "strftime('%Y', t.Transaction_date) = ?")
    }

    if mode not in groupings:
        return jsonify({'success': False, 'message': 'Chỉ hỗ trợ week/month/year'}), 400

    group_by, date_filter = groupings[mode]
    params = [account_id, value]

    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        try:
            cursor.execute(f'''
                SELECT {group_by} as label,
                       SUM(CASE WHEN t.Transaction_type = 'spend' THEN t.Amount ELSE 0 END) as TotalSpend,
                       SUM(CASE WHEN t.Transaction_type = 'earn' THEN t.Amount ELSE 0 END) as TotalEarn
                FROM Transactions t
                WHERE t.AccountID = ? AND {date_filter}
                GROUP BY label
                ORDER BY label
            ''', params)
            rows = cursor.fetchall()
        finally:
            conn.close()

        labels = [row['label'] for row in rows]
        spends = [row['TotalSpend'] for row in rows]
        earns = [row['TotalEarn'] for row in rows]

        if not labels:
            result = {'success': True, 'chart': None, 'message': 'No data found.'}
            if mode == 'week':
                from_date, to_date = get_week_range(value)
                result['week_range'] = {'from': from_date, 'to': to_date}
            return jsonify(result), 200

        # Vẽ biểu đồ
        plt.figure(figsize=(8, 4))
        if chart_type == 'bar':
            x = range(len(labels))
            plt.bar(x, spends, width=0.4, label='Chi tiêu', color='red', align='center')
            plt.bar(x, earns, width=0.4, label='Thu nhập', color='green', align='edge')
            plt.xticks(x, labels, rotation=45)
            plt.xlabel('Thời gian')
            plt.ylabel('Số tiền')
            plt.legend()
        elif chart_type == 'pie':
            plt.pie(spends, labels=labels, autopct='%1.1f%%')
            plt.title('Tỉ lệ chi tiêu')
        else:
            return jsonify({'success': False, 'message': 'Giá trị chart_type không hợp lệ'}), 400

        plt.tight_layout()
        buf = io.BytesIO()
        plt.savefig(buf, format='png')
        buf.seek(0)
        img_base64 = base64.b64encode(buf.read()).decode('utf-8')
        plt.close()

        result = {'success': True, 'chart': img_base64}
        if mode == 'week':
            from_date, to_date = get_week_range(value)
            result['week_range'] = {'from': from_date, 'to': to_date}
        return jsonify(result), 200
    except Exception as e:
        # Có thể log lỗi chi tiết hơn ở đây nếu cần
        return jsonify({'success': False, 'message': str(e)}), 500

