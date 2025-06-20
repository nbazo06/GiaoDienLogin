package com.Login1.GiaoDienChinh

import android.util.Log
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
import androidx.navigation.compose.rememberNavController
import com.Login1.GiaoDienLogin.R
import com.Login1.service.AuthService
import com.Login1.service.GiaoDich
import com.Login1.service.Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController = navController, user_id = "1")
}

//@Preview
@Composable
fun HomeScreen(navController: NavHostController, user_id: String) {
    var transactions by remember { mutableStateOf<Map<String, List<GiaoDich>>>(emptyMap()) }
    var wallets by remember { mutableStateOf<List<Wallet>>(emptyList()) }

    // Load transactions
    LaunchedEffect(user_id) {
        CoroutineScope(Dispatchers.IO).launch {
            AuthService.getWallets(user_id).fold(
                onSuccess = { fetchedWallets ->
                    withContext(Dispatchers.Main) {
                        wallets = fetchedWallets
                    }
                },
                onFailure = { exception ->
                    withContext(Dispatchers.Main) {
                        Log.e("HomeScreen", "Lỗi lấy ví: ${exception.message}")
                    }
                }
            )
            AuthService.getTransactions(user_id).fold(
                onSuccess = { fetchedTransactions ->
                    withContext(Dispatchers.Main) {
                        transactions = fetchedTransactions
                    }
                },
                onFailure = { exception ->
                    withContext(Dispatchers.Main) {
                        Log.e("HomeScreen", "Lỗi: ${exception.message}")
                    }
                }
            )
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController, user_id)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFE0E0E0))
                .verticalScroll(rememberScrollState())
        ) {
            HeaderSection(transactions)
            BalanceCard(transactions, wallets)
            TopSpendingSection(transactions)
            RecentTransactionsSection(navController, user_id, transactions)
        }
    }
}

