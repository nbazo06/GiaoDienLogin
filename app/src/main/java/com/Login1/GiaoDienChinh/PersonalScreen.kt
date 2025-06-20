package com.Login1.GiaoDienChinh

import android.util.Log
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.*
import com.Login1.service.AuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.Login1.service.DatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun PersonalScreenPreview() {
    val navController = rememberNavController()
    PersonalScreen(navController = navController, userId = "1")
}

data class Email(
    val fullEmail: String
) {
    val initials: String
        get() {
            val parts = fullEmail.trim().split(" ")
            return when {
                //parts.size >= 2 -> "${parts.first().first()}".uppercase()
                parts.isNotEmpty() -> parts.first().take(2).uppercase()
                else -> "??"
            }
        }
}

@Composable
fun PersonalCard(navController: NavHostController, email: Email) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = email.fullEmail,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = Color(0xFFE0E0E0)
                )

                // Đăng xuất
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("login_screen") {
                                popUpTo("personal_screen") { inclusive = true }
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Đăng xuất",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đăng xuất",
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Go",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Đường kẻ ngăn cách
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = Color(0xFFE0E0E0)
                )

                // Đổi tài khoản
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("login_screen") {
                                popUpTo("personal_screen") { inclusive = true }
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Đổi tài khoản",
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đổi tài khoản",
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Go",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Avatar tròn chứa chữ viết tắt
        Box(
            modifier = Modifier
                .offset(y = (-20).dp)
                .size(64.dp)
                .background(Color(0xFFFFD6FF), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = email.initials,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun PersonalScreen(navController: NavHostController, userId: String) {
    var email by remember { mutableStateOf("Đang tải...") }

    LaunchedEffect(userId) {
        CoroutineScope(Dispatchers.IO).launch {
            AuthService.getEmail(userId).fold(
                onSuccess = { fetchedemail ->
                    withContext(Dispatchers.Main) {
                        email = fetchedemail
                    }
                },
                onFailure = { exception ->
                    withContext(Dispatchers.Main) {
                        Log.e("PersonalScreen", "Lỗi: ${exception.message}")
                    }
                }
            )
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, userId) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFD9D9D9))
        ) {
            PersonalCard(navController, Email(email))
        }
    }
}