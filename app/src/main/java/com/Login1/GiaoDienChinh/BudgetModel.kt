package com.Login1.GiaoDienChinh

import com.Login1.GiaoDienLogin.R

data class BudgetItem(
    val category: String,
    val iconResId: Int, // Gán icon thủ công dựa vào category
    val spent: Float,
    val total: Float,
    val startDate: String,
    val endDate: String,
    val transactionType: String,
    val source: String,
    val note: String
)

suspend fun fetchBudgets(accountId: String): List<BudgetItem> {
    // Gọi API thực tế từ backend ở đây, ví dụ AuthService.getBudgets(accountId)
    // Đây chỉ là demo giả lập
    return listOf(
        BudgetItem("Ăn uống", R.drawable.ramen, 250_000f, 500_000f, "10/06/2025", "22/06/2025", "Expense", "Tiền mặt", ""),
        BudgetItem("Giải trí", R.drawable.multimedia, 400_000f, 800_000f, "10/06/2025", "22/06/2025", "Expense", "Ngân hàng", "")
    )
}
