package com.example.ui.roomsplit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.zIndex
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.model.RoomMember
import com.example.model.RoomSplit
import com.example.ui.theme.OwedOrange
import com.example.ui.theme.PaidGreen
import com.example.util.CurrencyFormatter

@Composable
fun RoomSplitScreen(
    roomMembers: List<RoomMember>,
    roomSplits: List<RoomSplit>,
    onAddMember: (name: String, phone: String, color: Long) -> Unit,
    onDeleteMember: (String) -> Unit,
    onSettleDebt: (memberId: String, amount: Double, currentOwed: Double) -> Unit,
    onAddSplitExpense: (title: String, totalAmount: Double, participantIds: List<String>, includeMe: Boolean, syncPersonal: Boolean) -> Unit,
    onDeleteSplitExpense: (RoomSplit) -> Unit
) {
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showAddSplitSheet by remember { mutableStateOf(false) }
    var selectedMemberForSettle by remember { mutableStateOf<RoomMember?>(null) }
    var selectedMemberForDetail by remember { mutableStateOf<RoomMember?>(null) }
    var splitToDelete by remember { mutableStateOf<RoomSplit?>(null) }
    val expandedMemberMap = remember { mutableStateMapOf<String, Boolean>() }

    val totalOwedToMe = remember(roomMembers) {
        roomMembers.sumOf { it.totalOwed }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSplitSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_room_split_fab")
            ) {
                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Mua đồ chia phòng")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Total Room Debts Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sổ nợ phòng trọ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = "${roomMembers.size} thành viên",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Tổng tiền phòng đang nợ bạn:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = CurrencyFormatter.formatVnd(totalOwedToMe),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Room Members List Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thành viên phòng",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showAddMemberDialog = true }) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Thêm người")
                    }
                }

                if (roomMembers.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = "Chưa có thành viên nào. Hãy thêm các bạn cùng phòng để chia tiền đi chợ!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(roomMembers, key = { it.id }) { member ->
                            val memberSplits = remember(roomSplits, member.id) {
                                roomSplits.filter { it.participantIds.contains(member.id) }
                            }
                            MemberChipCard(
                                member = member,
                                splitsCount = memberSplits.size,
                                onClick = { selectedMemberForDetail = member }
                            )
                        }
                    }
                }
            }

            // Detailed Debt Breakdown Per Member Section: shows which orders make up each person's debt
            if (roomMembers.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chi tiết ghi nợ từng người",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bấm để xem các đơn tính nợ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(roomMembers, key = { "breakdown_${it.id}" }) { member ->
                    val memberSplits = remember(roomSplits, member.id) {
                        roomSplits.filter { it.participantIds.contains(member.id) }
                    }
                    val isExpanded = expandedMemberMap[member.id] ?: (member.totalOwed > 0)
                    MemberDebtDetailCard(
                        member = member,
                        splits = memberSplits,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedMemberMap[member.id] = !isExpanded
                        },
                        onSettleClick = {
                            selectedMemberForSettle = member
                        },
                        onDetailClick = {
                            selectedMemberForDetail = member
                        }
                    )
                }
            }

            // Room Split History Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lịch sử mua đồ chia phòng",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showAddSplitSheet = true },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("add_room_split_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Đi chợ")
                    }
                }
            }

            if (roomSplits.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Chưa có đợt mua đồ ăn nào",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Khi đi chợ về, hãy nhấn '+ Đi chợ' để nhập tiền và tick chọn người tham gia!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            } else {
                items(roomSplits, key = { it.id }) { split ->
                    RoomSplitItemCard(
                        split = split,
                        roomMembers = roomMembers,
                        onDelete = { splitToDelete = split }
                    )
                }
            }
        }
    }

    // Dialog: Add Room Member
    if (showAddMemberDialog) {
        AddRoomMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onAdd = { name, phone, color ->
                onAddMember(name, phone, color)
                showAddMemberDialog = false
            }
        )
    }

    // Modal Bottom Sheet: Add Room Split Expense
    if (showAddSplitSheet) {
        AddRoomSplitBottomSheet(
            roomMembers = roomMembers,
            onDismiss = { showAddSplitSheet = false },
            onSave = { title, total, participantIds, includeMe, syncPersonal ->
                onAddSplitExpense(title, total, participantIds, includeMe, syncPersonal)
                showAddSplitSheet = false
            }
        )
    }

    // Dialog: Member Debt Detail (shows exactly which orders make up this member's debt)
    selectedMemberForDetail?.let { member ->
        val memberSplits = remember(roomSplits, member.id) {
            roomSplits.filter { it.participantIds.contains(member.id) }
        }
        MemberDebtDetailDialog(
            member = member,
            splits = memberSplits,
            onDismiss = { selectedMemberForDetail = null },
            onSettle = { amount ->
                onSettleDebt(member.id, amount, member.totalOwed)
                selectedMemberForDetail = null
            },
            onDeleteMember = {
                onDeleteMember(member.id)
                selectedMemberForDetail = null
            }
        )
    }

    // Dialog: Settle Member Debt
    selectedMemberForSettle?.let { member ->
        val memberSplits = remember(roomSplits, member.id) {
            roomSplits.filter { it.participantIds.contains(member.id) }
        }
        SettleDebtDialog(
            member = member,
            memberSplits = memberSplits,
            onDismiss = { selectedMemberForSettle = null },
            onSettle = { amount ->
                onSettleDebt(member.id, amount, member.totalOwed)
                selectedMemberForSettle = null
            },
            onDeleteMember = {
                onDeleteMember(member.id)
                selectedMemberForSettle = null
            }
        )
    }

    // Dialog: Confirm Delete Split
    splitToDelete?.let { split ->
        AlertDialog(
            onDismissRequest = { splitToDelete = null },
            title = { Text("Xác nhận xóa đợt chia tiền?") },
            text = { Text("Thao tác này sẽ xóa bản ghi '${split.title}' và hoàn trả lại tiền nợ tương ứng của các thành viên.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSplitExpense(split)
                        splitToDelete = null
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { splitToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun MemberChipCard(
    member: RoomMember,
    splitsCount: Int = 0,
    onClick: () -> Unit
) {
    val initial = member.name.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
    val isOwed = member.totalOwed > 0

    Card(
        modifier = Modifier
            .width(145.dp)
            .clickable { onClick() }
            .testTag("member_chip_${member.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(member.avatarColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = member.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (isOwed) "Nợ: ${CurrencyFormatter.formatVnd(member.totalOwed)}" else "Hết nợ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isOwed) OwedOrange else PaidGreen
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (splitsCount > 0) "$splitsCount đơn tính nợ" else "0 đơn",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun MemberDebtDetailCard(
    member: RoomMember,
    splits: List<RoomSplit>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSettleClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    val initial = member.name.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
    val isOwed = member.totalOwed > 0
    val totalFromSplits = remember(splits) { splits.sumOf { it.splitAmountPerPerson } }
    val totalPaid = remember(totalFromSplits, member.totalOwed) {
        (totalFromSplits - member.totalOwed).coerceAtLeast(0.0)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("member_debt_card_${member.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Avatar, Name, Debt status, Expand toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(member.avatarColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (member.phone.isNotBlank()) {
                            Text(
                                text = member.phone,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${splits.size} đơn chia tiền",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isOwed) CurrencyFormatter.formatVnd(member.totalOwed) else "0 đ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isOwed) OwedOrange else PaidGreen
                        )
                        Text(
                            text = if (isOwed) "Đang nợ" else "Đã hết nợ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isOwed) OwedOrange else PaidGreen
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Thu gọn" else "Xem chi tiết đơn nợ",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Expanded Breakdown: Shows EXACTLY which orders contribute to this debt
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Số nợ được tính từ các đơn (${splits.size} đơn):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Phần ${member.name} chịu",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (splits.isEmpty()) {
                    Text(
                        text = "${member.name} chưa tham gia đơn chia tiền nào.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        splits.forEach { split ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Receipt,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = split.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${CurrencyFormatter.formatDate(split.date)} • Tổng đơn ${CurrencyFormatter.formatVnd(split.totalAmount)} (${split.participantIds.size + 1} người)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "+${CurrencyFormatter.formatVnd(split.splitAmountPerPerson)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = OwedOrange
                                        )
                                        Text(
                                            text = "chia đều",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Summary Calculation Box: Total from splits - Total Paid = Remaining Debt
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tổng tiền từ ${splits.size} đơn:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyFormatter.formatVnd(totalFromSplits),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (totalPaid > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Đã cấn trừ / thanh toán:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PaidGreen
                                    )
                                    Text(
                                        text = "-${CurrencyFormatter.formatVnd(totalPaid)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PaidGreen
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Số nợ hiện tại cần thu:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = CurrencyFormatter.formatVnd(member.totalOwed),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isOwed) OwedOrange else PaidGreen
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDetailClick) {
                        Text("Xem sổ nợ đầy đủ")
                    }
                    if (isOwed) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onSettleClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Thu nợ")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoomSplitItemCard(
    split: RoomSplit,
    roomMembers: List<RoomMember>,
    onDelete: () -> Unit
) {
    val memberMap = remember(roomMembers) { roomMembers.associateBy { it.id } }
    val participantNames = remember(split.participantIds, memberMap) {
        split.participantIds.mapNotNull { memberMap[it]?.name }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = split.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = CurrencyFormatter.formatDate(split.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xóa",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Tổng chi tiền chợ:", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = CurrencyFormatter.formatVnd(split.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${split.participantIds.size + 1} người ăn",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "Mỗi người: ${CurrencyFormatter.formatVnd(split.splitAmountPerPerson)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            if (participantNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Người tham gia: Tôi, ${participantNames.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddRoomMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, color: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val colors = listOf(0xFF00685FL, 0xFF855400L, 0xFF006782L, 0xFF4A6572L, 0xFF8E24AAL, 0xFFE65100L)
    var selectedColor by remember { mutableStateOf(colors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm thành viên phòng") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ tên / Biệt danh") },
                    placeholder = { Text("Ví dụ: Nam, Tuấn, Trang") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("member_name_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại (Tùy chọn)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Chọn màu đại diện:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .border(
                                    width = if (selectedColor == c) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = c }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name.trim(), phone.trim(), selectedColor)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_member_button")
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRoomSplitBottomSheet(
    roomMembers: List<RoomMember>,
    onDismiss: () -> Unit,
    onSave: (title: String, totalAmount: Double, participantIds: List<String>, includeMe: Boolean, syncPersonal: Boolean) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current

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
    var totalAmountText by remember { mutableStateOf("") }
    var includeMe by remember { mutableStateOf(true) }
    var syncPersonal by remember { mutableStateOf(true) }

    // Map memberId -> Boolean (true if checked)
    val checkedMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            roomMembers.forEach { put(it.id, true) }
        }
    }

    val totalSelectedMembersCount = checkedMap.count { it.value }
    val totalPeople = totalSelectedMembersCount + if (includeMe) 1 else 0

    val totalAmount = totalAmountText.toDoubleOrNull() ?: 0.0
    val splitAmountPerPerson = if (totalPeople > 0) totalAmount / totalPeople else 0.0

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
                            text = "Ghi chép tiền đi chợ phòng",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Tên đồ ăn / hàng mua") },
                        placeholder = { Text("Ví dụ: Thịt bò + rau củ + gia vị trưa nay") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("split_title_input")
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    isAnyFieldFocused = true
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Total Amount
                    OutlinedTextField(
                        value = totalAmountText,
                        onValueChange = { input -> totalAmountText = input.filter { it.isDigit() } },
                        label = { Text("Tổng số tiền bạn đã chi (VNĐ)") },
                        placeholder = { Text("Ví dụ: 180000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("split_amount_input")
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    isAnyFieldFocused = true
                                }
                            }
                    )

                    if (totalAmountText.isNotEmpty()) {
                        Text(
                            text = "= ${CurrencyFormatter.formatVnd(totalAmount)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    // Quick Amount Buttons
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(50000, 100000, 200000).forEach { quickVal ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        keyboardController?.hide()
                                        focusManager.clearFocus(force = true)
                                        isAnyFieldFocused = false
                                        val current = totalAmountText.toDoubleOrNull() ?: 0.0
                                        totalAmountText = (current + quickVal).toLong().toString()
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "+${quickVal / 1000}k",
                                    modifier = Modifier.padding(vertical = 6.dp).align(Alignment.CenterHorizontally),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    keyboardController?.hide()
                                    focusManager.clearFocus(force = true)
                                    isAnyFieldFocused = false
                                    if (totalAmountText.isNotEmpty()) totalAmountText += "000"
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "+000",
                                modifier = Modifier.padding(vertical = 6.dp).align(Alignment.CenterHorizontally),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Checkbox: Include Me
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                keyboardController?.hide()
                                focusManager.clearFocus(force = true)
                                isAnyFieldFocused = false
                                includeMe = !includeMe
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeMe,
                            onCheckedChange = {
                                keyboardController?.hide()
                                focusManager.clearFocus(force = true)
                                isAnyFieldFocused = false
                                includeMe = it
                            },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "Bao gồm cả tôi (Người chi tiền)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Roommates Checkbox List
                    Text(
                        text = "Chọn ai tham gia ăn / chi trả:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (roomMembers.isEmpty()) {
                        Text(
                            text = "Chưa có thành viên nào trong phòng. Vui lòng thêm thành viên ở màn hình chính trước.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                roomMembers.forEach { member ->
                                    val isChecked = checkedMap[member.id] ?: false
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                keyboardController?.hide()
                                                focusManager.clearFocus(force = true)
                                                isAnyFieldFocused = false
                                                checkedMap[member.id] = !isChecked
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = {
                                                keyboardController?.hide()
                                                focusManager.clearFocus(force = true)
                                                isAnyFieldFocused = false
                                                checkedMap[member.id] = it
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(member.avatarColor)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = member.name.firstOrNull()?.uppercase() ?: "",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = member.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Realtime Auto-Split Calculation Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Tự động tính tiền chia đều:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tổng số người ăn:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "$totalPeople người",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Mỗi người chi trả:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = CurrencyFormatter.formatVnd(splitAmountPerPerson),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sync Personal Expenses Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                keyboardController?.hide()
                                focusManager.clearFocus(force = true)
                                isAnyFieldFocused = false
                                syncPersonal = !syncPersonal
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = syncPersonal,
                            onCheckedChange = {
                                keyboardController?.hide()
                                focusManager.clearFocus(force = true)
                                isAnyFieldFocused = false
                                syncPersonal = it
                            },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "Đồng bộ vào chi tiêu ví cá nhân của tôi",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button
                    val selectedParticipantIds = checkedMap.filter { it.value }.keys.toList()
                    val canSubmit = title.isNotBlank() && totalAmount > 0 && totalPeople > 0

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus(force = true)
                            onSave(title.trim(), totalAmount, selectedParticipantIds, includeMe, syncPersonal)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("confirm_room_split_button"),
                        shape = RoundedCornerShape(25.dp),
                        enabled = canSubmit
                    ) {
                        Text("Xác nhận & Cập nhật nợ", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun MemberDebtDetailDialog(
    member: RoomMember,
    splits: List<RoomSplit>,
    onDismiss: () -> Unit,
    onSettle: (Double) -> Unit,
    onDeleteMember: () -> Unit
) {
    var settleAmountText by remember { mutableStateOf(member.totalOwed.toLong().toString()) }
    val initial = member.name.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
    val isOwed = member.totalOwed > 0
    val totalFromSplits = remember(splits) { splits.sumOf { it.splitAmountPerPerson } }
    val totalPaid = remember(totalFromSplits, member.totalOwed) {
        (totalFromSplits - member.totalOwed).coerceAtLeast(0.0)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(member.avatarColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column {
                    Text(
                        text = "Sổ nợ: ${member.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (member.phone.isNotBlank()) {
                        Text(
                            text = member.phone,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Debt summary banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOwed) OwedOrange.copy(alpha = 0.12f) else PaidGreen.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isOwed) "Số tiền đang nợ bạn:" else "Tình trạng công nợ:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isOwed) CurrencyFormatter.formatVnd(member.totalOwed) else "Đã hết nợ (0 đ)",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isOwed) OwedOrange else PaidGreen
                        )

                        if (splits.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Gồm ${splits.size} đơn chia tiền" + if (totalPaid > 0) " • Đã cấn trừ ${CurrencyFormatter.formatVnd(totalPaid)}" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Section: List of contributing orders (Đơn nào tạo nên số nợ này)
                Text(
                    text = "Chi tiết số nợ từ các đơn (${splits.size} đơn):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (splits.isEmpty()) {
                    Text(
                        text = "${member.name} chưa tham gia đơn chia tiền nào.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        splits.forEach { split ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Receipt,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = split.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${CurrencyFormatter.formatDate(split.date)} • Đơn ${CurrencyFormatter.formatVnd(split.totalAmount)} (${split.participantIds.size + 1} người)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "+${CurrencyFormatter.formatVnd(split.splitAmountPerPerson)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = OwedOrange
                                        )
                                        Text(
                                            text = "chia đều",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Calculation breakdown summary
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tổng tiền từ ${splits.size} đơn:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyFormatter.formatVnd(totalFromSplits),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (totalPaid > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Đã cấn trừ / thanh toán:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PaidGreen
                                    )
                                    Text(
                                        text = "-${CurrencyFormatter.formatVnd(totalPaid)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PaidGreen
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Số nợ hiện tại cần thu:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = CurrencyFormatter.formatVnd(member.totalOwed),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isOwed) OwedOrange else PaidGreen
                                )
                            }
                        }
                    }
                }

                // Settle Debt Input (if member owes money)
                if (isOwed) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ghi nhận thu tiền / Trả bớt nợ:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = settleAmountText,
                        onValueChange = { input -> settleAmountText = input.filter { it.isDigit() } },
                        label = { Text("Số tiền đã nhận (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { settleAmountText = member.totalOwed.toLong().toString() }) {
                            Text("Trả hết (${CurrencyFormatter.formatVnd(member.totalOwed)})")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isOwed) {
                Button(
                    onClick = {
                        val amount = settleAmountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onSettle(amount)
                        }
                    }
                ) {
                    Text("Xác nhận đã thu")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("Đóng") }
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = onDeleteMember,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Xóa thành viên")
                }
                TextButton(onClick = onDismiss) {
                    Text("Đóng")
                }
            }
        }
    )
}

@Composable
fun SettleDebtDialog(
    member: RoomMember,
    memberSplits: List<RoomSplit> = emptyList(),
    onDismiss: () -> Unit,
    onSettle: (Double) -> Unit,
    onDeleteMember: () -> Unit
) {
    var settleAmountText by remember { mutableStateOf(member.totalOwed.toLong().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thu nợ: ${member.name}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Đang nợ bạn:",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = CurrencyFormatter.formatVnd(member.totalOwed),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OwedOrange
                )

                // Breakdown of contributing orders
                if (memberSplits.isNotEmpty()) {
                    Text(
                        text = "Số nợ được tính từ các đơn sau (${memberSplits.size} đơn):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            memberSplits.forEach { split ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = split.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${CurrencyFormatter.formatDate(split.date)} • Đơn ${CurrencyFormatter.formatVnd(split.totalAmount)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "+${CurrencyFormatter.formatVnd(split.splitAmountPerPerson)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = OwedOrange
                                    )
                                }
                            }
                        }
                    }
                }

                if (member.totalOwed > 0) {
                    OutlinedTextField(
                        value = settleAmountText,
                        onValueChange = { input -> settleAmountText = input.filter { it.isDigit() } },
                        label = { Text("Số tiền bạn đã nhận (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { settleAmountText = member.totalOwed.toLong().toString() }) {
                            Text("Thanh toán hết")
                        }
                    }
                } else {
                    Text(
                        text = "${member.name} hiện không nợ bạn khoản nào.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PaidGreen
                    )
                }
            }
        },
        confirmButton = {
            if (member.totalOwed > 0) {
                Button(
                    onClick = {
                        val amount = settleAmountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onSettle(amount)
                        }
                    }
                ) {
                    Text("Xác nhận đã nhận tiền")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("Đóng") }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDeleteMember,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Xóa thành viên")
            }
        }
    )
}
