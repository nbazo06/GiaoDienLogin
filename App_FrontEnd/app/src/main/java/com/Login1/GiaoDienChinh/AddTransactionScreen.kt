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
import com.Login1.service.Wallet
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

@Preview(showBackground = true)
@Composable
fun AddTransactionScreenPreview() {
    val navController = rememberNavController()
    AddTransactionScreen(navController = navController, user_id = "123")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(navController: NavHostController, user_id: String) {
    var selectedTab by remember { mutableStateOf("Chi tiêu") }
    var transaction_type by remember { mutableStateOf("expense") }
    var soTienRaw by remember { mutableStateOf("") }
    var danhMuc by remember { mutableStateOf(0) }
    var ngayThang by remember { mutableStateOf("") }
    var nguonTien by remember { mutableStateOf("") }
    var ghiChu by remember { mutableStateOf("") }

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
    // Lọc danh mục theo loại
    val expenseCategories = danhMucList.filter { it.type == "expense" }
    val incomeCategories = danhMucList.filter { it.type == "income" }

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
                    navController.navigate("home_screen/${user_id}") {
                        launchSingleTop = true
                    }
                }) {
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
                            onClick = {
                                selectedTab = "Chi tiêu"
                                transaction_type = "expense"
                            },
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
                            onClick = {
                                selectedTab = "Thu nhập"
                                transaction_type = "income"
                                      },
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
                    val currentCategoryList = if (transaction_type == "expense") expenseCategories else incomeCategories
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
                            if (currentCategoryList.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Không có danh mục nào") },
                                    onClick = { expanded = false }
                                )
                            } else {
                                currentCategoryList.forEach { item ->
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
                        danhMucList = currentCategoryList,
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
                        AuthService.addTransaction(
                            user_id,
                            transaction_type,
                            soTienRaw,
                            danhMuc,
                            ngayThang,
                            selectedWalletId,
                            ghiChu)
                            .fold(
                            onSuccess = { response ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    if (response.getBoolean("success")) {

                                        delay(500)
                                        navController.navigate("home_screen/${user_id}") {
                                            popUpTo("add_transaction_screen") { inclusive = true }
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
fun QuickCategoryButtons(
    danhMucList: List<Category>,
    onCategorySelected: (Category) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hiển thị 3 danh mục đầu
            danhMucList.take(3).forEach { item ->
                CategoryButton(item.icon, item.title) {
                    onCategorySelected(item)
                }
            }
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
