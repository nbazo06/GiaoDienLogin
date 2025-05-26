package com.Login1.GiaoDienLogin.service

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

        suspend fun forgotPassword(email: String): Result<JSONObject> {
            var connection: HttpURLConnection? = null
            return try {
                val url = URL("$BASE_URL/forgotPassword")
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
    }
}