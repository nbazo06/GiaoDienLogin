package com.Login1.GiaoDienChinh

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Login1.GiaoDienLogin.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.navigation.NavHostController
import com.Login1.service.AuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.Login1.service.GiaoDich
import kotlin.math.abs

//@Preview
@Composable
fun TransactionHistoryScreen(navController: NavHostController, account_id: String) {
    var nguonTien by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tháng này") }

    var transactions by remember { mutableStateOf<Map<String, List<GiaoDich>>>(emptyMap()) }
    var expandedNguonTien by remember { mutableStateOf(false) }

    val nguonTienList = listOf(
        NguonTienItem(R.drawable.cash, "Tất cả"),
        NguonTienItem(R.drawable.cash, "Tiền mặt"),
        NguonTienItem(R.drawable.atm, "Ngân hàng")
    )

    // Load transactions
    LaunchedEffect(account_id) {
        CoroutineScope(Dispatchers.IO).launch {
            AuthService.getTransactions(account_id).fold(
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
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, account_id) }
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
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
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

            TransactionHistoryContent(transactions, nguonTien)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionHistoryScreenPreview() {
    val navController = rememberNavController()
    TransactionHistoryScreen(navController = navController, account_id = "123")
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun NguonTienDropdown(
//    nguonTien: String,
//    onNguonTienChange: (String) -> Unit
//) {
//    var expandedNguonTien by remember { mutableStateOf(false) }
//
//    val nguonTienList = listOf(
//        NguonTienItem(R.drawable.cash, "Tiền mặt"),
//        NguonTienItem(R.drawable.atm, "Ngân hàng")
//    )
//
//    ExposedDropdownMenuBox(
//        expanded = expandedNguonTien,
//        onExpandedChange = { expandedNguonTien = !expandedNguonTien }
//    ) {
//        OutlinedTextField(
//            readOnly = true,
//            value = nguonTien,
//            onValueChange = {},
//            placeholder = { Text("Tổng cộng", fontSize = 20.sp) },
//            singleLine = true,
//            modifier = Modifier
//                .width(170.dp)
//                .menuAnchor(),
//            trailingIcon = {
//                Icon(
//                    painter = painterResource(id = R.drawable.reorder),
//                    contentDescription = "Dropdown Icon",
//                    modifier = Modifier.size(24.dp)
//                )
//            },
//            shape = RoundedCornerShape(15.dp),
//            textStyle = TextStyle(fontSize = 20.sp),
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = Color.White,
//                unfocusedBorderColor = Color.White,
//                focusedContainerColor = Color.White,
//                unfocusedContainerColor = Color.White,
//                disabledContainerColor = Color.White
//            )
//        )
//
//        ExposedDropdownMenu(
//            expanded = expandedNguonTien,
//            onDismissRequest = { expandedNguonTien = false }
//        ) {
//            nguonTienList.forEach { item ->
//                DropdownMenuItem(
//                    text = {
//                        Row(verticalAlignment = Alignment.CenterVertically) {
//                            Image(
//                                painter = painterResource(id = item.iconResid),
//                                contentDescription = null,
//                                modifier = Modifier.size(24.dp)
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(text = item.ten)
//                        }
//                    },
//                    onClick = {
//                        onNguonTienChange(item.ten)
//                        expandedNguonTien = false
//                    }
//                )
//            }
//        }
//    }
//}

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
    nguonTien: String
) {
    val filteredTransactions = filterTransactionsByNguonTien(transactionsByDate, nguonTien)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 150.dp)
    ) {
        LichSuGiaoDichScreen(filteredTransactions)
    }
}


@Composable
fun LichSuGiaoDichScreen(transactionsByDate: Map<String, List<GiaoDich>>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        transactionsByDate.forEach { (date, transactions) ->
            // Tính tổng thu và chi
            val tongThu = transactions.filter { it.thuNhap }.sumOf { it.soTien }
            val tongChi = transactions.filter { !it.thuNhap }.sumOf { it.soTien }
            val soDu = tongThu - tongChi

            // Xử lý ngày tháng để lấy định dạng hiển thị
            val ngaySo = date.substring(0, 2)
            val ngayLocalDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            val homNay = LocalDate.now()
            val homQua = homNay.minusDays(1)

            val label = when (ngayLocalDate) {
                homNay -> "Hôm nay"
                homQua -> "Hôm qua"
                else -> ""
            }
            val thangNam = "tháng ${ngayLocalDate.monthValue} ${ngayLocalDate.year}"

            val isPositive = soDu >= 0
            val soDuText = if (isPositive) "+%,d".format(soDu) else "-%,d".format(abs(soDu))
            val soDuColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)

            // Container cho từng ngày
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
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = thangNam,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Text(
                        text = soDuText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = soDuColor
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hiển thị các giao dịch trong ngày
                transactions.forEach { giaoDich ->
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
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "%,d".format(giaoDich.soTien),
                            color = if (giaoDich.thuNhap) Color(0xFF2196F3) else Color(0xFFF44336),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun MonthFilterButtons(
    selected: String,
    onSelectedChange: (String) -> Unit
) {
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

//Ham hộp thoại confirm xóa hay không, bienluutru em để ở trên nhe
@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .clickable(enabled = false) {}, // Ngăn click bên ngoài
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
