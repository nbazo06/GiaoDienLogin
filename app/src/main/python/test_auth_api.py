import requests
import json

BASE_URL = 'http://127.0.0.1:5000/api'

def print_response(r):
    print(f"Status: {r.status_code}")
    try:
        print(json.dumps(r.json(), ensure_ascii=False, indent=2))
    except Exception:
        print(r.text)

# 1. Test đăng ký tài khoản mới
print('--- Đăng ký tài khoản mới ---')
register_data = {
    "email": "testuser1@example.com",
    "password": "abc123",
    "rePassword": "abc123"
}
r = requests.post(f"{BASE_URL}/register", json=register_data)
print_response(r)

# 2. Test đăng nhập với tài khoản vừa đăng ký
print('\n--- Đăng nhập với tài khoản vừa đăng ký ---')
login_data = {
    "email": "testuser1@example.com",
    "password": "abc123"
}
r = requests.post(f"{BASE_URL}/login", json=login_data)
print_response(r)

# 3. Test đăng ký với email đã tồn tại
print('\n--- Đăng ký lại với email đã tồn tại ---')
r = requests.post(f"{BASE_URL}/register", json=register_data)
print_response(r)

# 4. Test đăng nhập sai mật khẩu
print('\n--- Đăng nhập sai mật khẩu ---')
login_data_wrong = {
    "email": "testuser1@example.com",
    "password": "sai123"
}
r = requests.post(f"{BASE_URL}/login", json=login_data_wrong)
print_response(r)