package com.Login1.GiaoDienChinh

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.Login1.GiaoDienLogin.R
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.Login1.service.AuthService
import com.Login1.service.GiaoDich
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.Login1.service.Wallet
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.abs



@Composable
fun TransactionHistoryScreen(navController: NavHostController, user_id: String) {
    var nguonTien by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tháng này") }
    var transactions by remember { mutableStateOf<Map<String, List<GiaoDich>>>(emptyMap()) }
    var wallets by remember { mutableStateOf<List<Wallet>>(emptyList()) }

    val nguonTienList = listOf(
        NguonTienItem(R.drawable.cash, "Tất cả"),
        NguonTienItem(R.drawable.cash, "Tiền mặt"),
        NguonTienItem(R.drawable.atm, "Ngân hàng")
    )

    // Load transactions and wallets
    LaunchedEffect(user_id) {
        CoroutineScope(Dispatchers.IO).launch {
            AuthService.getTransactions(user_id).fold(
                onSuccess = { fetchedCategories ->
                    withContext(Dispatchers.Main) {
                        transactions = fetchedCategories
                    }
                },
                onFailure = { exception ->
                    withContext(Dispatchers.Main) {
                        Log.e("AddTransactionScreen", "Lỗi: ${exception.message}")
                    }
                }
            )
            AuthService.getWallets(user_id).fold(
                onSuccess = { fetchedWallets ->
                    withContext(Dispatchers.Main) {
                        wallets = fetchedWallets
                    }
                },
                onFailure = { exception ->
                    withContext(Dispatchers.Main) {
                        Log.e("TransactionHistory", "Lỗi lấy ví: ${exception.message}")
                    }
                }
            )
        }
    }
//    LaunchedEffect(Unit) {
//        val fakeTransactions = listOf(
//            GiaoDich(20000, "Ăn sáng", false, "20-06-2025", "Tiền mặt", R.drawable.ramen),
//            GiaoDich(5000000, "Tiền lương", true, "20-06-2025", "Ngân hàng", R.drawable.cash),
//            GiaoDich(100000, "Mua sách", false, "20-06-2025", "Tiền mặt", R.drawable.onlineshopping),
//            GiaoDich(300000, "Đi chơi", false, "19-06-2025", "Tiền mặt", R.drawable.cash),
//            GiaoDich(1000000, "Làm thêm", true, "19-06-2025", "Ngân hàng", R.drawable.cash)
//        )
//        val grouped = fakeTransactions.groupBy { it.ngay }
//        transactions = grouped
//    }


    Scaffold(
        bottomBar = { BottomNavigationBar(navController, user_id) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE0E0E0))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    NguonTienBox(
                        nguonTien = nguonTien,
                        onNguonTienSelected = { selected -> nguonTien = selected },
                        nguonTienList = nguonTienList
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                MonthFilterButtons(
                    selected = selectedFilter,
                    onSelectedChange = { selectedFilter = it }
                )
            }

            TopBarIcons()
            FilterButtonsRow()
            TransactionHistoryContent(transactions, nguonTien, wallets)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionHistoryScreenPreview() {
    val navController = rememberNavController()
    TransactionHistoryScreen(navController = navController, user_id = "123")
}

@Composable
fun TopBarIcons() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopEnd
    ) {
        Row(
            modifier = Modifier.padding(top = 12.dp)
        ) {
            IconButton(onClick = { /*TODO*/ }) {
                Image(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = "Search",
                    modifier = Modifier.size(30.dp)
                )
            }

            IconButton(onClick = { /*TODO*/ }) {
                Image(
                    painter = painterResource(id = R.drawable.ellipsis),
                    contentDescription = "More",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun FilterButtonsRow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 75.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
        }
    }
}

@Composable
fun BottomIconWithText(iconRes: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(30.dp)
        )
        Text(text = label, fontSize = 12.sp)
    }
}

fun filterTransactionsByNguonTien(
    transactionsByDate: Map<String, List<GiaoDich>>,
    nguonTien: String
): Map<String, List<GiaoDich>> {
    if (nguonTien == "Tất cả" || nguonTien == "") return transactionsByDate

    return transactionsByDate.mapValues { (_, transactions) ->
        transactions.filter { it.nguonTien == nguonTien }
    }.filterValues { it.isNotEmpty() }
}

@Composable
fun TransactionHistoryContent(
    transactionsByDate: Map<String, List<GiaoDich>>,
    nguonTien: String,
    wallets: List<Wallet>
) {
    val filteredTransactions = filterTransactionsByNguonTien(transactionsByDate, nguonTien)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 150.dp)
    ) {
        LichSuGiaoDichScreen(filteredTransactions, wallets)
    }
}

