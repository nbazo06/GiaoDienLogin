# This file makes the directory a Python package
from .wallets import wallet_bp
from .category import category_bp
from .transactions import transactions_bp
from .budget import budget_bp

__all__ = [
    'wallet_bp',
    'category_bp',
    'transactions_bp',
    'budget_bp'
]