import sqlite3
import os

# Đường dẫn tương đối tới file database
DB_PATH = r"../database/login_database.db"

def print_db_info():
    print(f"[DEBUG] DB_PATH: {DB_PATH}")
    try:
        conn = sqlite3.connect(DB_PATH)
        cursor = conn.cursor()
        # Lấy danh sách bảng
        cursor.execute("SELECT name FROM sqlite_master WHERE type='table'")
        tables = cursor.fetchall()
        print("\nCác bảng trong database:")
        for table in tables:
            print(f"- {table[0]}")
            # In 3 dòng dữ liệu đầu tiên của mỗi bảng
            try:
                cursor.execute(f"SELECT * FROM {table[0]}")
                rows = cursor.fetchall()
                for row in rows:
                    print(f"  {row}")
            except Exception as e:
                print(f"  [ERROR] {e}")
        conn.close()
    except Exception as e:
        print(f"[ERROR] {e}")

if __name__ == "__main__":
    print_db_info()
