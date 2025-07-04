import sqlite3
import os
import logging

# Đường dẫn tới database
DB_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'database')
DB_PATH = os.path.join(DB_DIR, 'main_database.db')

def get_db_connection(db_path=DB_PATH):
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
            CREATE TABLE IF NOT EXISTS Users (
                UserID INTEGER PRIMARY KEY AUTOINCREMENT,
                Email TEXT UNIQUE NOT NULL,
                Password TEXT NOT NULL,
                Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')

        # Wallets table
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS Wallet (
                WalletID INTEGER PRIMARY KEY AUTOINCREMENT,
                UserID INTEGER NOT NULL,
                Name TEXT NOT NULL,
                Balance REAL NOT NULL DEFAULT 0,
                Type TEXT NOT NULL,
                Icon INTEGER NOT NULL,
                Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(UserID) REFERENCES User(UserID)
            )
        ''')
        
        # EmailVerification table
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS EmailVerification (
                Email TEXT PRIMARY KEY,
                OTP TEXT NOT NULL,
                Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')
        
        # Category table
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS Category (
                CategoryID INTEGER PRIMARY KEY AUTOINCREMENT,
                UserID INTEGER NOT NULL,
                Category_name TEXT NOT NULL,
                Category_type TEXT NOT NULL,
                IconID INTGER NOT NULL,
                Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(UserID) REFERENCES User(UserID),
                FoREIGN KEY(IconID) REFERENCES Icon(IconID)
            )
        ''')
        
        # Icon table
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS Icon (
                IconID INTEGER PRIMARY KEY AUTOINCREMENT,
                Icon_name TEXT NOT NULL,
                Icon_path INTEGER NOT NULL
            )
        ''')

        # Transactions table
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS Transactions (
                TransactionID INTEGER PRIMARY KEY AUTOINCREMENT,
                UserID INTEGER NOT NULL,
                Transaction_type TEXT NOT NULL,
                Amount REAL NOT NULL,
                CategoryID INTEGER NOT NULL,
                Transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                WalletID TEXT NOT NULL,
                Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Note TEXT,
                FOREIGN KEY(WalletID) REFERENCES Wallets(WalletID),
                FOREIGN KEY(CategoryID) REFERENCES Category(CategoryID)
            )
        ''')

        # Budget table
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS Budget (
                BudgetID INTEGER PRIMARY KEY AUTOINCREMENT,
                UserID INTEGER NOT NULL,
                CategoryID INTEGER NOT NULL,
                Budget_limit REAL NOT NULL,
                Start_date TIMESTAMP NOT NULL,
                End_date TIMESTAMP NOT NULL,
                WalletID INTEGER NOT NULL,
                Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                Updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(UserID) REFERENCES User(UserID),
                FOREIGN KEY(CategoryID) REFERENCES Category(CategoryID),
                FOREIGN KEY(WalletID) REFERENCES Wallets(WalletID)
            )
        ''')

        # Notification table
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS Notification (
                NotificationID INTEGER PRIMARY KEY AUTOINCREMENT,
                WalletID INTEGER NOT NULL,
                Title TEXT NOT NULL,
                Message TEXT,
                Type TEXT,
                Is_read INTEGER DEFAULT 0,
                Sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UserID INTEGER NOT NULL,
                FOREIGN KEY(WalletID) REFERENCES Wallets(WalletID),
                FOREIGN KEY(UserID) REFERENCES User(UserID)
            )
        ''')

        conn.commit()
        print("Database initialized successfully")
    except Exception as e:
        print(f"Error initializing database: {e}")
        raise
    finally:
        if 'conn' in locals():
            conn.close()

if __name__ == "__main__":
    init_db()