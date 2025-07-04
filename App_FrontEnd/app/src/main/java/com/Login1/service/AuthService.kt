package com.Login1.service

import android.util.Log
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

import android.content.Context
import android.content.ContentValues
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Category(
    val id: Int,
    val title: String,
    val type: String,
    val icon: Int
)

data class Wallet(
    val id: Int,
    val name: String,
    val iconResid: Int
)

data class GiaoDich(
    val id: String,
    val soTien: Int,
    val tenLoai: String,
    val thuNhap: Boolean,
    val ngay: String,
    val nguonTien: String,
    val iconRes: Int
)

data class BudgetItem(
    val id: Int,
    val categoryId: Int,
    val categoryName: String,
    val iconPath: Int,
    val budgetLimit: String,
    val startDate: String,
    val endDate: String,
    val walletId: Int
)

class AuthService {
    companion object {
        public const val BASE_URL = "http://10.0.2.2:5000/api"

        private fun readResponse(connection: HttpURLConnection): JSONObject {
            val inputStream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            return inputStream.bufferedReader().use { reader ->
                JSONObject(reader.readText())
            }
        }

        suspend fun get(endpoint: String): JSONObject {
            var connection: HttpURLConnection? = null
            return try {
                val url = URL("$endpoint")
                Log.d("AuthService", "Connecting to URL: $url") // Thêm log để kiểm tra URL
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                readResponse(connection)
            } catch (e: Exception) {
                Log.e("AuthService", "Error connecting to server: ${e.message}", e) // Thêm log để kiểm tra lỗi
                JSONObject().apply {
                    put("success", false)
                    put("message", "Không thể kết nối đến server: ${e.message}")
                }
            } finally {
                connection?.disconnect()
            }
        }

        suspend fun insertIcon(
            context: Context,
            ): Result<Unit> {
            var connection: HttpURLConnection? = null
            return try {
                val url = URL("$BASE_URL/register-icon")
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                val resources = context.resources
                val packageName = context.packageName

                // Sử dụng reflection để lấy danh sách tất cả resource ID trong R.drawable
                val drawableClass = Class.forName("$packageName.R\$drawable")
                val fields = drawableClass.fields
                val iconsArray = org.json.JSONArray()

                for (field in fields) {
                    val iconName = field.name // Tên của resource (ví dụ: "ramen")
                    val iconPath = field.getInt(null) // Resource ID (ví dụ: 2131230845)

                    // Thêm vào mảng JSON
                    val iconObject = JSONObject().apply {
                        put("icon_name", iconName)
                        put("icon_path", iconPath)
                    }
                    iconsArray.put(iconObject)
                }

                // Tạo JSON input string
                val jsonInputString = JSONObject().apply {
                    put("icons", iconsArray)
                }.toString()

                OutputStreamWriter(connection.outputStream).use { it.write(jsonInputString) }

                val response = readResponse(connection)
                if (connection.responseCode in 200..299) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.getString("message")))
                }
            } catch (e: Exception) {
                Log.e("InsertIcon", "Error inserting category icons: ${e.message}", e)
                Result.failure(Exception("Không thể kết nối đến server: ${e.message}"))
            } finally {
                connection?.disconnect()
            }
        }

        suspend fun login(email: String, password: String): Result<JSONObject> {
            var connection: HttpURLConnection? = null
            return try {
                val url = URL("$BASE_URL/login")
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val jsonInputString = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }.toString()

                OutputStreamWriter(connection.outputStream).use { it.write(jsonInputString) }

                val response = readResponse(connection)
                if (connection.responseCode in 200..299) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.getString("message")))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Không thể kết nối đến server"))
            } finally {
                connection?.disconnect()
            }
        }

        suspend fun register(
            email: String,
            password: String,
            rePassword: String
            ): Result<JSONObject> {
            var connection: HttpURLConnection? = null
            return try {
                val url = URL("$BASE_URL/register")
                Log.d("AuthService", "Trying to connect to: $url") // Better Android logging
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val jsonInputString = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                    put("rePassword", rePassword)
                }.toString()
                Log.d("AuthService", "Sending data: $jsonInputString")

                OutputStreamWriter(connection.outputStream).use { it.write(jsonInputString) }

                Log.d("AuthService", "Response code: ${connection.responseCode}")
                val response = readResponse(connection)
                Log.d("AuthService", "Response: $response")

                if (connection.responseCode in 200..299) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.getString("message")))
                }
            } catch (e: Exception) {
                Log.e("AuthService", "Error: ${e.message}", e)
                e.printStackTrace()
                Result.failure(Exception("Không thể kết nối đến server: ${e.message}"))
            } finally {
                connection?.disconnect()
            }
        }

        suspend fun forgotPassword(email: String): Result<JSONObject> {
            var connection: HttpURLConnection? = null
            return try {
                val url = URL("$BASE_URL/forgot-password")
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val jsonInputString = JSONObject().apply {
                    put("email", email)
                }.toString()

                OutputStreamWriter(connection.outputStream).use { it.write(jsonInputString) }

                val response = readResponse(connection)
                if (connection.responseCode in 200..299) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.getString("message")))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Không thể kết nối đến server"))
            } finally {
                connection?.disconnect()
            }
        }
        suspend fun emailConfirmation(
            email: String,
            otp: String
            ): Result<JSONObject> {
            var connection: HttpURLConnection? = null
            return try {
                val url = URL("$BASE_URL/email-confirmation")
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val jsonInputString = JSONObject().apply {
                    put("email", email)
                    put("otp", otp)
                }.toString()

                OutputStreamWriter(connection.outputStream).use { it.write(jsonInputString) }

                val response = readResponse(connection)
                if (connection.responseCode in 200..299) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.getString("message")))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Không thể kết nối đến server"))
            } finally {
                connection?.disconnect()
            }
        }
        suspend fun newPassword(
            email: String,
            newpassword: String,
            renewpassword: String
            ): Result<JSONObject> {
            var connection: HttpURLConnection? = null
            return try {
                val url = URL("$BASE_URL/new-password")
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val jsonInputString = JSONObject().apply {
                    put("email", email)
                    put("newPassword", newpassword)
                    put("reNewPassword", renewpassword)
                }.toString()

                OutputStreamWriter(connection.outputStream).use { it.write(jsonInputString) }

                val response = readResponse(connection)
                if (connection.responseCode in 200..299) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.getString("message")))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Không thể kết nối đến server"))
            } finally {
                connection?.disconnect()
            }
        }

        suspend fun addTransaction(
            user_id: String,
            transaction_type: String,
            amount: String,
            CategoryID: Int,
            transaction_date: String,
            walletID: Int,
            note: String,

            ): Result<JSONObject> {
            var connection: HttpURLConnection? = null
            return try {
                val url = URL("$BASE_URL/transactions")
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val jsonInputString = JSONObject().apply {
                    put("UserID", user_id)
                    put("Transaction_type", transaction_type)
                    put("Amount", amount)
                    put("CategoryID", CategoryID)
                    put("Transaction_date", transaction_date)
                    put("WalletID", walletID)
                    put("Note", note)
                }.toString()

                OutputStreamWriter(connection.outputStream).use { it.write(jsonInputString) }

                val response = readResponse(connection)
                if (connection.responseCode in 200..299) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.getString("message")))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Không thể kết nối đến server"))
            } finally {
                connection?.disconnect()
            }
        }

        suspend fun getEmail(userId: String): Result<String> {
            Log.d("AuthService", "Calling /accounts API with userId: $userId")
            return try {
                val response = get("$BASE_URL/accounts?user_id=$userId")
                Log.d("AuthService", "Response: $response")

                return if (response.getBoolean("success")) {
                    val email = response.getString("email")
                    Result.success(email)
                } else {
                    Result.failure(Exception(response.getString("message")))
                }
            } catch (e: Exception) {
                Log.e("AuthService", "Lỗi khi lấy email: ${e.message}", e)
                Result.failure(Exception("Không thể lấy email: ${e.message}"))
            }
        }

        suspend fun getCategories(userId: String): Result<List<Category>> {
            Log.d("AuthService", "Calling /categories API with userId: $userId")
            return try {
                val response = get("$BASE_URL/categories?user_id=$userId")
                Log.d("AuthService", "Response: $response")
                if (response.getBoolean("success")) {
                    val categoriesJsonArray = response.getJSONArray("categories")
                    val categories = (0 until categoriesJsonArray.length()).map { i ->
                        val categoryJson = categoriesJsonArray.getJSONObject(i)
                        Category(
                            id = categoryJson.getInt("CategoryID"),
                            title = categoryJson.getString("Category_name"),
                            type = categoryJson.getString("Category_type"),
                            icon = categoryJson.getInt("IconID")
                        )
                    }
                    Result.success(categories)
                } else {
                    Result.failure(Exception(response.getString("message")))
                }
            } catch (e: Exception) {
                Log.e("AuthService", "Error: ${e.message}", e)
                Result.failure(Exception("Không thể lấy dữ liệu categories: ${e.message}"))
            }
        }

        suspend fun getWallets(userId: String): Result<List<Wallet>> {
            return try {
                val response = get("$BASE_URL/wallets?user_id=$userId")
                if (response.getBoolean("success")) {
                    val walletsJson = response.getJSONArray("wallets")
                    val wallets = (0 until walletsJson.length()).map { i ->
                        val walletJson = walletsJson.getJSONObject(i)
                        Wallet(
                            id = walletJson.getInt("WalletID"),
                            name = walletJson.getString("Name"),
                            iconResid = walletJson.getInt("Icon")
                        )
                    }
                    Result.success(wallets)
                } else {
                    Result.failure(Exception(response.getString("message")))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Không thể lấy danh sách ví: ${e.message}"))
            }
        }

        suspend fun getTransactions(userId: String): Result<Map<String, List<GiaoDich>>> {
            return try {
                val response = get("$BASE_URL/transactions?user_id=$userId")
                if (response.getBoolean("success")) {
                    val transactions = response.getJSONArray("transactions")
                    val groupedTransactions = (0 until transactions.length())
                        .map { i -> transactions.getJSONObject(i) }
                        .map { it.toGiaoDich() }
                        .groupBy { it.ngay }
                    Result.success(groupedTransactions)
                } else {
                    Result.failure(Exception(response.getString("message")))
                }
            } catch (e: Exception) {
                Log.e("TransactionHistory", "Error: ${e.message}", e)
                Result.failure(e)
            }
        }

        // Extension function để chuyển đổi JSONObject thành GiaoDich
        private fun JSONObject.toGiaoDich(): GiaoDich {
            val rawDate = getString("Transaction_date") // Lấy ngày từ JSON
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy") // Định dạng ngày tháng khớp với dữ liệu
            val parsedDate = LocalDate.parse(rawDate, formatter) // Chuyển đổi sang LocalDate

            return GiaoDich(
                id = getString("TransactionID"),
                soTien = getInt("Amount"),
                tenLoai = getString("Category_name"),
                iconRes = getInt("Icon_path"),
                ngay = parsedDate.format(formatter),
                thuNhap = getString("Transaction_type") == "income",
                nguonTien = getString("WalletID")
            )
        }
        
        suspend fun deleteTransaction(transactionId: String): Result<JSONObject> {
            var connection: HttpURLConnection? = null
            return try {
                val url = URL("$BASE_URL/transactions/$transactionId")
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "DELETE"
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                val response = readResponse(connection)
                if (connection.responseCode in 200..299) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.optString("message", "Xóa giao dịch thất bại")))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Không thể kết nối đến server: ${e.message}"))
            } finally {
                connection?.disconnect()
            }
        }

        suspend fun addBudget(
            userId: String,
            budgetLimit: String,
            categoryId: Int,
            startDate: String,
            endDate: String,
            walletId: Int
            ): Result<JSONObject> {
            var connection: HttpURLConnection? = null
            return try {
                val url = URL("$BASE_URL/budgets")
                connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 5000
                    readTimeout = 5000
                }

                val jsonInputString = JSONObject().apply {
                    put("UserID", userId)
                    put("Budget_limit", budgetLimit)
                    put("CategoryID", categoryId)
                    put("Start_date", startDate)
                    put("End_date", endDate)
                    put("WalletID", walletId)
                }.toString()

                OutputStreamWriter(connection.outputStream).use { it.write(jsonInputString) }

                val response = readResponse(connection)
                if (connection.responseCode in 200..299) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.getString("message")))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Không thể kết nối đến server: ${e.message}"))
            } finally {
                connection?.disconnect()
            }
        }

        suspend fun getBudgets(userId: String): Result<List<BudgetItem>> {
            return try {
                val response = get("$BASE_URL/budgets?user_id=$userId")
                if (response.getBoolean("success")) {
                    val budgetsJson = response.getJSONArray("budgets")
                    val budgets = (0 until budgetsJson.length()).map { i ->
                        val budgetJson = budgetsJson.getJSONObject(i)
                        BudgetItem(
                            id = budgetJson.getInt("BudgetID"),
                            categoryId = budgetJson.getInt("CategoryID"),
                            categoryName = budgetJson.getString("Category_name"),
                            iconPath = budgetJson.getInt("Icon_path"),
                            budgetLimit = budgetJson.getString("Budget_limit"),
                            startDate = budgetJson.getString("Start_date"),
                            endDate = budgetJson.getString("End_date"),
                            walletId = budgetJson.getInt("WalletID")
                        )
                    }
                    Result.success(budgets)
                } else {
                    Result.failure(Exception(response.getString("message")))
                }
            } catch (e: Exception) {
                Log.e("AuthService", "Error: ${e.message}", e)
                Result.failure(Exception("Không thể lấy dữ liệu ngân sách: ${e.message}"))
            }
        }
    }
}