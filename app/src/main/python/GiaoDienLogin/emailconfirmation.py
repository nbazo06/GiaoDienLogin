from flask import Blueprint, request, jsonify
import logging
import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from database import get_db_connection

email_confirmation_bp = Blueprint('email-confirmation', __name__, url_prefix='/api')

@email_confirmation_bp.route('/email-confirmation', methods=['POST'])
def confirm_otp():
    try:
        data = request.get_json()

        email = data.get('email', '').strip()
        otp = data.get('otp', '').strip()

        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('SELECT OTP FROM EmailVerification WHERE Email = ?', (email,))
        row = cursor.fetchone()

        if row['OTP'] == otp:
            
            # Xác nhận thành công => có thể xóa OTP hoặc đánh dấu đã xác nhận
            cursor.execute('DELETE FROM EmailVerification WHERE Email = ?', (email,))
            conn.commit()

            return jsonify({
                'success': True,
                'message': 'Xác nhận email thành công'
            }), 200
            
        return jsonify({
            'success': False,
            'message': 'Mã OTP không đúng'
        }), 400


    except Exception as e:
        logging.error(f"Error in confirm_otp: {str(e)}")
        return jsonify({
            'success': False,
            'message': 'Đã có lỗi xảy ra khi xác nhận OTP'
        }), 500

    finally:
        if 'conn' in locals():
            conn.close()
