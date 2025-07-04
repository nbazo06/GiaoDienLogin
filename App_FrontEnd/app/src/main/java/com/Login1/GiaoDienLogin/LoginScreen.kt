package com.Login1.GiaoDienLogin

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg2_login),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(70.dp))

        Text(text = "Xin chào !", fontSize = 24.sp)
        Text(
            text = "CHÀO MỪNG TRỞ LẠI",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Đăng nhập",
            color = Color.Black,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))

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

        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                errorMessage = null
                successMessage = null
            },
            label = { Text("Email", color = Color.Black) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                errorMessage = null
                successMessage = null
            },
            label = { Text("Mật khẩu", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Đăng ký",
                modifier = Modifier.clickable { 
                    navController.navigate("register_screen") {
                        popUpTo("login_screen") { inclusive = true }
                    }
                },
                color = Color.Black,
                fontSize = 14.sp
            )

            Text(
                text = "Quên mật khẩu?",
                modifier = Modifier.clickable {
                    navController.navigate("forgot_password_screen") {
                        launchSingleTop = true
                    }
                },
                color = Color.Black,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Reset messages
                errorMessage = null
                successMessage = null

                // Kiểm tra các trường bắt buộc
                if (email.isBlank() || password.isBlank()) {
                    val missingFields = mutableListOf<String>()
                    if (email.isBlank()) missingFields.add("Email")
                    if (password.isBlank()) missingFields.add("Mật khẩu")
                    errorMessage = "Vui lòng nhập ${missingFields.joinToString(", ")}"
                    return@Button
                }
                
                CoroutineScope(Dispatchers.IO).launch {
                    AuthService.login(email, password).fold(
                        onSuccess = { response ->
                            CoroutineScope(Dispatchers.Main).launch {
                                if (response.getBoolean("success")) {
                                    successMessage = "Đăng nhập thành công"
                                    CoroutineScope(Dispatchers.IO).launch {
                                    AuthService.insertIcon(context)
                                }
                                    delay(1000)
                                    navController.navigate("home_screen/${response.getString("user_id")}") {
                                        popUpTo("login_screen") { inclusive = true }
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
            Text("Đăng nhập", fontSize = 20.sp, color = Color.Black)
        }
    }
}