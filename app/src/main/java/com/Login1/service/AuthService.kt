package com.Login1.service

import android.util.Log
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Category(
    val id: String,
    val title: String,
    val type: String,
    val icon: Int
)

private const val DATABASE_NAME = "login_database.db"
private const val DATABASE_VERSION = 1

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS CategoryIcon (
                Icon_name TEXT NOT NULL,
                Icon_path INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS CategoryIcon")
        onCreate(db)
    }
}

fun insertDrawableIconsIntoDatabase(context: Context) {
    val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    val hasInsertedIcons = sharedPreferences.getBoolean("HasInsertedIcons", false)

    if (hasInsertedIcons) {
        Log.d("DatabaseInsert", "Drawable icons have already been inserted. Skipping...")
        return
    }

    val dbHelper = DatabaseHelper(context)
    val db: SQLiteDatabase = dbHelper.writableDatabase

    try {
        val resources = context.resources
        val packageName = context.packageName

        // Sử dụng reflection để lấy danh sách tất cả resource ID trong R.drawable
        val drawableClass = Class.forName("$packageName.R\$drawable")
        val fields = drawableClass.fields

        for (field in fields) {
            val iconName = field.name // Tên của resource (ví dụ: "ramen")
            val iconPath = field.getInt(null) // Resource ID (ví dụ: 2131230845)

            // Insert vào database
            val values = ContentValues().apply {
                put("Icon_name", iconName)
                put("Icon_path", iconPath)
            }
            db.insert("CategoryIcon", null, values)

            Log.d("DatabaseInsert", "Inserted icon: $iconName with ID: $iconPath")
        }

        // Cập nhật cờ trong SharedPreferences
        sharedPreferences.edit().putBoolean("HasInsertedIcons", true).apply()
        Log.d("DatabaseInsert", "All drawable icons inserted successfully")
    } catch (e: Exception) {
        Log.e("DatabaseInsert", "Error inserting drawable icons: ${e.message}", e)
    } finally {
        db.close()
    }
}

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
            account_id: String,
            transaction_type: String,
            amount: String,
            CategoryID: String,
            transaction_date: String,
            money_source: String,
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
                    put("account_id", account_id)
                    put("transaction_type", transaction_type)
                    put("amount", amount)
                    put("CategoryID", CategoryID)
                    put("transaction_date", transaction_date)
                    put("money_source", money_source)
                    put("note", note)
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
                            id = categoryJson.getString("CategoryID"),
                            title = categoryJson.getString("Category_name"),
                            type = categoryJson.getString("Category_type"),
                            icon = categoryJson.getString("Category_icon").toInt()
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
    }
}