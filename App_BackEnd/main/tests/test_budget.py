import unittest
import json
import sys
import os
from datetime import datetime, timedelta
import uuid
import sqlite3
import threading
import tempfile
import atexit

# Thêm đường dẫn để import module từ thư mục cha
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from flask import g
from GiaoDienChinh import create_app
from database import init_db
from register import register_bp
from login import login_bp


class TestDB:
    _db_path = None
    _lock = threading.Lock()

    @classmethod
    def get_instance(cls):
        """Lấy connection database test"""
        if cls._db_path is None:
            with cls._lock:
                if cls._db_path is None:
                    db_file = tempfile.NamedTemporaryFile(delete=False, suffix='.db')
                    db_file.close()
                    cls._db_path = db_file.name
                    atexit.register(cls._cleanup)
                    conn = sqlite3.connect(cls._db_path)
                    conn.row_factory = sqlite3.Row
                    conn.execute('PRAGMA foreign_keys=ON;')
                    conn.commit()
                    cls.init_db(conn)
                    conn.close()

        conn = sqlite3.connect(cls._db_path, check_same_thread=False)
        conn.row_factory = sqlite3.Row
        conn.execute('PRAGMA foreign_keys=ON;')
        return conn

    @classmethod
    def _cleanup(cls):
        if cls._db_path and os.path.exists(cls._db_path):
            try:
                os.unlink(cls._db_path)
            except:
                pass

    @classmethod
    def init_db(cls, db):
        cursor = db.cursor()
        try:
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
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS Account (
                    AccountID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Account_type TEXT NOT NULL,
                    Account_name TEXT NOT NULL,
                    Balance REAL NOT NULL DEFAULT 0,
                    UserID INTEGER NOT NULL,
                    Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    Updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(UserID) REFERENCES User(UserID) ON DELETE CASCADE
                )
            ''')
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS Category (
                    CategoryID INTEGER PRIMARY KEY AUTOINCREMENT,
                    UserID INTEGER NOT NULL,
                    Category_name TEXT NOT NULL,
                    Category_type TEXT CHECK(Category_type IN ('income', 'expense')) DEFAULT 'expense',
                    Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    Updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(UserID) REFERENCES User(UserID) ON DELETE CASCADE
                )
            ''')
            cursor.execute('''
                CREATE TABLE IF NOT EXISTS Budget (
                    BudgetID INTEGER PRIMARY KEY AUTOINCREMENT,
                    UserID INTEGER NOT NULL,
                    CategoryID INTEGER NOT NULL,
                    Budget_limit REAL NOT NULL,
                    Repeat_type TEXT CHECK(Repeat_type IN ('none', 'daily', 'weekly', 'monthly', 'yearly')) DEFAULT 'none',
                    Created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    Start_date TIMESTAMP,
                    End_date TIMESTAMP,
                    AccountID INTEGER NOT NULL,
                    FOREIGN KEY(UserID) REFERENCES User(UserID) ON DELETE CASCADE,
                    FOREIGN KEY(CategoryID) REFERENCES Category(CategoryID) ON DELETE CASCADE,
                    FOREIGN KEY(AccountID) REFERENCES Account(AccountID) ON DELETE CASCADE
                )
            ''')

            db.commit()
        finally:
            cursor.close()

    @classmethod
    def reset_db(cls):
        conn = cls.get_instance()
        cursor = conn.cursor()
        try:
            cursor.execute('PRAGMA foreign_keys=OFF;')
            cursor.execute('DELETE FROM Budget;')
            cursor.execute('DELETE FROM Category;')
            cursor.execute('DELETE FROM Account;')
            cursor.execute('DELETE FROM User;')
            cursor.execute('PRAGMA foreign_keys=ON;')
            conn.commit()
        finally:
            cursor.close()

    @classmethod
    def execute_insert(cls, query, params):
        conn = cls.get_instance()
        cursor = conn.cursor()
        try:
            cursor.execute(query, params)
            last_id = cursor.lastrowid
            conn.commit()
            return last_id
        finally:
            cursor.close()


def get_test_db_connection():
    return TestDB.get_instance()


class TestBudgetAPI(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        print("Setting up test class...")
        cls.test_db = TestDB.get_instance()

        cls.app = create_app({
            'TESTING': True,
            'DATABASE': TestDB._db_path,
            'WTF_CSRF_ENABLED': False
        })

        cls.app.register_blueprint(register_bp)
        cls.app.register_blueprint(login_bp)

        cls.app_context = cls.app.app_context()
        cls.app_context.push()

        # Patch get_db_connection trong các module
        import database
        database.get_db_connection = get_test_db_connection

        for module_name in ['basic', 'category', 'budget', 'account', 'transactions', 'report']:
            try:
                module = __import__(f'GiaoDienChinhBE.{module_name}', fromlist=[module_name])
                if hasattr(module, 'get_db_connection'):
                    setattr(module, 'get_db_connection', get_test_db_connection)
            except ImportError:
                print(f"Warning: Could not import {module_name}")

    @classmethod
    def tearDownClass(cls):
        print("Tearing down test class...")
        if hasattr(cls, 'app_context'):
            cls.app_context.pop()
        TestDB._cleanup()

    def setUp(self):
        print(f"\nSetting up test: {self._testMethodName}")
        TestDB.reset_db()
        self.client = self.app.test_client()
        self.test_user = self._create_test_user()
        self.test_category = self._create_test_category()

    def tearDown(self):
        print(f"Tearing down test: {self._testMethodName}")

    def _create_test_user(self):
        unique_id = str(uuid.uuid4())[:8]
        email = f"testuser_{unique_id}@example.com"
        username = f"testuser_{unique_id}"
        
        # First, insert directly into User table
        hashed_password = '$2b$12$LQVKgS1wSYYw1/QEJ4Hp1O1R.twijENqZqvZ5kkx.f8GrDQQRgU2i'  # Hash for 'Password123!'
        user_id = TestDB.execute_insert('''
            INSERT INTO User (Username, Email, Password)
            VALUES (?, ?, ?)
        ''', (username, email, hashed_password))

        # Then create the default account
        account_id = TestDB.execute_insert('''
            INSERT INTO Account (Account_type, Account_name, UserID, Balance)
            VALUES (?, ?, ?, ?)
        ''', ('default', 'Default Account', user_id, 1000.0))

        self.test_account_id = account_id
        return user_id

    def _create_test_category(self):
        category_name = f'Test Category {str(uuid.uuid4())[:8]}'
        category_data = {
            'user_id': self.test_user,
            'category_name': category_name,
            'category_type': 'expense'
        }

        response = self.client.post('/api/categories',
                                    json=category_data,
                                    content_type='application/json')

        data = json.loads(response.get_data(as_text=True))
        self.assertEqual(response.status_code, 201)
        self.assertTrue(data.get('success', False))

        return data['category_id']

    def test_create_budget(self):
        start_date = datetime.now().isoformat()
        end_date = (datetime.now() + timedelta(days=30)).isoformat()

        budget_data = {
            'user_id': self.test_user,
            'category_id': self.test_category,
            'budget_limit': 1000.0,
            'repeat_type': 'monthly',
            'start_date': start_date,
            'end_date': end_date,
            'account_id': self.test_account_id
        }

        response = self.client.post('/api/budgets',
                                    json=budget_data,
                                    content_type='application/json')

        data = json.loads(response.get_data(as_text=True))
        self.assertEqual(response.status_code, 201, f"Budget creation failed: {data}")
        self.assertTrue(data.get('success', False))
        self.assertIn('budget_id', data)


if __name__ == '__main__':
    unittest.main()
