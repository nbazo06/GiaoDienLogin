# Ứng dụng Android Login/Register với Python Backend

Đây là ứng dụng Android sử dụng Jetpack Compose cho giao diện người dùng và Python Flask cho backend.

## Yêu cầu hệ thống

- Python 3.8 trở lên
- Android Studio Hedgehog (2023.1.1) trở lên
- JDK 11 trở lên

## Cài đặt và Chạy Backend

### 1. Tạo môi trường ảo Python

```bash
# Windows
python -m venv venv
venv\Scripts\activate

# Linux/macOS
python3 -m venv venv
source venv/bin/activate
```

### 2. Cài đặt các thư viện cần thiết

```bash
pip install flask bcrypt python-dotenv
```

### 3. Cấu trúc thư mục backend

```
app/src/main/
├── python/
│   ├── database.py    # Module xử lý database
│   ├── login.py       # API đăng nhập
│   └── register.py    # API đăng ký
└── database/
    └── login_database.db  # SQLite database
```

### 4. Chạy backend server

```bash
# Di chuyển vào thư mục chứa code Python
cd app/src/main/python

# Chạy Flask server
python app.py
```

Server sẽ chạy tại địa chỉ `http://localhost:5000`

## Chạy ứng dụng Android

1. Mở project trong Android Studio
2. Đảm bảo backend server đang chạy
3. Chạy ứng dụng trên máy ảo Android hoặc thiết bị thật

### Lưu ý khi chạy trên thiết bị thật

Nếu bạn chạy ứng dụng trên thiết bị thật, cần thay đổi địa chỉ IP trong `AuthService.kt`:

```kotlin
private const val BASE_URL = "http://10.0.2.2:5000/api"  // Địa chỉ mặc định cho máy ảo
```

thành địa chỉ IP của máy tính của bạn:

```kotlin
private const val BASE_URL = "http://YOUR_COMPUTER_IP:5000/api"
```

## API Endpoints

### Đăng ký
- URL: `/api/register`
- Method: `POST`
- Body:
```json
{
    "email": "example@email.com",
    "password": "password123",
    "rePassword": "password123"
}
```

### Đăng nhập
- URL: `/api/login`
- Method: `POST`
- Body:
```json
{
    "email": "example@email.com",
    "password": "password123"
}
```

## Xử lý lỗi

### Backend
- Kiểm tra định dạng email
- Kiểm tra độ mạnh của mật khẩu
- Kiểm tra mật khẩu xác nhận
- Kiểm tra email đã tồn tại

### Frontend
- Hiển thị thông báo lỗi màu đỏ
- Hiển thị thông báo thành công màu xanh
- Xóa thông báo khi người dùng thay đổi input

## Phát triển

### Thêm tính năng mới
1. Tạo API endpoint mới trong thư mục `python/`
2. Thêm route trong `app.py`
3. Thêm service trong `AuthService.kt`
4. Thêm UI trong các file Compose

### Cập nhật database
1. Thêm bảng/cột mới trong `database.py`
2. Chạy lại server để cập nhật schema

## Vấn đề thường gặp

1. Lỗi kết nối backend
   - Kiểm tra server Flask đang chạy
   - Kiểm tra địa chỉ IP trong `AuthService.kt`
   - Kiểm tra port 5000 không bị chặn

2. Lỗi database
   - Xóa file `login_database.db` để tạo mới
   - Kiểm tra quyền ghi trong thư mục `database/`

3. Lỗi Android Studio
   - Clean và Rebuild project
   - Kiểm tra phiên bản JDK
   - Cập nhật Gradle 