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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Login1.GiaoDienLogin.R
import android.app.DatePickerDialog
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import java.util.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.Login1.service.AuthService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.Login1.service.Category
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

@Preview(showBackground = true)
@Composable
fun ModifyTransactionScreenPreview() {
    val navController = rememberNavController()
    ModifyTransactionScreen(navController = navController, account_id = "1", transaction_id = "123")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyTransactionScreen(navController: NavHostController, account_id: String, transaction_id: String) {
    var soTienRaw by remember { mutableStateOf("") }
    var transaction_type by remember { mutableStateOf("") }
    var danhMuc by remember { mutableStateOf("") }
    var ngayThang by remember { mutableStateOf("") }
    var nguonTien by remember { mutableStateOf("") }
    var ghiChu by remember { mutableStateOf("") }

    var soTienText by remember { mutableStateOf(TextFieldValue(text = "", selection = TextRange(0))) }
    var danhMucHienThi by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var expandedNguonTien by remember { mutableStateOf(false) }

    // Lấy danh mục từ backend
    var danhMucList by remember { mutableStateOf<List<Category>>(emptyList()) }
    LaunchedEffect(transaction_id) {
        CoroutineScope(Dispatchers.IO).launch {
            AuthService.getCategories(transaction_id).fold(
                onSuccess = { fetchedCategories ->
                    withContext(Dispatchers.Main) {
                        danhMucList = fetchedCategories
                        Log.d("AddTransactionScreen", "Danh mục: $danhMucList")
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
            ngayThang = "%02d-%02d-%d".format(selectedDay, selectedMonth + 1, selectedYear)
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
                IconButton(onClick = {
                    navController.navigate("transaction_history_screen/${account_id}") {
                        launchSingleTop = true
                    }
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.backbutton),
                        contentDescription = "Back",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text("Sửa giao dịch", fontSize = 25.sp)
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
                    // Hiển thị loại giao dịch (không cho chỉnh sửa)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (transaction_type == "expense") "Chi tiêu" else "Thu nhập",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier
                                .background(Color.LightGray, shape = RoundedCornerShape(25.dp))
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }


                    Spacer(modifier = Modifier.height(20.dp))

                    // Nhập số tiền
                    OutlinedTextField(
                        value = soTienText,
                        onValueChange = { input ->
                            val digitsOnly = input.text.filter { it.isDigit() }

                            soTienRaw = digitsOnly

                            val formatted = DecimalFormat("#,###").format(digitsOnly).replace(",", ".")

                            // Cập nhật TextFieldValue và đặt dấu nháy về cuối
                            soTienText = TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length)
                            )
                        },
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

                    // Dropdown danh mục
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = danhMucHienThi,
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
                            if (danhMucList.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Không có danh mục nào") },
                                    onClick = { expanded = false }
                                )
                            } else {
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
                                            danhMuc = item.id
                                            danhMucHienThi = item.title
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    QuickCategoryButtons(
                        danhMucList = danhMucList,
                        onCategorySelected = { item ->
                            danhMuc = item.id
                            danhMucHienThi = item.title
                            expanded = false
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Chọn ngày giao dịch
                    OutlinedTextField(
                        value = ngayThang,
                        onValueChange = {},
                        label = requiredLabel("Ngày giao dịch"),
                        placeholder = { Text("Chọn ngày...", fontSize = 20.sp) },
                        singleLine = true,
                        readOnly = true,
                        enabled = false, // Disable TextField interaction
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }, // Chỉ giữ một clickable modifier
                        textStyle = TextStyle(fontSize = 18.sp),
                        shape = RoundedCornerShape(15.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray,
                            disabledTextColor = Color.Black, // Giữ màu text khi disabled
                            disabledBorderColor = Color.LightGray, // Giữ màu border khi disabled
                            disabledLabelColor = Color.Gray // Giữ màu label khi disabled
                        )
                    )

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
                                                painter = painterResource(id = item.iconResid),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = item.ten)
                                        }
                                    },
                                    onClick = {
                                        nguonTien = item.ten
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
                onClick = {

                    CoroutineScope(Dispatchers.IO).launch {
                        AuthService.updateTransaction(transaction_id, soTienRaw, transaction_type, danhMuc, ngayThang, nguonTien, ghiChu).fold(
                            onSuccess = { response ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    if (response.getBoolean("success")) {
                                        delay(500)
                                        navController.navigate("transaction_history_screen/${account_id}") {
                                            popUpTo("modify_transaction_screen") { inclusive = true }
                                        }
                                    }
                                }
                            },

                            onFailure = { exception ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    //errorMessage = exception.message ?: "Lỗi không xác định"
                                }
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green,
                    contentColor = Color.White
                )
            ) {
                Text("Sửa giao dịch", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}



