package com.Login1.GiaoDienChinh

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import java.time.LocalDate
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


data class CategoryBudget(
    val name: String,
    val iconResId: Int,
    val spent: Float,
    val total: Float
)

@Composable
fun BudgetTabSelector(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 0.dp,
        containerColor = Color.White,
        indicator = { tabPositions ->
            SecondaryIndicator(
                Modifier
                    .tabIndicatorOffset(tabPositions[selectedIndex])
                    .height(2.dp),
                color = Color.Black
            )
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        title,
                        fontSize = 14.sp,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedIndex == index) Color.Black else Color.Gray
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetScreenPreview() {
    val navController = rememberNavController()
    BudgetScreen(navController = navController, account_id = "123")
}

@Composable
fun InfoColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}
//
@Composable
fun DividerLine() {
    Box(
        modifier = Modifier
            .height(32.dp)
            .width(1.dp)
            .background(Color.LightGray)
    )
}

@Composable
fun CategoryBudgetCard(category: CategoryBudget) {
    val remaining = category.total - category.spent
    val percent = if (category.total > 0) category.spent / category.total else 0f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = category.iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color.Unspecified
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(category.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Còn lại ${String.format("%,.0f", remaining)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = String.format("%,.0f", category.total),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { percent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE0E0E0),
            )

            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
fun BudgetCard(
    categories: List<CategoryBudget>,
    endDate: LocalDate = LocalDate.of(2025, 6, 22)
) {
    val totalBudget = categories.sumOf { it.total.toDouble() }.toFloat()
    val totalSpent = categories.sumOf { it.spent.toDouble() }.toFloat()
    val remaining = totalBudget - totalSpent
    val percentUsed = if (totalBudget > 0) totalSpent / totalBudget else 0f
    val today = LocalDate.now()
    val daysLeft = ChronoUnit.DAYS.between(today, endDate).coerceAtLeast(0).toInt()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    val strokeWidth = 16.dp.toPx()
                    drawArc(
                        color = Color(0xFFEEEEEE),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFF4CAF50),
                        startAngle = 270f,
                        sweepAngle = 360f * percentUsed,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Số tiền bạn có thể chi",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = String.format("%,.0f", remaining),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InfoColumn(String.format("%,.0f", totalBudget), "Tổng ngân sách")
                DividerLine()
                InfoColumn(String.format("%,.0f", totalSpent), "Tổng đã chi")
                DividerLine()
                InfoColumn("$daysLeft ngày", "Từ hôm nay")
            }
        }
    }
}

@Composable
fun NguonTienBox(
    nguonTien: String,
    onNguonTienSelected: (String) -> Unit,
    nguonTienList: List<NguonTienItem>
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(12.dp)
            .clickable { expanded = true }
            .background(Color.White, shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = nguonTien.ifBlank { "Tổng cộng" },
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Icon(
                painter = painterResource(id = R.drawable.reorder), // icon tùy bạn
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .padding(start = 6.dp),
                tint = Color.Gray
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            nguonTienList.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = item.iconResid),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = item.ten)
                        }
                    },
                    onClick = {
                        onNguonTienSelected(item.ten)
                        expanded = false
                    }
                )
            }
        }
    }
}


fun String.toLocalDate(pattern: String = "dd/MM/yyyy"): LocalDate {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return LocalDate.parse(this, formatter)
}


//Anh lấy dữ liệu ngân sách từ bên Adđbudget nha

@Composable
fun BudgetScreen(navController: NavHostController, account_id: String) {

    var selectedTabIndex by remember { mutableStateOf(0) }
    var nguonTien by remember { mutableStateOf("Tổng cộng") }

    //Biến lấy các khoảng thời gian của các ngân sách
    val tabOptions = listOf("")

    var budgetItems by remember { mutableStateOf<List<BudgetItem>>(emptyList()) }
    val nguonTienList = listOf(
        NguonTienItem(R.drawable.cash, "Tiền mặt"),
        NguonTienItem(R.drawable.atm, "Ngân hàng")
    )

    // Gọi API khi vào màn
    LaunchedEffect(Unit) {
        budgetItems = fetchBudgets(account_id) // thay bằng hàm gọi API thật
    }

    val filteredBudgets = budgetItems.filter {
        nguonTien.isBlank() || it.source == nguonTien
    }

    val endDate = filteredBudgets.maxOfOrNull { it.endDate.toLocalDate() }
        ?: LocalDate.of(2025, 6, 22)

    val categoryBudgets = filteredBudgets.map {
        CategoryBudget(
            name = it.category,
            iconResId = it.iconResId,
            spent = it.spent,
            total = it.total
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController, account_id) },
            floatingActionButton = {
                Button(
                    onClick = {
                        navController.navigate("add_budget_screen/${account_id}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Text("Thêm ngân sách", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            containerColor = Color(0xFFE0E0E0)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE0E0E0))
                    .padding(paddingValues)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        NguonTienBox(
                            nguonTien = nguonTien,
                            onNguonTienSelected = { selected -> nguonTien = selected },
                            nguonTienList = nguonTienList
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        BudgetTabSelector(
                            tabs = tabOptions,
                            selectedIndex = selectedTabIndex,
                            onTabSelected = { selectedTabIndex = it }
                        )
                    }
                }

                Spacer(Modifier.height(15.dp))

                BudgetCard(categories = categoryBudgets, endDate = endDate)

                categoryBudgets.forEach { category ->
                    CategoryBudgetCard(category = category)
                }
            }
        }
    }
}

