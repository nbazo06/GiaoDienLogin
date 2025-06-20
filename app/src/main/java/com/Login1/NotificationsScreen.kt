package com.Login1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.Login1.GiaoDienChinh.BottomNavigationBar
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.Login1.GiaoDienLogin.R


// ==== MODEL PHÙ HỢP DATABASE ====
data class NotificationItem(
    val notificationID: Int,
    val accountID: Int,
    val userID: Int,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val sentAt: String
)

// ==== DỮ LIỆU GIẢ ====
val sampleNotifications = listOf(
    NotificationItem(
        notificationID = 1,
        accountID = 123,
        userID = 456,
        title = "Cảnh báo ngân sách",
        message = "Số tiền cho ngân sách Ăn uống đã vượt mức 80%",
        type = "Cảnh báo",
        isRead = false,
        sentAt = "2025-06-18 10:30:00"
    ),
    NotificationItem(
        notificationID = 2,
        accountID = 123,
        userID = 456,
        title = "Cảnh báo ngân sách",
        message = "Ngân sách Giải trí còn lại dưới 10%",
        type = "Cảnh báo",
        isRead = true,
        sentAt = "2025-06-17 09:15:00"
    ),
    NotificationItem(
        notificationID = 3,
        accountID = 123,
        userID = 456,
        title = "Giao dịch mới",
        message = "Đã thêm một giao dịch mới vào ngân sách Di chuyển",
        type = "Thông báo",
        isRead = true,
        sentAt = "2025-06-16 14:22:00"
    )
)

// ==== MÀN HÌNH HIỂN THỊ THÔNG BÁO ====
@Composable
fun NotificationScreen(
    navController: NavHostController,
    account_id: String,
    notifications: List<NotificationItem>
) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController, account_id) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Thông báo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(8.dp)
                    )

                    HorizontalDivider(thickness = 1.dp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(notifications) { index, notification ->
                            val backgroundColor = if (notification.isRead)
                                Color.White else Color(0xFFE3F2FD)

                            Card(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = backgroundColor)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = notification.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = notification.message,
                                        fontSize = 14.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${notification.sentAt}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    if (index < notifications.size - 1) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==== PREVIEW ====
@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    val navController = rememberNavController()
    NotificationScreen(
        navController = navController,
        account_id = "123",
        notifications = sampleNotifications
    )
}

fun sendLocalNotification(context: Context, title: String, message: String) {
    val channelId = "budget_channel"
    val notificationId = 1001

    // Tạo channel nếu cần
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Thông báo ngân sách",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Thông báo về giới hạn ngân sách"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    // Tạo thông báo
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.bell) // icon phải tồn tại trong drawable
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    //  Kiểm tra quyền trước khi gọi notify (API 33+ yêu cầu)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    } else {
        Log.e("NotifyError", "Thiếu quyền POST_NOTIFICATIONS, không gửi được thông báo.")
    }
}
