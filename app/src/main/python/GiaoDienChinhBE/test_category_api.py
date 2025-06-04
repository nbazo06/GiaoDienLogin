import requests
import json

BASE_URL = 'http://127.0.0.1:5000/api/categories'  # hoặc /api/accounts, /api/transactions, ...
USER_ID = 1  # Thay đổi nếu cần

def print_response(r):
    print(f"Status: {r.status_code}")
    try:
        print(json.dumps(r.json(), ensure_ascii=False, indent=2))
    except Exception:
        print(r.text)

# 1. Tạo mới category (KHÔNG truyền type)
print('--- Tạo mới category ---')
r = requests.post(BASE_URL, json={
    'category_name': 'Ăn uống',
    'user_id': USER_ID
})
print_response(r)
try:
    category_id = r.json().get('category_id')
except Exception:
    category_id = None

# 2. Lấy danh sách category
print('\n--- Lấy danh sách category ---')
r = requests.get(BASE_URL, params={'user_id': USER_ID})
print_response(r)

# 3. Xem chi tiết category vừa tạo
if category_id:
    print(f'\n--- Xem chi tiết category id={category_id} ---')
    r = requests.get(f'{BASE_URL}/{category_id}')
    print_response(r)

    # 4. Cập nhật category (KHÔNG truyền type)
    print(f'\n--- Cập nhật category id={category_id} ---')
    r = requests.put(f'{BASE_URL}/{category_id}', json={
        'category_name': 'Ăn uống ngoài'
    })
    print_response(r)

    # 5. Xóa category
    print(f'\n--- Xóa category id={category_id} ---')
    r = requests.delete(f'{BASE_URL}/{category_id}')
    print_response(r)
else:
    print('Không tạo được category mới, bỏ qua các bước tiếp theo.')
