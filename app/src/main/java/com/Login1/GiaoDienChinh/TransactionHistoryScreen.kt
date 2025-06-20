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
    var reloadTrigger by remember { mutableStateOf(0) }
    // Đưa state dialog lên đây
    var showDeleteDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<GiaoDich?>(null) }
    var resetDialogKey by remember { mutableStateOf(0) }

    val nguonTienList = listOf(
        NguonTienItem(R.drawable.cash, "Tất cả"),
        NguonTienItem(R.drawable.cash, "Tiền mặt"),
        NguonTienItem(R.drawable.atm, "Ngân hàng")
    )

    // Load transactions and wallets
    LaunchedEffect(user_id, reloadTrigger) {
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
            TransactionHistoryContent(
                navController = navController,
                transactionsByDate = transactions,
                nguonTien = nguonTien,
                wallets = wallets,
                user_id = user_id,
                onSwipeDelete = { giaoDich ->
                    Log.d("Swipe", "Swipe delete called for: ${giaoDich.id}")
                    transactionToDelete = giaoDich.copy()
                    showDeleteDialog = true
                },
                onSwipeEdit = { giaoDich ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = AuthService.deleteTransaction(giaoDich.id)
                        withContext(Dispatchers.Main) {
                            if (result.isSuccess) {
                                reloadTrigger++
                                navController.navigate("add_transaction_screen/${user_id}")
                            }
                        }
                    }
                },
                resetDialogKey = resetDialogKey,
                onDeleteTransaction = { transactionId ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val result = AuthService.deleteTransaction(transactionId)
                            if (result.isSuccess) {
                                withContext(Dispatchers.Main) { reloadTrigger++ }
                            }
                        } catch (_: Exception) {}
                    }
                }
            )
            // Dialog xác nhận xóa
            if (showDeleteDialog && transactionToDelete != null) {
                DeleteConfirmationDialog(
                    onDismiss = {
                        showDeleteDialog = false
                        resetDialogKey++
                    },
                    onConfirmDelete = {
                        showDeleteDialog = false
                        transactionToDelete?.let { giaoDich ->
                            // Gọi xóa
                            CoroutineScope(Dispatchers.IO).launch {
                                AuthService.deleteTransaction(giaoDich.id)
                                withContext(Dispatchers.Main) {
                                    reloadTrigger++
                                }
                            }
                        }
                        resetDialogKey++
                    }
                )
            }
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
    navController: NavHostController,
    transactionsByDate: Map<String, List<GiaoDich>>,
    nguonTien: String,
    wallets: List<Wallet>,
    user_id: String,
    onSwipeDelete: (GiaoDich) -> Unit,
    onSwipeEdit: (GiaoDich) -> Unit,
    resetDialogKey: Int,
    onDeleteTransaction: (String) -> Unit
) {
    val filteredTransactions = filterTransactionsByNguonTien(transactionsByDate, nguonTien)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 150.dp)
    ) {
        LichSuGiaoDichScreen(filteredTransactions, wallets, onSwipeDelete, onSwipeEdit, resetDialogKey)
    }
}

@Composable
fun LichSuGiaoDichScreen(
    transactionsByDate: Map<String, List<GiaoDich>>,
    wallets: List<Wallet>,
    onSwipeDelete: (GiaoDich) -> Unit,
    onSwipeEdit: (GiaoDich) -> Unit,
    resetDialogKey: Int
) {
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
                        onSwipeDelete = { onSwipeDelete(giaoDich) },
                        onSwipeEdit = { onSwipeEdit(giaoDich) },
                        resetDialogKey = resetDialogKey
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
    resetDialogKey: Int,
    content: @Composable () -> Unit
) {
    var offsetX by remember(resetDialogKey) { mutableStateOf(0f) }
    val swipeThreshold = 120f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .pointerInput(resetDialogKey) {
                detectDragGestures(
                    onDragEnd = {
                        if (offsetX <= -swipeThreshold) {
                            onSwipeDelete()
                        } else if (offsetX >= swipeThreshold) {
                            onSwipeEdit()
                        }
                        offsetX = 0f // luôn reset swipe về vị trí cũ
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(-300f, 300f)
                    }
                )
            }
    ) {
        // Background hành động
        if (offsetX < -swipeThreshold / 2) {
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
        } else if (offsetX > swipeThreshold / 2) {
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