@Composable
fun HeaderSection(transactions: Map<String, List<GiaoDich>>) {

    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val tongSoTien = transactions.values.flatten().sumOf { giaoDich ->
                if (giaoDich.thuNhap) giaoDich.soTien else -giaoDich.soTien
            }
            Text("${DecimalFormat("#,###").format(tongSoTien).replace(",", ".")} đ", fontSize = 30.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tổng số dư", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(4.dp))
                //Icon(Icons.Default.Visibility, contentDescription = "Show Balance")
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
fun BottomNavigationBar(navController: NavHostController, user_id: String) {
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
                IconButton(onClick = {
                    navController.navigate("home_screen/${user_id}") {
                        launchSingleTop = true
                    }
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = "Home",
                        modifier = Modifier.size(35.dp)
                    )
                }
                Text("Trang chủ", fontSize = 12.sp, color = Color.Black)
            }

            // Nút History với label
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        navController.navigate("transaction_history_screen/${user_id}") {
                            launchSingleTop = true
                        }
                    }
                ) {
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
                onClick = {
                    navController.navigate("add_transaction_screen/${user_id}") {
                        launchSingleTop = true
                    }
                },
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
                IconButton(onClick = {
                    navController.navigate("budget_screen/${user_id}") {
                        launchSingleTop = true
                    }
                }) {
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
                IconButton(onClick = {
                    navController.navigate("personal_screen/${user_id}") {
                        launchSingleTop = true
                    }
                }) {
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
fun BalanceCard(transactions: Map<String, List<GiaoDich>>, wallets: List<Wallet>) {
    Card(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ví của tôi", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                TextButton(onClick = { /* Xử lý click xem tất cả */ }) {
                    Text("Xem tất cả", color = Color.Green, fontSize = 15.sp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(thickness = 1.dp, color = Color.Gray)
            Spacer(modifier = Modifier.height(14.dp))

            wallets.forEach { wallet ->
                val tongSoDu = tinhTongTheoWalletID(transactions, wallet.id)
                WalletItem(wallet.iconResid, wallet.name, "${DecimalFormat("#,###").format(tongSoDu).replace(",", ".")} đ")
                Spacer(modifier = Modifier.height(15.dp))
            }
        }
    }
}

@Composable
fun WalletItem(icon: Int, name: String, amount: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(10.dp))
        Image(painter = painterResource(id = icon), contentDescription = name, modifier = Modifier.size(35.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(name, fontSize = 20.sp)
        Spacer(modifier = Modifier.weight(1f))
        Text(amount, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(10.dp))
    }
}

@Composable
fun TopSpendingSection(transactions: Map<String, List<GiaoDich>>) {
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
            val tyLeChiTieu = tinhTiLeChiTieuTheoDanhMuc(transactions)
            Column(modifier = Modifier.padding(16.dp)) {
                tyLeChiTieu.forEach { (icon, label, percent) ->
                    SpendingItem(icon = icon, label = label, percentage = percent)
                }
            }
        }
    }
}

//Anh thêm data cho cái chi tiêu nhieu nhat o day nha

@Composable
fun SpendingItem(icon: Int, label: String, percentage: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(35.dp)
        )
        Text(label, modifier = Modifier.weight(1f), fontSize = 20.sp)
        Text(percentage, fontSize = 20.sp)
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun RecentTransactionsSection(navController: NavHostController, user_id: String, transactionsByDate: Map<String, List<GiaoDich>>) {
    val giaoDichGanNhat = get3GiaoDichGanNhat(transactionsByDate)

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Giao dịch gần đây", fontWeight = FontWeight.Bold)
            TextButton(onClick = {
                navController.navigate("transaction_history_screen/${user_id}") {
                    launchSingleTop = true
                }
            }) {
                Text("Xem chi tiết", color = Color.Green, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (giaoDichGanNhat.isEmpty()) {
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                giaoDichGanNhat.forEach { giaoDich ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = giaoDich.iconRes),
                            contentDescription = giaoDich.tenLoai,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(giaoDich.tenLoai, fontSize = 16.sp)
                            Text(giaoDich.ngay, fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(
                            text = "%,d".format(giaoDich.soTien),
                            color = if (giaoDich.thuNhap) Color(0xFF2196F3) else Color(0xFFF44336),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}


fun tinhTongTheoNguonTien(
    transactions: Map<String, List<GiaoDich>>,
    loaiNguonTien: String // "Tiền mặt" hoặc "Bank"
): Int {
    return transactions.values
        .flatten()
        .filter { it.nguonTien == loaiNguonTien }
        .sumOf { if (it.thuNhap) it.soTien else -it.soTien }
}

fun tinhTiLeChiTieuTheoDanhMuc(transactions: Map<String, List<GiaoDich>>): List<Triple<Int, String, String>> {
    val allTransactions = transactions.values.flatten()

    // Lọc ra các khoản chi (thuNhap == false)
    val chiTieuList = allTransactions.filter { !it.thuNhap }

    val tongChiTieu = chiTieuList.sumOf { it.soTien }.takeIf { it != 0 } ?: 1 // tránh chia 0

    return chiTieuList
        .groupBy { Pair(it.tenLoai, it.iconRes) }
        .map { (key, list) ->
            val (tenLoai, iconRes) = key
            val tongTheoLoai = list.sumOf { it.soTien }
            val tyLe = tongTheoLoai * 100.0 / tongChiTieu
            Triple(iconRes, tenLoai, String.format("%.1f%%", tyLe))
        }
        .sortedByDescending {
            it.third.removeSuffix("%").toDouble()
        } // sắp xếp giảm dần theo tỷ lệ
}

fun get3GiaoDichGanNhat(transactionsByDate: Map<String, List<GiaoDich>>): List<GiaoDich> {
    return transactionsByDate
        .toSortedMap(compareByDescending { LocalDate.parse(it, DateTimeFormatter.ofPattern("dd-MM-yyyy")) })
        .values
        .flatten()
        .sortedByDescending { LocalDate.parse(it.ngay, DateTimeFormatter.ofPattern("dd-MM-yyyy")) }
        .take(3)
}

fun tinhTongTheoWalletID(transactions: Map<String, List<GiaoDich>>, walletId: String): Int {
    return transactions.values
        .flatten()
        .filter { it.nguonTien == walletId }
        .sumOf { if (it.thuNhap) it.soTien else -it.soTien }
}