@Composable
fun LichSuGiaoDichScreen(
    transactionsByDate: Map<String, List<GiaoDich>>,
    wallets: List<Wallet>
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<GiaoDich?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        transactionsByDate.forEach { (date, transactions) ->
            val tongThu = transactions.filter { it.thuNhap }.sumOf { it.soTien }
            val tongChi = transactions.filter { !it.thuNhap }.sumOf { it.soTien }
            val soDu = tongThu - tongChi
            val ngayLocalDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            val homNay = LocalDate.now()
            val homQua = homNay.minusDays(1)

            val label = when (ngayLocalDate) {
                homNay -> "Hôm nay"
                homQua -> "Hôm qua"
                else -> ""
            }
            val ngaySo = date.substring(0, 2)
            val thangNam = "tháng ${ngayLocalDate.monthValue} ${ngayLocalDate.year}"
            val isPositive = soDu >= 0
            val soDuText = if (isPositive) "+%,d".format(soDu) else "-%,d".format(abs(soDu))
            val soDuColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(15.dp))
                    .padding(16.dp)
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ngaySo,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(text = label, color = Color.Gray)
                            Text(text = thangNam, color = Color.Gray)
                        }
                    }
                    Text(
                        text = soDuText,
                        fontWeight = FontWeight.Bold,
                        color = soDuColor
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                transactions.forEach { giaoDich ->
                    val walletName =
                        wallets.find { it.id == giaoDich.nguonTien }?.name ?: "Không rõ"
                    SwipeableTransactionItem(
                        giaoDich = giaoDich,
                        onSwipeDelete = {
                            transactionToDelete = giaoDich
                            showDeleteDialog = true
                        },
                        onSwipeEdit = {
                            // TODO: chuyển sang màn sửa hoặc hiển thị dialog
                        }
                    ) {
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
                            Text(
                                text = giaoDich.tenLoai,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "% ,d".format(giaoDich.soTien),
                                color = if (giaoDich.thuNhap) Color(0xFF2196F3) else Color(0xFFF44336),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showDeleteDialog && transactionToDelete != null) {
        DeleteConfirmationDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirmDelete = {
                showDeleteDialog = false
                transactionToDelete?.let { giaoDich ->
                    // Gọi API xóa transaction
                    coroutineScope.launch {
                        try {
                            val transactionId = giaoDich.id
                            val result = AuthService.deleteTransaction(transactionId)
                            // Sau khi xóa thành công, bạn nên reload lại danh sách giao dịch
                            // ... gọi lại API lấy transactions ...
                        } catch (e: Exception) {
                            // Xử lý lỗi nếu cần
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun MonthFilterButtons(selected: String, onSelectedChange: (String) -> Unit) {
    val options = listOf("Tháng X", "Tháng trước", "Tháng này")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEach { label ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onSelectedChange(label) }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = if (label == selected) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (label == selected) {
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(40.dp)
                            .background(Color.Black, shape = RoundedCornerShape(1.dp))
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(onDismiss: () -> Unit, onConfirmDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray.copy(alpha = 0.3f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .width(300.dp)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Bạn có chắc chắn muốn xóa?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                HorizontalDivider(thickness = 1.dp, color = Color.Gray)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy", color = Color.Gray, fontSize = 16.sp)
                    }
                    TextButton(onClick = onConfirmDelete) {
                        Text("Xóa", color = Color.Red, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeableTransactionItem(
    giaoDich: GiaoDich,
    onSwipeDelete: () -> Unit,
    onSwipeEdit: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val swipeThreshold = 120f
    var actionTriggered by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (offsetX <= -swipeThreshold && !actionTriggered) {
                            onSwipeDelete()
                            actionTriggered = true
                        } else if (offsetX >= swipeThreshold && !actionTriggered) {
                            onSwipeEdit()
                            actionTriggered = true
                        } else {
                            // Nếu chưa đủ ngưỡng thì reset lại vị trí
                            offsetX = 0f
                            actionTriggered = false
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Giới hạn kéo tối đa để không bị trượt quá xa
                        offsetX = (offsetX + dragAmount.x).coerceIn(-300f, 300f)
                    }
                )
            }
    ) {
        // Background hành động
        if (offsetX < -swipeThreshold) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color.Red.copy(alpha = 0.8f))
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text("Xóa", color = Color.White, fontWeight = FontWeight.Bold)
            }
        } else if (offsetX > swipeThreshold) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0xFF4CAF50).copy(alpha = 0.8f))
                    .padding(start = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Sửa", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Foreground nội dung
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.toInt(), 0) }
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(12.dp)
        ) {
            content()
        }
    }
}
