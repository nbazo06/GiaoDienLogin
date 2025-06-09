package com.Login1.GiaoDienChinh

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Login1.GiaoDienLogin.R
import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import java.util.*
import androidx.navigation.NavHostController

data class DanhMucItem(val icon: Int, val title: String)
data class NguonTienItem(val icon: Int, val title: String)

@OptIn(ExperimentalMaterial3Api::class)
//@Preview
@Composable
fun AddTransactionScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf("Chi tiêu") }
    var soTien by remember { mutableStateOf("") }
    var danhMuc by remember { mutableStateOf("") }
    var ngayThang by remember { mutableStateOf("") }
    var nguonTien by remember { mutableStateOf("") }
    var ghiChu by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var expandedNguonTien by remember { mutableStateOf(false) }

    val danhMucList = listOf(
        DanhMucItem(R.drawable.ramen, "Ăn uống"),
        DanhMucItem(R.drawable.multimedia, "Giải trí"),
        DanhMucItem(R.drawable.bill, "Hóa đơn"),
        DanhMucItem(R.drawable.onlineshopping, "Chợ, siêu thị"),
        DanhMucItem(R.drawable.motorcycle, "Di chuyển"),
        DanhMucItem(R.drawable.ellipsis, "Khác")
    )

    val nguonTienList = listOf(
        NguonTienItem(R.drawable.cash, "Tiền mặt"),
        NguonTienItem(R.drawable.atm, "Ngân hàng")
    )

    // Lấy context để mở DatePickerDialog
    val context = LocalContext.current

    // Lấy ngày hiện tại
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    // Tạo DatePickerDialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            ngayThang = "$selectedDay/${selectedMonth + 1}/$selectedYear"
        }, year, month, day
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Image(
                        painter = painterResource(id = R.drawable.backbutton),
                        contentDescription = "Back",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text("Ghi Chép GD", fontSize = 25.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                thickness = 1.dp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                elevation = CardDefaults.elevatedCardElevation(4.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // chiếm toàn bộ không gian còn lại
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Tab Chi tiêu / Thu nhập
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Color.LightGray.copy(alpha = 0.8f), shape = RoundedCornerShape(50))
                            .padding(4.dp)
                    ) {
                        Button(
                            onClick = { selectedTab = "Chi tiêu" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == "Chi tiêu") Color.White else Color.LightGray.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Text("Chi tiêu", fontSize = 15.sp, color = Color.Black)
                        }

                        Button(
                            onClick = { selectedTab = "Thu nhập" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == "Chi tiêu") Color.LightGray.copy(alpha = 0.8f) else Color.White
                            ),
                            shape = RoundedCornerShape(50),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Text("Thu nhập", fontSize = 15.sp, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Các trường nhập
                    OutlinedTextField(
                        value = soTien,
                        onValueChange = { soTien = it },
                        label = requiredLabel("Số tiền"),
                        placeholder = { Text("Nhập số tiền...", fontSize = 20.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 18.sp),
                        shape = RoundedCornerShape(15.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = danhMuc,
                            onValueChange = {},
                            label = requiredLabel("Danh mục"),
                            placeholder = { Text("Chọn danh mục...", fontSize = 20.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            shape = RoundedCornerShape(15.dp),
                            textStyle = TextStyle(fontSize = 20.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Gray,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            danhMucList.forEach { item ->
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
                                        danhMuc = item.title
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    QuickCategoryButtons(onCategorySelected = { selectedCategory -> danhMuc = selectedCategory
                        expanded = false
                    })

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }
                    ) {
                        OutlinedTextField(
                            value = ngayThang,
                            onValueChange = {},
                            label = requiredLabel("Ngày giao dịch"),
                            placeholder = { Text("Chọn ngày...", fontSize = 20.sp) },
                            singleLine = true,
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 18.sp),
                            shape = RoundedCornerShape(15.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Gray,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedNguonTien,
                        onExpandedChange = { expandedNguonTien = !expandedNguonTien }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = nguonTien,
                            onValueChange = {},
                            label = requiredLabel("Nguồn tiền"),
                            placeholder = { Text("Chọn nguồn tiền...", fontSize = 20.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNguonTien)
                            },
                            shape = RoundedCornerShape(15.dp),
                            textStyle = TextStyle(fontSize = 20.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Gray,
                                unfocusedBorderColor = Color.LightGray
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

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = ghiChu,
                        onValueChange = { ghiChu = it },
                        label = { Text("Ghi chú", fontSize = 16.sp) },
                        placeholder = { Text("Nhập ghi chú giao dịch...", fontSize = 20.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle(fontSize = 18.sp),
                        shape = RoundedCornerShape(15.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green,
                    contentColor = Color.White
                )
            ) {
                Text("Thêm giao dịch chi tiêu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun requiredLabel(text: String): @Composable () -> Unit = {
    Text(
        buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Black)) {
                append("$text ")
            }
            withStyle(style = SpanStyle(color = Color.Red)) {
                append("*")
            }
        },
        fontSize = 16.sp
    )
}

@Composable
fun QuickCategoryButtons(onCategorySelected: (String) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryButton(R.drawable.ramen, "Ăn uống") { onCategorySelected("Ăn uống") }
            CategoryButton(R.drawable.onlineshopping, "Chợ, siêu thị") { onCategorySelected("Chợ, siêu thị") }
            CategoryButton(R.drawable.bill, "Hóa đơn") { onCategorySelected("Hóa đơn") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            CategoryButton(R.drawable.multimedia, "Giải trí") { onCategorySelected("Giải trí") }
        }
    }
}

@Composable
fun CategoryButton(iconId: Int, text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.LightGray.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(50),
        elevation = ButtonDefaults.buttonElevation(0.dp),
        border = null,
        modifier = Modifier
            .height(35.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = iconId),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, fontSize = 15.sp, color = Color.Black)
        }
    }
}
