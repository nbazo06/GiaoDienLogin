# Ứng dụng Quản lý chi tiêu trên Android

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
pip install -r requirements.txt
```

### 3. Chạy backend server

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
