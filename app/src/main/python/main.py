from flask import Flask
from register import register_bp
from login import login_bp
from forgotpassword import forgot_password_bp
import logging
from database import init_db, get_db_connection

# Cấu hình logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)

app = Flask(__name__)

# Đăng ký các blueprint
app.register_blueprint(register_bp)
app.register_blueprint(login_bp)
app.register_blueprint(forgot_password_bp)

@app.before_request
def before_request():
    if not get_db_connection():
        init_db()

@app.route('/')
def hello():
    return 'Server is running!'

if __name__ == '__main__':
    # Khởi tạo database khi khởi động server
    init_db()
    logging.info("Starting Flask server on http://127.0.0.1:5000")
    app.run(host='127.0.0.1', port=5000, debug=True)