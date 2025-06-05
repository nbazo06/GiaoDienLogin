package com.Login1.GiaoDienLogin

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.Login1.GiaoDienLogin.service.AuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Preview
@Composable
fun NewPasswordScreen (navController: NavHostController, email: String)
{
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    Box(
        modifier = Modifier.fillMaxSize()
    )
    {
        Image(
            painter = painterResource(id = R.drawable.bg2_login),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }

    Column (modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp))
    {
        Spacer(modifier = Modifier.height(180.dp))

        Text(
            text = "Cài đặt mật khẩu mới",
            color = Color.Black,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(15.dp))

        successMessage?.let { success ->
            Text(
                text = success,
                color = Color.Green,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Hiển thị thông báo lỗi nếu có
        errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        var password by remember { mutableStateOf(  "") }
        var repassword by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Nhập mật khẩu mới của bạn", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible})
                {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = repassword,
            onValueChange = { repassword = it },
            label = { Text("Nhập lại mật khẩu mới của bạn", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible})
                {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Reset messages
                errorMessage = null
                successMessage = null

                // Kiểm tra trường bắt buộc
                if (password.isBlank() || repassword.isBlank()) {
                    val missingFields = mutableListOf<String>()
                    if (password.isBlank()) missingFields.add("Mật khẩu")
                    if (repassword.isBlank()) missingFields.add("Xác nhật mật khẩu")
                    errorMessage = "Vui lòng nhập ${missingFields.joinToString(", ")}"
                    return@Button
                }
                CoroutineScope(Dispatchers.IO).launch {
                    AuthService.newPassword(email, password, repassword).fold(
                        onSuccess = { response ->
                            CoroutineScope(Dispatchers.Main).launch {
                                if (response.getBoolean("success")) {
                                    successMessage = "Đổi mật khẩu thành công"
                                    delay(1000)
                                    navController.navigate("login_screen") {
                                        popUpTo("new_password_screen") { inclusive = true }
                                    }
                                } else {
                                    errorMessage = response.getString("message")
                                }
                            }
                        },
                        onFailure = { exception ->
                            CoroutineScope(Dispatchers.Main).launch {
                                errorMessage = exception.message ?: "Lỗi không xác định"
                            }
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(Color.White),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(6.dp)
        ) {
            Text("Xác nhận", fontSize = 20.sp, color = Color.Black)
        }

    }
}