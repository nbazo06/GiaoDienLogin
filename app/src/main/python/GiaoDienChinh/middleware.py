from functools import wraps
from flask import request, jsonify
import jwt
import time
import os
from datetime import datetime, timedelta

# Rate limiting configuration
RATE_LIMIT_WINDOW = 60  # seconds
MAX_REQUESTS = 100  # requests per window
rate_limit_store = {}  # Store for rate limiting {ip: [(timestamp, count)]}

def token_required(f):
    """Decorator to verify JWT token for protected routes"""
    @wraps(f)
    def decorated(*args, **kwargs):
        token = None
        
        # Get token from Authorization header
        if 'Authorization' in request.headers:
            auth_header = request.headers['Authorization']
            try:
                token = auth_header.split(" ")[1]  # Bearer <token>
            except IndexError:
                return jsonify({
                    'success': False,
                    'message': 'Invalid token format'
                }), 401

        if not token:
            return jsonify({
                'success': False,
                'message': 'Token is missing'
            }), 401

        try:
            # Verify token
            # Note: You should store your secret key in a secure configuration
            secret_key = os.getenv('JWT_SECRET_KEY', 'your-secret-key')
            data = jwt.decode(token, secret_key, algorithms=["HS256"])
            current_user_id = data['user_id']
            
            # You can add the user to the request context
            request.current_user_id = current_user_id
            
        except jwt.ExpiredSignatureError:
            return jsonify({
                'success': False,
                'message': 'Token has expired'
            }), 401
        except jwt.InvalidTokenError:
            return jsonify({
                'success': False,
                'message': 'Invalid token'
            }), 401

        return f(*args, **kwargs)
    return decorated

def rate_limit(f):
    """Decorator to implement rate limiting"""
    @wraps(f)
    def decorated(*args, **kwargs):
        ip = request.remote_addr
        current_time = time.time()
        
        # Clean up old entries
        if ip in rate_limit_store:
            rate_limit_store[ip] = [
                (ts, count) for ts, count in rate_limit_store[ip]
                if current_time - ts < RATE_LIMIT_WINDOW
            ]
        
        # Initialize if IP not in store
        if ip not in rate_limit_store:
            rate_limit_store[ip] = []
        
        # Calculate current request count
        current_window = rate_limit_store[ip]
        request_count = sum(count for _, count in current_window)
        
        if request_count >= MAX_REQUESTS:
            return jsonify({
                'success': False,
                'message': 'Rate limit exceeded. Please try again later.'
            }), 429
        
        # Add current request to store
        if current_window and current_window[-1][0] == current_time:
            current_window[-1] = (current_time, current_window[-1][1] + 1)
        else:
            current_window.append((current_time, 1))
        
        return f(*args, **kwargs)
    return decorated

def generate_token(user_id, expires_in=24):
    """Generate JWT token for user
    
    Args:
        user_id: User ID to encode in token
        expires_in: Token expiry time in hours (default 24)
    
    Returns:
        str: JWT token
    """
    try:
        secret_key = os.getenv('JWT_SECRET_KEY', 'your-secret-key')
        payload = {
            'user_id': user_id,
            'exp': datetime.utcnow() + timedelta(hours=expires_in),
            'iat': datetime.utcnow()
        }
        return jwt.encode(payload, secret_key, algorithm="HS256")
    except Exception as e:
        print(f"Error generating token: {e}")
        return None

def verify_token(token):
    """Verify JWT token
    
    Args:
        token: JWT token to verify
    
    Returns:
        dict: Token payload if valid
        None: If token is invalid
    """
    try:
        secret_key = os.getenv('JWT_SECRET_KEY', 'your-secret-key')
        return jwt.decode(token, secret_key, algorithms=["HS256"])
    except jwt.ExpiredSignatureError:
        print("Token has expired")
        return None
    except jwt.InvalidTokenError:
        print("Invalid token")
        return None
    except Exception as e:
        print(f"Error verifying token: {e}")
        return None
