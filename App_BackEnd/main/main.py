from flask import Flask
from flask_cors import CORS
import logging
import sys

# Cấu hình logging chi tiết hơn
logging.basicConfig(
    level=logging.DEBUG,  # Set to DEBUG for more detailed logs
    format='%(asctime)s - %(levelname)s - %(message)s',
    stream=sys.stdout  # Log to stdout for immediate feedback
)

from GiaoDienLogin.register import register_bp
from GiaoDienLogin.login import login_bp
from GiaoDienLogin.forgotpassword import forgot_password_bp
from GiaoDienLogin.emailconfirmation import email_confirmation_bp
from GiaoDienLogin.newpassword import new_password_bp

from GiaoDienChinh.notifications import notifications_bp
from GiaoDienChinh.transactions import transactions_bp
from GiaoDienChinh.category import category_bp
from GiaoDienChinh.wallets import wallet_bp
from GiaoDienChinh.budget import budget_bp

from database import init_db, get_db_connection

app = Flask(__name__)
CORS(app, resources={r"/api/*": {"origins": "*"}})

# Đăng ký các blueprint của GiaoDienLogin
app.register_blueprint(register_bp)
app.register_blueprint(login_bp)
app.register_blueprint(forgot_password_bp)
app.register_blueprint(email_confirmation_bp)
app.register_blueprint(new_password_bp)

# Đăng ký các blueprint của GiaoDienChinh
app.register_blueprint(transactions_bp)
app.register_blueprint(category_bp)
app.register_blueprint(wallet_bp)
app.register_blueprint(notifications_bp)
app.register_blueprint(budget_bp)

@app.before_request
def before_request():
    if not get_db_connection():
        init_db()

@app.route('/')
def hello():
    return 'Server is running!'

if __name__ == '__main__':
    try:
        init_db()  # Khởi tạo database nếu chưa có
        app.run(host='0.0.0.0', port=5000, debug=True)
    except Exception as e:
        logging.error(f"Error starting server: {str(e)}")
        raise