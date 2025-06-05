package com.Login1.GiaoDienChinh

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Scaffold
import com.Login1.GiaoDienLogin.R
import androidx.navigation.NavHostController

@Preview
@Composable
fun HomeScreen(navController: NavHostController? = null) {
    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(
                    color = Color.Black,
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0))
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nhóm trái
                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.home),
                            contentDescription = "Home",
                            modifier = Modifier.size(35.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.file),
                            contentDescription = "File",
                            modifier = Modifier.size(35.dp)
                        )
                    }

                    // Nút thêm ở giữa
                    Image(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = "Add",
                        modifier = Modifier.size(60.dp)
                    )

                    // Nhóm phải
                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.wallet),
                            contentDescription = "Wallet",
                            modifier = Modifier.size(35.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.user),
                            contentDescription = "User",
                            modifier = Modifier.size(35.dp)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            var selectedTab by remember { mutableStateOf("Tuần") }
            Spacer(modifier = Modifier.height(10.dp))

            Column(modifier = Modifier.fillMaxSize().background(Color(0xFFE0E0E0))) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text("0.00 đ", fontSize = 30.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Tổng số dư", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Visibility, contentDescription = "Show Balance")
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = {}) {
                            Image(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = "Notification"
                            )
                        }
                        IconButton(onClick = {}) {
                            Image(
                                painter = painterResource(id = R.drawable.bell),
                                contentDescription = "Notification"
                            )
                        }
                    }
                }

                Card(
                    elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(
                        defaultElevation = 4.dp
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ví của tôi", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Xem tất cả", color = Color.Green, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Spacer(modifier = Modifier.width(10.dp))

                            Image(
                                painter = painterResource(id = R.drawable.cash),
                                contentDescription = "Cash",
                                modifier = Modifier.size(35.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("Tiền mặt", fontSize = 20.sp)

                            Spacer(modifier = Modifier.weight(1f))
                            Text("0.00đ", fontSize = 20.sp)

                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Spacer(modifier = Modifier.width(10.dp))

                            Image(
                                painter = painterResource(id = R.drawable.cash),
                                contentDescription = "Cash",
                                modifier = Modifier.size(35.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text("Ngân hàng", fontSize = 20.sp)

                            Spacer(modifier = Modifier.weight(1f))
                            Text("0.00đ", fontSize = 20.sp)

                            Spacer(modifier = Modifier.width(10.dp))
                        }

                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Chi tiêu nhiều nhất", fontWeight = FontWeight.Bold)
                        Text("Xem chi tiết", color = Color.Green)
                    }
                }

                Card(
                    elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(
                        defaultElevation = 4.dp
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp)
                )

                {
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(250.dp)
                            .height(40.dp) // Tăng chiều cao để chữ có chỗ hiển thị
                            .background(Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(50))
                            .padding(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Button(
                                onClick = { selectedTab = "Tuần" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedTab == "Tuần") Color.LightGray else Color.White
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Text("Tuần", fontSize = 12.sp, color = Color.Black)
                            }

                            Button(
                                onClick = { selectedTab = "Tháng" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedTab == "Tháng") Color.LightGray else Color.White
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Text("Tháng", fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically)
                        {
                            Spacer(modifier = Modifier.width(20.dp))
                            Text("🍕 Ăn uống", modifier = Modifier.weight(1f), fontSize = 20.sp)
                            Text("70%", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(20.dp))
                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(20.dp))
                            Text("🏠 Thuê nhà", modifier = Modifier.weight(1f), fontSize = 20.sp)
                            Text("20%", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(20.dp))
                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(20.dp))
                            Text("🛍️ Mua sắm", modifier = Modifier.weight(1f), fontSize = 20.sp)
                            Text("10%", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(20.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Giao dịch gần đây", fontWeight = FontWeight.Bold)
                        Text("Xem chi tiết", color = Color.Green)
                    }
                }

                Card(
                    elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(
                        defaultElevation = 4.dp
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(16.dp)         // Chiều rộng cụ thể
                        .height(170.dp)         // Chiều cao cụ thể
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Giao dịch đã thêm sẽ hiển thị ở đây", fontSize = 16.sp)
                    }

                }

            }
        }
    }


}
