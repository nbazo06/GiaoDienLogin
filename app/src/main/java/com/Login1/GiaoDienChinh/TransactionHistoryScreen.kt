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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Login1.GiaoDienLogin.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


//Anh trích giao dịch từ database nha, đây là em để fake data

data class GiaoDich(
    val id: Int,
    val ngay: String,
    val tenLoai: String,
    val soTien: Int,
    val iconRes: Int,
    val thuNhap: Boolean
)

@Preview
@Composable
fun TransactionHistoryScreen() {
    var nguonTien by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tháng này") } // 👈 Thêm dòng này

    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val homNay = LocalDate.now().format(formatter)
    val homQua = LocalDate.now().minusDays(1).format(formatter)

    val fakeData = mapOf(
        homNay to listOf(
            GiaoDich(1, homNay, "Giáo dục", 35000, R.drawable.scholarship, false),
            GiaoDich(2, homNay, "Lương", 1000000, R.drawable.cash, true),
            GiaoDich(3, homNay, "Ăn uống", 30000, R.drawable.ramen, false)
        ),
        homQua to listOf(
            GiaoDich(4, homQua, "Giáo dục", 30000, R.drawable.scholarship, false),
            GiaoDich(5, homQua, "Xăng, xe", 50000, R.drawable.motorcycle, false),
            GiaoDich(6, homQua, "Ăn uống", 85000, R.drawable.ramen, false)
        )
    )

    Scaffold(
        bottomBar = { BottomNavigationBar() }
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
                    NguonTienDropdown(
                        nguonTien = nguonTien,
                        onNguonTienChange = { nguonTien = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp)) // 👈 Thêm khoảng cách giữa "Tổng cộng" và các tab

                MonthFilterButtons(
                    selected = selectedFilter,
                    onSelectedChange = { selectedFilter = it }
                )
            }

            TopBarIcons()

            FilterButtonsRow()

            TransactionHistoryContent(fakeData)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NguonTienDropdown(
    nguonTien: String,
    onNguonTienChange: (String) -> Unit
) {
    var expandedNguonTien by remember { mutableStateOf(false) }

    val nguonTienList = listOf(
        NguonTienItem(R.drawable.cash, "Tiền mặt"),
        NguonTienItem(R.drawable.atm, "Ngân hàng")
    )

    ExposedDropdownMenuBox(
        expanded = expandedNguonTien,
        onExpandedChange = { expandedNguonTien = !expandedNguonTien }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = nguonTien,
            onValueChange = {},
            placeholder = { Text("Tổng cộng", fontSize = 20.sp) },
            singleLine = true,
            modifier = Modifier
                .width(170.dp)
                .menuAnchor(),
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.reorder),
                    contentDescription = "Dropdown Icon",
                    modifier = Modifier.size(24.dp)
                )
            },
            shape = RoundedCornerShape(15.dp),
            textStyle = TextStyle(fontSize = 20.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White
            )
        )

        ExposedDropdownMenu(
            expanded = expandedNguonTien,
            onDismissRequest = { expandedNguonTien = false }
        ) {
            nguonTienList.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = item.icon),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = item.title)
                        }
                    },
                    onClick = {
                        onNguonTienChange(item.title)
                        expandedNguonTien = false
                    }
                )
            }
        }
    }
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
            // các Button như cũ
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

@Composable
fun TransactionHistoryContent(transactionsByDate: Map<String, List<GiaoDich>>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 150.dp)
    ) {
        LichSuGiaoDichScreen(transactionsByDate)
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
            val soDuText = if (isPositive) "+%,d".format(soDu) else "-%,d".format(kotlin.math.abs(soDu))
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
