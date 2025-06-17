package com.Login1.GiaoDienChinh

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

@Preview(showBackground = true)
@Composable
fun PersonalScreenPreview() {
    val navController = rememberNavController()
    personalScreen(navController = navController, userId = "123")
}

data class User(
    val fullName: String,
    val phoneNumber: String
) {
    // Lấy chữ cái đầu của họ và tên cuối (VD: "Nguyễn Đỗ Gia Bảo" → "NĐ")
    val initials: String
        get() {
            val parts = fullName.trim().split(" ")
            return when {
                parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}".uppercase()
                parts.isNotEmpty() -> parts.first().take(2).uppercase()
                else -> "??"
            }
        }
}

@Composable
fun PersonalCard(user: User) {
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
                    text = user.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = user.phoneNumber,
                    fontSize = 12.sp,
                    color = Color.Gray
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
                        .clickable { /* Xử lý đăng xuất */ }
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
                        imageVector = Icons.Default.ArrowForwardIos,
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
                        .clickable { /* Xử lý đổi tài khoản */ }
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
                        imageVector = Icons.Default.ArrowForwardIos,
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
                text = user.initials,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun personalScreen(navController: NavHostController, userId: String) {
    val currentUser = User(
        fullName = "Nguyễn Đỗ Gia Bảo",
        phoneNumber = "0911874264"
    )

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, userId) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFD9D9D9))
        ) {
            PersonalCard(user = currentUser)
        }
    }
}