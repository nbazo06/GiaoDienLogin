package com.Login1.GiaoDienChinh

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
    val ngay: String,        // định dạng "dd-MM-yyyy"
    val loai: String,        // ví dụ: "Giáo dục", "Lương"
    val soTien: Int,         // đơn vị: đồng
    val iconResId: Int,      // id của drawable
    val isThuNhap: Boolean   // true nếu là thu nhập
)

@Composable
fun NgayGiaoDich(
    ngay: String,
    danhSach: List<GiaoDich>
) {
    val tongThu = danhSach.filter { it.isThuNhap }.sumOf { it.soTien }
    val tongChi = danhSach.filter { !it.isThuNhap }.sumOf { it.soTien }
    val formatTong = tongThu - tongChi
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val localDate = LocalDate.parse(ngay, formatter)
    val homNay = LocalDate.now()
    val homQua = LocalDate.now().minusDays(1)

    val labelNgay = when (localDate) {
        homNay -> "Hôm nay"
        homQua -> "Hôm qua"
        else -> ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {

                        Text(
                            text = "%02d".format(localDate.dayOfMonth), // Ngày có 2 chữ số
                            fontSize = 30.sp,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(Modifier.width(10.dp))

                        Column {
                        if (labelNgay.isNotEmpty()) {
                            Text(
                                text = labelNgay,
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                        }
                        Text(
                            text = localDate.format(
                                DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi"))
                            ),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Text(
                    text = if (formatTong >= 0) "+%,d".format(formatTong) else "-%,d".format(-formatTong),
                    color = if (formatTong >= 0) Color(0xFF2E7D32) else Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            danhSach.forEach { giaoDich ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = giaoDich.iconResId),
                            contentDescription = giaoDich.loai,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text = giaoDich.loai,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "%,d".format(giaoDich.soTien),
                        fontSize = 18.sp,
                        color = if (giaoDich.isThuNhap) Color(0xFF0288D1) else Color.Red,
                    )
                }
            }
        }
    }
}

@Composable
fun LichSuGiaoDichScreen(transactionsByDate: Map<String, List<GiaoDich>>) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        transactionsByDate.entries.sortedByDescending { it.key }.forEach { (ngay, danhSach) ->
            NgayGiaoDich(ngay = ngay, danhSach = danhSach)
        }
    }
}

@Composable
fun BottomIconWithText(iconResId: Int, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = label,
            modifier = Modifier.size(35.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Black
        )
    }
}


@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen() {
    var expandedNguonTien by remember { mutableStateOf(false) }
    var nguonTien by remember { mutableStateOf("") }

    val nguonTienList = listOf(
        NguonTienItem(R.drawable.cash, "Tiền mặt"),
        NguonTienItem(R.drawable.atm, "Ngân hàng")
    )

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
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BottomIconWithText(R.drawable.home, "Trang chủ")
                        BottomIconWithText(R.drawable.file, "Lịch sử")
                    }

                    Box(
                        modifier = Modifier.weight(0.8f),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.add),
                            contentDescription = "Thêm",
                            modifier = Modifier.size(65.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BottomIconWithText(R.drawable.wallet, "Ngân sách")
                        BottomIconWithText(R.drawable.user, "Người dùng")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE0E0E0))
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            ) {
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
                                    nguonTien = item.title
                                    expandedNguonTien = false
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp)
            ) {
                IconButton(onClick = {}) {
                    Image(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = "Search",
                        modifier = Modifier.size(30.dp)
                    )
                }

                IconButton(onClick = {}) {
                    Image(
                        painter = painterResource(id = R.drawable.ellipsis),
                        contentDescription = "More",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 75.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { /* Xử lý Tháng x */ },
                    modifier = Modifier.weight(0.9f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
                ) {
                    Text("THÁNG x", color = Color.Black, fontSize = 12.sp)
                }

                Button(
                    onClick = { /* Xử lý Tháng trước */ },
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
                ) {
                    Text("THÁNG TRƯỚC", color = Color.Black, fontSize = 12.sp)
                }

                Button(
                    onClick = { /* Xử lý Tháng này */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
                ) {
                    Text("THÁNG NÀY", color = Color.Black, fontSize = 12.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 150.dp)
            ) {
                LichSuGiaoDichScreen(fakeData)
            }
        }
    }
}


