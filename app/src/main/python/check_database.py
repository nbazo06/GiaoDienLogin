import sqlite3
from datetime import datetime
import os

def check_database():
    try:
        # Đường dẫn tới database
        DB_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), '../database/login_database.db')
        
        # Kiểm tra xem file database có tồn tại không
        if not os.path.exists(DB_PATH):
            print(f"Database không tồn tại tại đường dẫn: {DB_PATH}")
            return

        # Kết nối đến database
        conn = sqlite3.connect(DB_PATH)
        cursor = conn.cursor()
        
        # Kiểm tra các bảng trong database
        cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
        tables = cursor.fetchall()
        
        if not tables:
            print("Database không có bảng nào!")
            return
    
        print("\n=== DANH SÁCH BẢNG NGƯỜI DÙNG ===")
        for table in tables:
            if table[0] != 'sqlite_sequence':  # Bỏ qua bảng sqlite_sequence
                print(f"Bảng: {table[0]}")
                
                # Lấy thông tin cấu trúc của bảng
                cursor.execute(f"PRAGMA table_info({table[0]})")
                columns = cursor.fetchall()
                print("\nCấu trúc bảng:")
                for col in columns:
                    print(f"- {col[1]} ({col[2]})")
                
                # Đếm số bản ghi trong bảng
                cursor.execute(f"SELECT COUNT(*) FROM {table[0]}")
                count = cursor.fetchone()[0]
                print(f"\nSố bản ghi: {count}")
                
                # Hiển thị dữ liệu trong bảng
                if count > 0:
                    cursor.execute(f"SELECT * FROM {table[0]}")
                    rows = cursor.fetchall()
                    print("\nDữ liệu trong bảng:")
                    for row in rows:
                        created_at = datetime.fromisoformat(row[3].replace('Z', '+00:00'))
                        print(f"ID: {row[0]}")
                        print(f"Email: {row[1]}")
                        print(f"Password (đã mã hóa): {row[2]}")
                        print(f"Ngày tạo: {created_at.strftime('%d-%m-%Y %H:%M:%S')}")
                        print("-" * 50)
                else:
                    print("\nBảng chưa có dữ liệu!")
                
                print("\n" + "=" * 50)
        
        cursor.close()
        conn.close()
        
    except sqlite3.Error as e:
        print(f"Lỗi SQLite: {e}")
    except Exception as e:
        print(f"Lỗi: {e}")

if __name__ == "__main__":
    check_database() 