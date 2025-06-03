import sqlite3
import os
import logging

# Đường dẫn tới database
DB_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'database')
DB_PATH = os.path.join(DB_DIR, 'login_database.db')

def get_db_connection():
    """Tạo và trả về kết nối đến database"""
    # Đảm bảo thư mục database tồn tại
    os.makedirs(DB_DIR, exist_ok=True)
    
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        print("Starting database initialization...")

        # User table
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS User (
                UserID INTEGER PRIMARY KEY AUTOINCREMENT,
                Username TEXT NOT NULL,
                Email TEXT UNIQUE NOT NULL,
                Password TEXT NOT NULL,
                Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')
        print("User table created or already exists.")

        # Account table
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS Account (
                AccountID INTEGER PRIMARY KEY AUTOINCREMENT,
                Account_type TEXT NOT NULL,
                Account_name TEXT NOT NULL,
                Balance REAL NOT NULL DEFAULT 0,
                UserID INTEGER NOT NULL,
                Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(UserID) REFERENCES User(UserID)
            )
        ''')
        print("Account table created or already exists.")        # Category table
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS Category (
                CategoryID INTEGER PRIMARY KEY AUTOINCREMENT,
                UserID INTEGER NOT NULL,
                Category_name TEXT NOT NULL,
                Category_type TEXT CHECK(Category_type IN ('income', 'expense')) DEFAULT 'expense',
                Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(UserID) REFERENCES User(UserID)
            )
        ''')
        print("Category table created or already exists.")

        # Transaction table
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS Transactions (
                TransactionID INTEGER PRIMARY KEY AUTOINCREMENT,
                AccountID INTEGER NOT NULL,
                Transaction_type TEXT NOT NULL,
                Amount REAL NOT NULL,
                CategoryID INTEGER NOT NULL,
                Transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Note TEXT,
                FOREIGN KEY(AccountID) REFERENCES Account(AccountID),
                FOREIGN KEY(CategoryID) REFERENCES Category(CategoryID)
            )
        ''')
        print("Transactions table created or already exists.")

        # Budget table
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS Budget (
                BudgetID INTEGER PRIMARY KEY AUTOINCREMENT,
                UserID INTEGER NOT NULL,
                CategoryID INTEGER NOT NULL,
                Budget_limit REAL NOT NULL,
                Repeat_type TEXT CHECK(Repeat_type IN ('none', 'daily', 'weekly', 'monthly', 'yearly')) DEFAULT 'none',
                Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Start_date TIMESTAMP,
                End_date TIMESTAMP,
                AccountID INTEGER NOT NULL,
                FOREIGN KEY(UserID) REFERENCES User(UserID),
                FOREIGN KEY(CategoryID) REFERENCES Category(CategoryID),
                FOREIGN KEY(AccountID) REFERENCES Account(AccountID)
            )
        ''')
        print("Budget table created or already exists.")

        # Notification table
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS Notification (
                NotificationID INTEGER PRIMARY KEY AUTOINCREMENT,
                AccountID INTEGER NOT NULL,
                Title TEXT NOT NULL,
                Message TEXT,
                Type TEXT,
                Is_read INTEGER DEFAULT 0,
                Sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UserID INTEGER NOT NULL,
                FOREIGN KEY(AccountID) REFERENCES Account(AccountID),
                FOREIGN KEY(UserID) REFERENCES User(UserID)
            )
        ''')
        print("Notification table created or already exists.")

        conn.commit()
        print("Database initialized successfully")
    except Exception as e:
        print(f"Error initializing database: {e}")
        raise
    finally:
        if 'conn' in locals():
            conn.close()


# Thay đoạn cuối của database.py thành:
if __name__ == "__main__":
    init_db()