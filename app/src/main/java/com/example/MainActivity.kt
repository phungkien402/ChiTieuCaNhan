package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.signOutUser
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.roomsplit.AddRoomSplitBottomSheet
import com.example.ui.roomsplit.RoomSplitScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.stats.MonthlyStatsScreen
import com.example.ui.theme.ChiTieuTheme
import com.example.ui.transactions.AddEditTransactionDialog
import com.example.ui.transactions.TransactionListScreen
import com.example.ui.urgentdebts.UrgentDebtScreen
import com.example.ui.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel()
            val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

            val isDarkTheme = when (userSettings.isDarkMode) {
                true -> true
                false -> false
                null -> isSystemInDarkTheme()
            }

            ChiTieuTheme(darkTheme = isDarkTheme) {
                AppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppRoot(viewModel: MainViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                viewModel.setUserId(auth.currentUser!!.uid)
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                    currentUser?.uid?.let { viewModel.setUserId(it) }
                },
                onUnauthenticated = {},
                scope = coroutineScope
            )
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = { uid ->
                currentUser = Firebase.auth.currentUser
                viewModel.setUserId(uid)
            }
        )
    } else {
        MainAppContent(
            viewModel = viewModel,
            onSignOut = {
                signOutUser(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                        currentUser = null
                    },
                    scope = coroutineScope
                )
            }
        )
    }
}

enum class MainTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Tổng quan", Icons.Default.Dashboard),
    TRANSACTIONS("Chi tiêu", Icons.Default.ReceiptLong),
    ROOM_SPLIT("Chia phòng", Icons.Default.Group),
    URGENT_DEBTS("Nợ gấp", Icons.Default.NotificationImportant),
    STATS("Thống kê", Icons.Default.PieChart)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.DASHBOARD) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showAddRoomSplitSheet by remember { mutableStateOf(false) }

    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val roomMembers by viewModel.roomMembers.collectAsStateWithLifecycle()
    val roomSplits by viewModel.roomSplits.collectAsStateWithLifecycle()
    val urgentDebts by viewModel.urgentDebts.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    // Handle Back Press when in Settings or secondary tabs
    BackHandler(enabled = isSettingsOpen || selectedTab != MainTab.DASHBOARD) {
        if (isSettingsOpen) {
            isSettingsOpen = false
        } else {
            selectedTab = MainTab.DASHBOARD
        }
    }

    val unsettledUrgentCount = remember(urgentDebts) {
        urgentDebts.count { it.isPayable && !it.isSettled }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isSettingsOpen) "Cài đặt" else selectedTab.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    if (isSettingsOpen) {
                        IconButton(onClick = { isSettingsOpen = false }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại"
                            )
                        }
                    }
                },
                actions = {
                    if (!isSettingsOpen) {
                        IconButton(
                            onClick = { isSettingsOpen = true },
                            modifier = Modifier.testTag("settings_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Cài đặt"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (!isSettingsOpen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    MainTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                if (tab == MainTab.URGENT_DEBTS && unsettledUrgentCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge { Text("$unsettledUrgentCount") }
                                        }
                                    ) {
                                        Icon(imageVector = tab.icon, contentDescription = tab.title)
                                    }
                                } else {
                                    Icon(imageVector = tab.icon, contentDescription = tab.title)
                                }
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isSettingsOpen) {
                SettingsScreen(
                    userSettings = userSettings,
                    onUpdateBudget = { viewModel.updateMonthlyBudget(it) },
                    onUpdateReminder = { h, m, enabled -> viewModel.updateReminderTime(h, m, enabled) },
                    onUpdateDarkMode = { viewModel.updateDarkMode(it) },
                    onExportData = { viewModel.exportData() },
                    onSignOut = onSignOut
                )
            } else {
                when (selectedTab) {
                    MainTab.DASHBOARD -> {
                        DashboardScreen(
                            transactions = transactions,
                            userSettings = userSettings,
                            totalBalance = viewModel.getTotalBalance(),
                            monthExpense = viewModel.getThisMonthExpenses(),
                            monthIncome = viewModel.getThisMonthIncome(),
                            roomDebtsOwedToMe = viewModel.getTotalRoomDebtsOwedToMe(),
                            unsettledUrgentPayablesCount = unsettledUrgentCount,
                            onOpenAddTransaction = { showAddTransactionDialog = true },
                            onOpenAddRoomSplit = { showAddRoomSplitSheet = true },
                            onNavigateToRoomSplit = { selectedTab = MainTab.ROOM_SPLIT },
                            onNavigateToUrgentDebts = { selectedTab = MainTab.URGENT_DEBTS },
                            onNavigateToTransactions = { selectedTab = MainTab.TRANSACTIONS },
                            onDeleteTransaction = { viewModel.deleteTransaction(it) }
                        )
                    }
                    MainTab.TRANSACTIONS -> {
                        TransactionListScreen(
                            transactions = transactions,
                            onAddTransactionClick = { showAddTransactionDialog = true },
                            onDeleteTransaction = { viewModel.deleteTransaction(it) }
                        )
                    }
                    MainTab.ROOM_SPLIT -> {
                        RoomSplitScreen(
                            roomMembers = roomMembers,
                            roomSplits = roomSplits,
                            onAddMember = { name, phone, color -> viewModel.addRoomMember(name, phone, color) },
                            onDeleteMember = { viewModel.deleteRoomMember(it) },
                            onSettleDebt = { id, amount, owed -> viewModel.settleMemberDebt(id, amount, owed) },
                            onAddSplitExpense = { title, total, pIds, includeMe, sync ->
                                viewModel.addRoomSplitExpense(title, total, pIds, includeMe, sync)
                            },
                            onDeleteSplitExpense = { viewModel.deleteRoomSplit(it) }
                        )
                    }
                    MainTab.URGENT_DEBTS -> {
                        UrgentDebtScreen(
                            urgentDebts = urgentDebts,
                            onAddUrgentDebt = { viewModel.addUrgentDebt(it) },
                            onToggleSettled = { id, curr -> viewModel.toggleDebtSettled(id, curr) },
                            onDeleteUrgentDebt = { viewModel.deleteUrgentDebt(it) }
                        )
                    }
                    MainTab.STATS -> {
                        MonthlyStatsScreen(
                            transactions = transactions,
                            userSettings = userSettings
                        )
                    }
                }
            }
        }
    }

    // Dialog: Add Transaction
    if (showAddTransactionDialog) {
        AddEditTransactionDialog(
            onDismiss = { showAddTransactionDialog = false },
            onSave = { title, amount, type, category, note ->
                viewModel.addTransaction(title, amount, type, category, note)
                showAddTransactionDialog = false
            }
        )
    }

    // Modal: Add Room Split Expense
    if (showAddRoomSplitSheet) {
        AddRoomSplitBottomSheet(
            roomMembers = roomMembers,
            onDismiss = { showAddRoomSplitSheet = false },
            onSave = { title, total, participantIds, includeMe, syncPersonal ->
                viewModel.addRoomSplitExpense(title, total, participantIds, includeMe, syncPersonal)
                showAddRoomSplitSheet = false
            }
        )
    }
}
