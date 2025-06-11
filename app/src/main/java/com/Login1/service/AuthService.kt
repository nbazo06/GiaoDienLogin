package com.Login1.service

import android.util.Log
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AuthService {
    companion object {
        private const val BASE_URL = "http://10.0.2.2:5000/api"

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
            category_id: String,
            transaction_date: String,
            money_soure: String,
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
                    put("category_id", category_id)
                    put("transaction_date", transaction_date)
                    put("money_soure", money_soure)
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
    }
}