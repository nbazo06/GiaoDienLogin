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
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import com.Login1.GiaoDienLogin.R
    import android.app.DatePickerDialog
    import android.widget.DatePicker
    import androidx.compose.foundation.clickable
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.tooling.preview.Preview
    import java.util.*
    import androidx.navigation.NavHostController
    import com.Login1.service.AuthService
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.delay
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.navigation.compose.rememberNavController

    data class DanhMucItem(
        val icon: Int,
        val title: String
    )

    data class NguonTienItem(
        val iconResid: Int,
        val ten: String
    )

    @Preview(showBackground = true)
    @Composable
    fun AddBudgetScreenPreview() {
        val navController = rememberNavController()
        AddBudgetScreen(navController = navController, account_id = "123")
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun AddBudgetScreen(navController: NavHostController, account_id: String) {
        var selectedTab by remember { mutableStateOf("Chi tiêu") }
        var transaction_type by remember { mutableStateOf("") }
        var soTien by remember { mutableStateOf("") }
        var danhMuc by remember { mutableStateOf("") }
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

        var ngayBatDau by remember { mutableStateOf("") }
        var ngayKetThuc by remember { mutableStateOf("") }

        // DatePicker cho ngày bắt đầu
        val datePickerDialogBatDau = DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                ngayBatDau = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            }, year, month, day
        )

        // DatePicker cho ngày kết thúc
        val datePickerDialogKetThuc = DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                ngayKetThuc = "$selectedDay/${selectedMonth + 1}/$selectedYear"
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
                    Text("Thêm ngân sách", fontSize = 25.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                var successMessage by remember { mutableStateOf<String?>(null) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                // Hiển thị thông báo thành công nếu có
                successMessage?.let { success ->
                    Text(
                        text = success,
                        color = Color.Green,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Hiển thị thông báo lỗi nếu có
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

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
                                .background(
                                    Color.LightGray.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(4.dp)
                        ) {
                            Button(
                                onClick = {
                                    selectedTab = "Chi tiêu"
                                    transaction_type = "Expense"
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedTab == "Chi tiêu") Color.White else Color.LightGray.copy(
                                        alpha = 0.8f
                                    )
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Text("Chi tiêu", fontSize = 15.sp, color = Color.Black)
                            }

                            Button(
                                onClick = {
                                    selectedTab = "Thu nhập"
                                    transaction_type = "Income"
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedTab == "Chi tiêu") Color.LightGray.copy(
                                        alpha = 0.8f
                                    ) else Color.White
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

                        // Nhập số tiền
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

                        // Dropdown danh mục
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

                        // Nút nhanh chọn danh mục
                        QuickCategoryButtons(onCategorySelected = { selectedCategory ->
                            danhMuc = selectedCategory
                            expanded = false
                        })

                        Spacer(modifier = Modifier.height(16.dp))

                        // Chọn ngày giao dịch
                        OutlinedTextField(
                            value = ngayBatDau,
                            onValueChange = {},
                            label = requiredLabel("Ngày bắt đầu"),
                            placeholder = { Text("Chọn ngày...", fontSize = 20.sp) },
                            singleLine = true,
                            readOnly = true,
                            enabled = false, // Disable TextField interaction
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialogBatDau.show() }, // Chỉ giữ một clickable modifier
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

                        // Ô Ngày kết thúc
                        OutlinedTextField(
                            value = ngayKetThuc,
                            onValueChange = {},
                            label = requiredLabel("Ngày kết thúc"),
                            placeholder = { Text("Chọn ngày...", fontSize = 20.sp) },
                            singleLine = true,
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialogKetThuc.show() },
                            textStyle = TextStyle(fontSize = 18.sp),
                            shape = RoundedCornerShape(15.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Gray,
                                unfocusedBorderColor = Color.LightGray,
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.LightGray,
                                disabledLabelColor = Color.Gray
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

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                // Reset messages
                                errorMessage = null
                                successMessage = null
                                CoroutineScope(Dispatchers.IO).launch {
                                    AuthService.addTransaction(
                                        account_id,
                                        transaction_type,
                                        soTien,
                                        danhMuc,
                                        ngayBatDau,
                                        nguonTien,
                                        ghiChu
                                    ).fold(
                                        onSuccess = { response ->
                                            CoroutineScope(Dispatchers.Main).launch {
                                                if (response.getBoolean("success")) {
                                                    successMessage = "Thêm giao dịch thành công"
                                                    delay(500)
                                                    navController.navigate("home_screen/${account_id}") {
                                                        popUpTo("add_transaction_screen") {
                                                            inclusive = true
                                                        }
                                                    }
                                                } else {
                                                    errorMessage = response.getString("message")
                                                }
                                            }
                                        },
                                        onFailure = { exception ->
                                            CoroutineScope(Dispatchers.Main).launch {
                                                errorMessage = exception.message ?: "Lỗi không xác định"
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
                            Text("Thêm ngân sách", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
        }

