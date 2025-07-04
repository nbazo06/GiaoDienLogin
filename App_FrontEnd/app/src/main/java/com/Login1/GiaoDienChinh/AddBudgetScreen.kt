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
    import androidx.compose.ui.text.TextRange
    import androidx.compose.ui.text.input.TextFieldValue
    import androidx.navigation.compose.rememberNavController
    import com.Login1.service.Category
    import com.Login1.service.Wallet
    import kotlinx.coroutines.withContext
    import java.text.DecimalFormat

    @Preview(showBackground = true)
    @Composable
    fun AddBudgetScreenPreview() {
        val navController = rememberNavController()
        AddBudgetScreen(navController = navController, user_id = "123")
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun AddBudgetScreen(navController: NavHostController, user_id: String) {
        var selectedTab by remember { mutableStateOf("Chi tiêu") }
        var transaction_type by remember { mutableStateOf("") }
        var soTienRaw by remember { mutableStateOf("") }
        var danhMucId by remember { mutableStateOf(0) }
        var nguonTien by remember { mutableStateOf("") }

        var selectedWalletId by remember { mutableStateOf(0) }
        var wallets by remember { mutableStateOf<List<Wallet>>(emptyList()) }
        var soTienText by remember { mutableStateOf(TextFieldValue(text = "", selection = TextRange(0))) }
        var danhMucHienThi by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }
        var expandedNguonTien by remember { mutableStateOf(false) }

        // Lấy danh mục từ backend
        var danhMucList by remember { mutableStateOf<List<Category>>(emptyList()) }
        LaunchedEffect(user_id) {
            CoroutineScope(Dispatchers.IO).launch {
                AuthService.getCategories(user_id).fold(
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
                AuthService.getWallets(user_id).fold(
                    onSuccess = { fetchedWallets ->
                        withContext(Dispatchers.Main) {
                            wallets = fetchedWallets
                        }
                    },
                    onFailure = { exception ->
                        withContext(Dispatchers.Main) {
                        }
                    }
                )
            }
        }

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
                ngayBatDau = "%02d-%02d-%d".format(selectedDay, selectedMonth + 1, selectedYear)
            }, year, month, day
        )

        // DatePicker cho ngày kết thúc
        val datePickerDialogKetThuc = DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                ngayKetThuc = "%02d-%02d-%d".format(selectedDay, selectedMonth + 1, selectedYear)
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
                        // Tab Chi tiêu
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


                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Nhập số tiền
                        OutlinedTextField(
                            value = soTienText,
                            onValueChange = { input ->
                                val digitsOnly = input.text.filter { it.isDigit() }

                                soTienRaw = digitsOnly

                                val formatted = DecimalFormat("#,###").format(digitsOnly.toLongOrNull() ?: 0).replace(",", ".")

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
                                            danhMucId = item.id
                                            danhMucHienThi = item.title
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        QuickCategoryButtons(
                            danhMucList = danhMucList,
                            onCategorySelected = { item ->
                                danhMucId = item.id
                                danhMucHienThi = item.title
                                expanded = false
                            }
                        )

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
                                wallets.forEach { item ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Image(
                                                    painter = painterResource(id = item.iconResid),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = item.name)
                                            }
                                        },
                                        onClick = {
                                            selectedWalletId = item.id
                                            nguonTien = item.name
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
                                    AuthService.addBudget(
                                        user_id,
                                        soTienRaw,
                                        danhMucId,
                                        ngayBatDau,
                                        ngayKetThuc,
                                        selectedWalletId
                                    ).fold(
                                        onSuccess = { response ->
                                            CoroutineScope(Dispatchers.Main).launch {
                                                if (response.getBoolean("success")) {
                                                    successMessage = "Thêm ngân sách thành công"
                                                    delay(500)
                                                    navController.navigate("home_screen/${user_id}") {
                                                        popUpTo("add_budget_screen") { inclusive = true }
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

