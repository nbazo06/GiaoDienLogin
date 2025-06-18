                            package com.Login1

                            import androidx.compose.material3.*
                            import androidx.compose.runtime.Composable
                            import androidx.navigation.NavHostController
                            import com.Login1.GiaoDienChinh.BottomNavigationBar
                            import androidx.compose.foundation.background
                            import androidx.compose.foundation.layout.*
                            import androidx.compose.foundation.lazy.LazyColumn
                            import androidx.compose.foundation.lazy.itemsIndexed
                            import androidx.compose.foundation.shape.RoundedCornerShape
                            import androidx.compose.material.icons.Icons
                            import androidx.compose.material.icons.filled.CalendarToday
                            import androidx.compose.ui.Alignment
                            import androidx.compose.ui.Modifier
                            import androidx.compose.ui.graphics.Color
                            import androidx.compose.ui.text.font.FontWeight
                            import androidx.compose.ui.tooling.preview.Preview
                            import androidx.compose.ui.unit.dp
                            import androidx.compose.ui.unit.sp
                            import androidx.navigation.compose.rememberNavController

                            // ==== DỮ LIỆU MODEL ====
                            data class NotificationItem(
                                val date: String,
                                val content: String
                            )

                            // ==== DỮ LIỆU DEMO ====
                            val sampleNotifications = listOf(
                                NotificationItem("18/06/2025", "Số tiền cho ngân sách Ăn uống đã vượt mức 80%"),
                                NotificationItem("17/06/2025", "Ngân sách Giải trí còn lại dưới 10%"),
                                NotificationItem("16/06/2025", "Đã thêm một giao dịch mới vào ngân sách Di chuyển")
                            )

                            // ==== PREVIEW ====
                            @Preview(showBackground = true)
                            @Composable
                            fun NotificationScreenPreview() {
                                val navController = rememberNavController()
                                NotificationScreen(
                                    navController = navController,
                                    account_id = "123",
                                    notifications = sampleNotifications
                                )
                            }

                            // ==== MÀN HÌNH CHÍNH ====
                            @Composable
                            fun NotificationScreen(
                                navController: NavHostController,
                                account_id: String,
                                notifications: List<NotificationItem>
                            ) {
                                Scaffold(
                                    bottomBar = { BottomNavigationBar(navController, account_id) }
                                ) { paddingValues ->
                                    Box(
                                        modifier = Modifier
                                            .padding(paddingValues)
                                            .fillMaxSize()
                                            .background(Color(0xFFE0E0E0)), // Nền xám nhạt
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth(0.95f)
                                                .fillMaxHeight(0.85f)
                                                .padding(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = "Thông báo",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .align(Alignment.CenterHorizontally)
                                                        .padding(8.dp)
                                                )

                                                HorizontalDivider(thickness = 1.dp, color = Color.Gray)

                                                Spacer(modifier = Modifier.height(4.dp))

                                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                                    itemsIndexed(notifications) { index, notification ->
                                                        Card(
                                                            shape = RoundedCornerShape(10.dp),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 4.dp)
                                                        ) {
                                                            Column(modifier = Modifier.padding(12.dp)) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.CalendarToday,
                                                                        contentDescription = null,
                                                                        modifier = Modifier.size(18.dp),
                                                                        tint = Color.Gray
                                                                    )
                                                                    Spacer(modifier = Modifier.width(8.dp))
                                                                    Text(
                                                                        text = "Ngân sách, ${notification.date}",
                                                                        fontSize = 12.sp,
                                                                        color = Color.Gray
                                                                    )
                                                                }

                                                                Spacer(modifier = Modifier.height(8.dp))

                                                                Text(
                                                                    text = notification.content,
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Medium
                                                                )

                                                                if (index < notifications.size - 1) {
                                                                    Spacer(modifier = Modifier.height(12.dp))
                                                                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
