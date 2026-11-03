package com.example.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.UserSettings
import com.example.util.CurrencyFormatter
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun SettingsScreen(
    userSettings: UserSettings,
    onUpdateBudget: (Double) -> Unit,
    onUpdateReminder: (hour: Int, minute: Int, enabled: Boolean) -> Unit,
    onUpdateDarkMode: (Boolean?) -> Unit,
    onExportData: () -> Unit,
    onSignOut: () -> Unit
) {
    val user = Firebase.auth.currentUser
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Account Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.displayName ?: "Người dùng Google",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user?.email ?: "Đã liên kết Google",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Cài đặt ứng dụng",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Setting Options
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                // Monthly Budget Item
                SettingRow(
                    icon = Icons.Default.Savings,
                    title = "Hạn mức ngân sách tháng",
                    subtitle = CurrencyFormatter.formatVnd(userSettings.monthlyBudget),
                    onClick = { showBudgetDialog = true }
                )

                // Dark Mode Item
                val currentModeLabel = when (userSettings.isDarkMode) {
                    true -> "Chế độ tối (Dark Mode)"
                    false -> "Chế độ sáng (Light Mode)"
                    null -> "Tự động theo hệ thống"
                }
                SettingRow(
                    icon = Icons.Default.DarkMode,
                    title = "Giao diện",
                    subtitle = currentModeLabel,
                    onClick = {
                        val nextMode = when (userSettings.isDarkMode) {
                            null -> true
                            true -> false
                            false -> null
                        }
                        onUpdateDarkMode(nextMode)
                    }
                )

                // Daily Reminder Item
                val reminderTimeStr = String.format("%02d:%02d", userSettings.reminderHour, userSettings.reminderMinute)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimeDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nhắc nhở chi tiêu hàng ngày",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (userSettings.isReminderEnabled) "Lúc $reminderTimeStr mỗi tối" else "Đang tắt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = userSettings.isReminderEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            onUpdateReminder(userSettings.reminderHour, userSettings.reminderMinute, enabled)
                        }
                    )
                }

                // Export to CSV Item
                SettingRow(
                    icon = Icons.Default.FileDownload,
                    title = "Xuất dữ liệu Excel / PDF (CSV)",
                    subtitle = "Sao lưu danh sách thu chi, chia phòng & nợ",
                    onClick = onExportData
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Out Button
        Button(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("sign_out_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đăng xuất tài khoản", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Chi Tiêu Pro • v1.0.0 • Dữ liệu lưu an toàn trên Firestore",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Dialog: Edit Monthly Budget
    if (showBudgetDialog) {
        var budgetText by remember { mutableStateOf(userSettings.monthlyBudget.toLong().toString()) }
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("Đặt hạn mức ngân sách tháng") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Ứng dụng sẽ cảnh báo khi bạn chi tiêu vượt quá mức này.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { input -> budgetText = input.filter { it.isDigit() } },
                        label = { Text("Số tiền (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (budgetText.isNotEmpty()) {
                        val parsed = budgetText.toDoubleOrNull() ?: 0.0
                        Text(
                            text = "= ${CurrencyFormatter.formatVnd(parsed)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = budgetText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onUpdateBudget(amount)
                        }
                        showBudgetDialog = false
                    }
                ) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) { Text("Hủy") }
            }
        )
    }

    // Dialog: Edit Reminder Time
    if (showTimeDialog) {
        var hourText by remember { mutableStateOf(userSettings.reminderHour.toString()) }
        var minuteText by remember { mutableStateOf(userSettings.reminderMinute.toString()) }

        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = { Text("Giờ nhắc nhở hàng ngày") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Chọn thời điểm mỗi tối ứng dụng sẽ gửi thông báo nhắc bạn ghi lại chi tiêu và tiền đi chợ:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = hourText,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                val num = filtered.toIntOrNull()
                                if (filtered.isEmpty() || (num != null && num in 0..23)) {
                                    hourText = filtered
                                }
                            },
                            label = { Text("Giờ (0-23)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minuteText,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                val num = filtered.toIntOrNull()
                                if (filtered.isEmpty() || (num != null && num in 0..59)) {
                                    minuteText = filtered
                                }
                            },
                            label = { Text("Phút (0-59)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val h = hourText.toIntOrNull() ?: 21
                        val m = minuteText.toIntOrNull() ?: 0
                        onUpdateReminder(h, m, true)
                        showTimeDialog = false
                    }
                ) {
                    Text("Cài đặt")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDialog = false }) { Text("Hủy") }
            }
        )
    }
}

@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
