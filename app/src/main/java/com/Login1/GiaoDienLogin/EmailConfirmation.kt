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
import com.Login1.service.AuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//@Preview
@Composable

fun EmailConfirmation(navController: NavHostController, email: String)
{
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg2_login),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }

    Column (modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp))
    {
        Spacer(modifier = Modifier.height(150.dp))

        Text(
            text = "Xác nhận Email",
            color = Color.Black,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

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
            text = "Đã gửi mã xác nhận đến Email của bạn:\n$email",  // <-- Hiển thị email tại đây
            color = Color.Black,
            fontSize = 18.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Vui lòng nhập mã xác nhận:" +
                    "",
            color = Color.Black,
            fontSize = 15.sp,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(5.dp))

        var otp by remember { mutableStateOf("") }

        OutlinedTextField(value = otp, onValueChange = { otp = it}, label = { Text("Mã xác nhận", color = Color.Black) }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Reset messages
                errorMessage = null
                successMessage = null

                // Kiểm tra trường bắt buộc
                if (otp.isBlank()) {
                    errorMessage = "Vui lòng nhập mã OTP"
                    return@Button
                }

                CoroutineScope(Dispatchers.IO).launch {
                    AuthService.emailConfirmation(email, otp).fold(
                        onSuccess = { response ->
                            CoroutineScope(Dispatchers.Main).launch {
                                if (response.getBoolean("success")) {
                                    successMessage = "Xác nhận OTP"
                                    delay(1000)
                                    navController.navigate("new_password_screen/${email}") {
                                        popUpTo("email_confirmation_screen") { inclusive = true }
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
