package com.example.ui.urgentdebts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.zIndex
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.UrgentDebt
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.UrgentDebtRed
import com.example.util.CurrencyFormatter
import com.google.firebase.Timestamp
import java.util.Calendar

@Composable
fun UrgentDebtScreen(
    urgentDebts: List<UrgentDebt>,
    onAddUrgentDebt: (UrgentDebt) -> Unit,
    onToggleSettled: (debtId: String, currentSettled: Boolean) -> Unit,
    onDeleteUrgentDebt: (debtId: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Tôi cần trả ngay, 1: Người khác nợ tôi
    var showAddDialog by remember { mutableStateOf(false) }
    var debtToDelete by remember { mutableStateOf<UrgentDebt?>(null) }

    val isPayableTab = selectedTab == 0
    val filteredDebts = remember(urgentDebts, isPayableTab) {
        urgentDebts.filter { it.isPayable == isPayableTab }
    }

    val unsettledTotal = remember(filteredDebts) {
        filteredDebts.filter { !it.isSettled }.sumOf { it.amount }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                modifier = Modifier.testTag("add_urgent_debt_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm nợ khẩn cấp")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Tabs: Payable vs Receivable
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Tôi cần trả ngay") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Người khác nợ tôi") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (unsettledTotal > 0) UrgentDebtRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (unsettledTotal > 0) UrgentDebtRed else PaidGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (unsettledTotal > 0) Icons.Default.NotificationImportant else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (isPayableTab) "Tổng nợ khẩn cấp cần trả:" else "Tổng tiền cần thu hồi gấp:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatVnd(unsettledTotal),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (unsettledTotal > 0) UrgentDebtRed else PaidGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredDebts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PaidGreen.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isPayableTab) "Không có khoản nợ nào cần thanh toán gấp!" else "Không có ai nợ bạn khoản nào khẩn cấp!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredDebts, key = { it.id }) { debt ->
                        UrgentDebtItemCard(
                            debt = debt,
                            onToggleSettled = { onToggleSettled(debt.id, debt.isSettled) },
                            onDelete = { debtToDelete = debt }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddUrgentDebtBottomSheet(
            defaultIsPayable = isPayableTab,
            onDismiss = { showAddDialog = false },
            onSave = { debt ->
                onAddUrgentDebt(debt)
                showAddDialog = false
            }
        )
    }

    debtToDelete?.let { debt ->
        AlertDialog(
            onDismissRequest = { debtToDelete = null },
            title = { Text("Xác nhận xóa khoản nợ?") },
            text = { Text("Bạn có chắc chắn muốn xóa '${debt.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteUrgentDebt(debt.id)
                        debtToDelete = null
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { debtToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun UrgentDebtItemCard(
    debt: UrgentDebt,
    onToggleSettled: () -> Unit,
    onDelete: () -> Unit
) {
    val isSettled = debt.isSettled

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSettled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSettled) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleSettled) {
                Icon(
                    imageVector = if (isSettled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Chuyển trạng thái",
                    tint = if (isSettled) PaidGreen else UrgentDebtRed,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = debt.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSettled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                if (debt.contactName.isNotBlank()) {
                    Text(
                        text = "Liên hệ: ${debt.contactName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (debt.dueDate != null) {
                    Text(
                        text = "Hạn chót: ${CurrencyFormatter.formatDateOnly(debt.dueDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSettled) MaterialTheme.colorScheme.onSurfaceVariant else UrgentDebtRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatVnd(debt.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSettled) PaidGreen else UrgentDebtRed
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xóa",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddUrgentDebtBottomSheet(
    defaultIsPayable: Boolean,
    onDismiss: () -> Unit,
    onSave: (UrgentDebt) -> Unit
) {
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible
    val imeBottom = WindowInsets.ime.getBottom(density)
    val isKeyboardOpen by androidx.compose.runtime.rememberUpdatedState(isImeVisible || imeBottom > 0)
    var isAnyFieldFocused by remember { mutableStateOf(false) }

    // Xử lý cử chỉ vuốt trở về (Back gesture / Back button):
    // Khi bàn phím đang mở hoặc đang focus ô nhập liệu: chỉ ẩn bàn phím và huỷ focus, giữ nguyên form!
    // Chỉ khi bàn phím đã tắt hoàn toàn và không còn focus thì cử chỉ vuốt back tiếp theo mới đóng form.
    BackHandler(enabled = true) {
        if (isKeyboardOpen || isAnyFieldFocused) {
            keyboardController?.hide()
            focusManager.clearFocus(force = true)
            isAnyFieldFocused = false
        } else {
            keyboardController?.hide()
            focusManager.clearFocus(force = true)
            onDismiss()
        }
    }

    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var isPayable by remember { mutableStateOf(defaultIsPayable) }
    var daysUntilDue by remember { mutableStateOf("0") } // 0: Hôm nay, 1: Ngày mai, 3: 3 ngày, 7: 1 tuần

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f)
    ) {
        // Màn nền mờ phía sau (Scrim backdrop)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (isKeyboardOpen || isAnyFieldFocused) {
                        keyboardController?.hide()
                        focusManager.clearFocus(force = true)
                        isAnyFieldFocused = false
                    } else {
                        onDismiss()
                    }
                }
        )

        // Animated Bottom Sheet Surface
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Consume clicks inside the sheet so tapping form never dismisses
                        if (isKeyboardOpen) {
                            keyboardController?.hide()
                            focusManager.clearFocus(force = true)
                            isAnyFieldFocused = false
                        }
                    }
                    .imePadding()
                    .navigationBarsPadding()
                    .heightIn(max = 680.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Thanh gạt thẩm mỹ đầu form (Drag handle bar)
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Thêm khoản nợ khẩn cấp",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Type Toggle Tabs
                    TabRow(
                        selectedTabIndex = if (isPayable) 0 else 1,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Tab(
                            selected = isPayable,
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus(force = true)
                                isAnyFieldFocused = false
                                isPayable = true
                            },
                            text = { Text("Tôi cần trả gấp") }
                        )
                        Tab(
                            selected = !isPayable,
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus(force = true)
                                isAnyFieldFocused = false
                                isPayable = false
                            },
                            text = { Text("Cần đòi gấp") }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Tiêu đề khoản nợ") },
                        placeholder = { Text("Ví dụ: Tiền phòng tháng này, Trả nợ Nam") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("urgent_debt_title_input")
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    isAnyFieldFocused = true
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { input -> amountText = input.filter { it.isDigit() } },
                        label = { Text("Số tiền (VNĐ)") },
                        placeholder = { Text("Ví dụ: 1500000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("urgent_debt_amount_input")
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    isAnyFieldFocused = true
                                }
                            }
                    )

                    if (amountText.isNotEmpty()) {
                        val parsed = amountText.toDoubleOrNull() ?: 0.0
                        Text(
                            text = "= ${CurrencyFormatter.formatVnd(parsed)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Contact Name
                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        label = { Text("Người liên hệ / Chủ nợ / Người vay") },
                        placeholder = { Text("Ví dụ: Chủ trọ cô Hoa, Anh Nam...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    isAnyFieldFocused = true
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Due Date Selection
                    Text(
                        text = "Hạn thanh toán:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("0" to "Hôm nay", "1" to "Ngày mai", "3" to "3 ngày tới", "7" to "1 tuần").forEach { (days, label) ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        keyboardController?.hide()
                                        focusManager.clearFocus(force = true)
                                        isAnyFieldFocused = false
                                        daysUntilDue = days
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (daysUntilDue == days) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (daysUntilDue == days) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val canSubmit = title.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0.0

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus(force = true)
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            val cal = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, daysUntilDue.toIntOrNull() ?: 0)
                            }
                            val dueTimestamp = Timestamp(cal.time)

                            val debt = UrgentDebt(
                                title = title.trim(),
                                amount = amount,
                                isPayable = isPayable,
                                contactName = contactName.trim(),
                                dueDate = dueTimestamp,
                                isSettled = false
                            )
                            onSave(debt)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_urgent_debt_button"),
                        shape = RoundedCornerShape(25.dp),
                        enabled = canSubmit,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Lưu khoản nợ khẩn cấp", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
