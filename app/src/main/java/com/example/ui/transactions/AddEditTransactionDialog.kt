package com.example.ui.transactions

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.model.ExpenseCategory
import com.example.model.TransactionType
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, type: TransactionType, category: String, note: String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

    val isImeVisible = WindowInsets.isImeVisible
    val imeBottom = WindowInsets.ime.getBottom(density)
    val isKeyboardOpen by rememberUpdatedState(isImeVisible || imeBottom > 0)
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
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD.displayName) }
    var note by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    // Danh sách mục danh mục theo loại Chi hoặc Thu
    val visibleCategories = remember(selectedType) {
        if (selectedType == TransactionType.EXPENSE) {
            listOf(
                ExpenseCategory.FOOD,
                ExpenseCategory.SHOPPING,
                ExpenseCategory.HOUSING,
                ExpenseCategory.BILLS,
                ExpenseCategory.TRANSPORT,
                ExpenseCategory.ENTERTAINMENT,
                ExpenseCategory.HEALTH,
                ExpenseCategory.OTHER
            )
        } else {
            listOf(
                ExpenseCategory.SALARY,
                ExpenseCategory.OTHER
            )
        }
    }

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
                    // Nếu bấm ra ngoài khi bàn phím đang mở thì chỉ ẩn phím trước, tránh mất nội dung vừa gõ
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
        )

        // Bề mặt Form popup (Surface)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(top = 36.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // Chạm vào khoảng trống bên trong form thì ẩn bàn phím nếu đang mở, không đóng form
                    if (isKeyboardOpen || isAnyFieldFocused) {
                        keyboardController?.hide()
                        focusManager.clearFocus(force = true)
                        isAnyFieldFocused = false
                    }
                }
                .imePadding()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Thanh gạt chỉ thị
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ghi chép giao dịch",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus(force = true)
                        onDismiss()
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Chi tiêu / Thu nhập
                TabRow(
                    selectedTabIndex = if (selectedType == TransactionType.EXPENSE) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = {
                            selectedType = TransactionType.EXPENSE
                            selectedCategory = ExpenseCategory.FOOD.displayName
                        },
                        text = { Text("Khoản chi (-)") }
                    )
                    Tab(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = {
                            selectedType = TransactionType.INCOME
                            selectedCategory = ExpenseCategory.SALARY.displayName
                        },
                        text = { Text("Khoản thu (+)") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nhập số tiền
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        amountText = filtered
                    },
                    label = { Text("Số tiền (VNĐ)") },
                    placeholder = { Text("Ví dụ: 50000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                isAnyFieldFocused = true
                            }
                        }
                        .testTag("transaction_amount_input")
                )

                if (amountText.isNotEmpty()) {
                    val parsed = amountText.toDoubleOrNull() ?: 0.0
                    Text(
                        text = "= ${CurrencyFormatter.formatVnd(parsed)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                // Các nút chọn nhanh số tiền
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10000, 50000, 100000, 500000).forEach { quickVal ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val current = amountText.toDoubleOrNull() ?: 0.0
                                    amountText = (current + quickVal).toLong().toString()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "+${quickVal / 1000}k",
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .align(Alignment.CenterHorizontally),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (amountText.isNotEmpty()) {
                                    amountText += "000"
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "+000",
                            modifier = Modifier
                                .padding(vertical = 6.dp)
                                .align(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nhập tiêu đề
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề / Tên mục chi thu") },
                    placeholder = { Text("Ví dụ: Ăn trưa, Cà phê, Mua sách") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                isAnyFieldFocused = true
                            }
                        }
                        .testTag("transaction_title_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Danh mục dạng FlowRow: hiển thị đầy đủ, bấm chọn tiện lợi, không lo vuốt bị tắt popup
                Text(
                    text = "Danh mục",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    visibleCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.displayName,
                            onClick = {
                                selectedCategory = cat.displayName
                                if (isKeyboardOpen || isAnyFieldFocused) {
                                    keyboardController?.hide()
                                    focusManager.clearFocus(force = true)
                                    isAnyFieldFocused = false
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = getCategoryIcon(cat.displayName),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(cat.displayName, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ghi chú
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú (Tùy chọn)") },
                    placeholder = { Text("Thêm chi tiết giao dịch nếu cần...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                isAnyFieldFocused = true
                            }
                        }
                )

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorText ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Nút Lưu giao dịch
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount == null || amount <= 0.0) {
                            errorText = "Vui lòng nhập số tiền hợp lệ (> 0)"
                            return@Button
                        }
                        if (title.isBlank()) {
                            errorText = "Vui lòng nhập tiêu đề giao dịch"
                            return@Button
                        }
                        keyboardController?.hide()
                        focusManager.clearFocus(force = true)
                        isAnyFieldFocused = false
                        onSave(title.trim(), amount, selectedType, selectedCategory, note.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_transaction_button"),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Lưu giao dịch",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
