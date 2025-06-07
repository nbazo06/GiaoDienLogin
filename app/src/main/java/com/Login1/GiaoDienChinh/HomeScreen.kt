package com.Login1.GiaoDienChinh

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.Login1.GiaoDienLogin.R

@Preview
@Composable
fun HomeScreen(navController: NavHostController? = null) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFE0E0E0))
                .verticalScroll(rememberScrollState())
        ) {
            HeaderSection()
            BalanceCard()
            TopSpendingSection()
            RecentTransactionsSection()
        }
    }
}

@Composable
fun HeaderSection() {

    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("0.00 đ", fontSize = 30.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tổng số dư", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Visibility, contentDescription = "Show Balance")
            }
        }
        Row {
            IconButton(onClick = {}) {
                Image(painter = painterResource(id = R.drawable.search), contentDescription = "Search")
            }
            IconButton(onClick = {}) {
                Image(painter = painterResource(id = R.drawable.bell), contentDescription = "Notification")
            }
        }
    }

}

@Composable
fun BottomNavigationBar() {
    Column {
        HorizontalDivider(color = Color.Black, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE0E0E0))
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Home với label
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = { /* Xử lý click Home */ }) {
                    Image(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = "Home",
                        modifier = Modifier.size(35.dp)
                    )
                }
                Text("Trang chủ", fontSize = 12.sp, color = Color.Black)
            }

            // Nút File với label
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = { /* Xử lý click File */ }) {
                    Image(
                        painter = painterResource(id = R.drawable.file),
                        contentDescription = "File",
                        modifier = Modifier.size(35.dp)
                    )
                }
                Text("Lịch sử", fontSize = 12.sp, color = Color.Black)
            }

            // Nút Add lớn hơn, không có label
            IconButton(
                onClick = { /* Xử lý click Add */ },
                modifier = Modifier.size(70.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "Add",
                    modifier = Modifier.size(60.dp)
                )
            }

            // Nút Wallet với label
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = { /* Xử lý click Wallet */ }) {
                    Image(
                        painter = painterResource(id = R.drawable.wallet),
                        contentDescription = "Wallet",
                        modifier = Modifier.size(35.dp)
                    )
                }
                Text("Ngân sách", fontSize = 12.sp, color = Color.Black)
            }

            // Nút User với label
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = { /* Xử lý click User */ }) {
                    Image(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "User",
                        modifier = Modifier.size(35.dp)
                    )
                }
                Text("Cá nhân", fontSize = 12.sp, color = Color.Black)
            }
        }
    }
}


@Composable
fun BalanceCard() {
    Card(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically  // thêm dòng này
            ) {
                Text("Ví của tôi", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                TextButton(onClick = { /* Xử lý click xem tất cả */ }) {
                    Text("Xem tất cả", color = Color.Green, fontSize = 15.sp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 1.dp, color = Color.Gray)
            Spacer(modifier = Modifier.height(14.dp))

            WalletItem("Tiền mặt", "0.00đ")
            Spacer(modifier = Modifier.height(15.dp))
            WalletItem("Ngân hàng", "0.00đ")
        }
    }
}

@Composable
fun WalletItem(name: String, amount: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(10.dp))
        Image(painter = painterResource(id = R.drawable.cash), contentDescription = name, modifier = Modifier.size(35.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(name, fontSize = 20.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(amount, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(10.dp))
    }
}

@Composable
fun TopSpendingSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically // thêm dòng này
        ) {
            Text("Chi tiêu nhiều nhất", fontWeight = FontWeight.Bold)
            TextButton(onClick = { /* Xử lý click xem tất cả */ }) {
                Text("Xem chi tiết", color = Color.Green, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SpendingItem("🍕 Ăn uống", "70%")
                SpendingItem("🏠 Thuê nhà", "20%")
                SpendingItem("🛍️ Mua sắm", "10%")
            }
        }
    }
}

//Anh thêm data cho cái chi tiêu nhieu nhat o day nha

@Composable
fun SpendingItem(label: String, percentage: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), fontSize = 20.sp)
        Text(percentage, fontSize = 20.sp)
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun RecentTransactionsSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically // thêm dòng này
        ) {
            Text("Giao dịch gần đây", fontWeight = FontWeight.Bold)
            TextButton(onClick = { /* Xử lý click xem tất cả */ }) {
                Text("Xem chi tiết", color = Color.Green, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
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
