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
import androidx.compose.ui.platform.LocalContext

@Preview
@Composable
fun ForgotPasswordScreen(navController: NavHostController? = null)
{
    val context = LocalContext.current
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

    Column (modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp))
    {
        Spacer(modifier = Modifier.height(180.dp))

        Text(
            text = "Quên mật khẩu",
            color = Color.Black,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Hiển thị thông báo thành công nếu có
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

        Text(
            text = "Vui lòng điền tài khoản bạn dùng để đăng nhập",
            color = Color.Black,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        var email by remember { mutableStateOf("") }
        OutlinedTextField(value = email,
            onValueChange = { email = it},
            label = { Text("Email",
                color = Color.Black) },
            modifier = Modifier.fillMaxWidth() )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                // Reset messages
                errorMessage = null
                successMessage = null

                // Kiểm tra trường bắt buộc
                if (email.isBlank()) {
                    errorMessage = "Vui lòng điều Email"
                    return@Button
                }

                CoroutineScope(Dispatchers.IO).launch {
                    AuthService.forgotPassword(email).fold(
                        onSuccess = { response ->
                            CoroutineScope(Dispatchers.Main).launch {
                                if (response.getBoolean("success")) {
                                    successMessage = "Đã gửi OTP"
                                    // Đợi 2 giây trước khi chuyển màn hình
                                    delay(2000)
                                    // Chuyển sang màn hình Email Confirmation
                                    navController?.navigate("email_confirmation_screen") {
                                        popUpTo("forgot_password_screen") { inclusive = true }
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
            Text("Tiếp theo", fontSize = 20.sp, color = Color.Black)
        }

    }
}