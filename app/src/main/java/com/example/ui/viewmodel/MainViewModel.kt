package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ExpenseRepository
import com.example.data.RoomSplitRepository
import com.example.data.UrgentDebtRepository
import com.example.data.UserSettingsRepository
import com.example.model.RoomMember
import com.example.model.RoomSplit
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.model.UrgentDebt
import com.example.model.UserSettings
import com.example.receiver.ExpenseReminderReceiver
import com.example.util.ExportHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository = ExpenseRepository(application)
    private val roomSplitRepository = RoomSplitRepository(application)
    private val urgentDebtRepository = UrgentDebtRepository(application)
    private val userSettingsRepository = UserSettingsRepository(application)

    private val _currentUserId = MutableStateFlow(Firebase.auth.currentUser?.uid ?: "")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _roomMembers = MutableStateFlow<List<RoomMember>>(emptyList())
    val roomMembers: StateFlow<List<RoomMember>> = _roomMembers.asStateFlow()

    private val _roomSplits = MutableStateFlow<List<RoomSplit>>(emptyList())
    val roomSplits: StateFlow<List<RoomSplit>> = _roomSplits.asStateFlow()

    private val _urgentDebts = MutableStateFlow<List<UrgentDebt>>(emptyList())
    val urgentDebts: StateFlow<List<UrgentDebt>> = _urgentDebts.asStateFlow()

    val userSettings: StateFlow<UserSettings> = userSettingsRepository.userSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        val uid = Firebase.auth.currentUser?.uid
        if (!uid.isNullOrBlank()) {
            loadUserData(uid)
        }
    }

    fun setUserId(uid: String) {
        _currentUserId.value = uid
        if (uid.isNotBlank()) {
            loadUserData(uid)
        }
    }

    private fun loadUserData(uid: String) {
        viewModelScope.launch {
            expenseRepository.observeTransactions(uid).collect { list ->
                _transactions.value = list
            }
        }
        viewModelScope.launch {
            roomSplitRepository.observeMembers(uid).collect { list ->
                _roomMembers.value = list
            }
        }
        viewModelScope.launch {
            roomSplitRepository.observeSplits(uid).collect { list ->
                _roomSplits.value = list
            }
        }
        viewModelScope.launch {
            urgentDebtRepository.observeUrgentDebts(uid).collect { list ->
                _urgentDebts.value = list
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // --- TRANSACTIONS ---

    fun addTransaction(title: String, amount: Double, type: TransactionType, category: String, note: String) {
        viewModelScope.launch {
            try {
                val t = Transaction(
                    title = title,
                    amount = amount,
                    type = type.name,
                    category = category,
                    note = note
                )
                expenseRepository.addTransaction(t)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi thêm giao dịch"
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                expenseRepository.deleteTransaction(transactionId)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi xóa giao dịch"
            }
        }
    }

    // --- ROOM SPLIT & MEMBERS ---

    fun addRoomMember(name: String, phone: String, avatarColor: Long) {
        viewModelScope.launch {
            try {
                roomSplitRepository.addMember(name, phone, avatarColor)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi thêm thành viên"
            }
        }
    }

    fun updateRoomMember(member: RoomMember) {
        viewModelScope.launch {
            try {
                roomSplitRepository.updateMember(member)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi cập nhật thành viên"
            }
        }
    }

    fun deleteRoomMember(memberId: String) {
        viewModelScope.launch {
            try {
                roomSplitRepository.deleteMember(memberId)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi xóa thành viên"
            }
        }
    }

    fun settleMemberDebt(memberId: String, amount: Double, currentOwed: Double) {
        viewModelScope.launch {
            try {
                roomSplitRepository.settleMemberDebt(memberId, amount, currentOwed)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi thanh toán công nợ"
            }
        }
    }

    fun addRoomSplitExpense(
        title: String,
        totalAmount: Double,
        participantIds: List<String>,
        includeMe: Boolean,
        syncPersonal: Boolean
    ) {
        viewModelScope.launch {
            try {
                roomSplitRepository.addRoomSplit(
                    title = title,
                    totalAmount = totalAmount,
                    participantIds = participantIds,
                    allMembers = _roomMembers.value,
                    includeMeInSplit = includeMe,
                    syncToPersonalExpenses = syncPersonal
                )
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi tạo đợt chia tiền"
            }
        }
    }

    fun deleteRoomSplit(split: RoomSplit) {
        viewModelScope.launch {
            try {
                roomSplitRepository.deleteRoomSplit(split, _roomMembers.value)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi xóa đợt chia tiền"
            }
        }
    }

    // --- URGENT DEBTS ---

    fun addUrgentDebt(debt: UrgentDebt) {
        viewModelScope.launch {
            try {
                urgentDebtRepository.addUrgentDebt(debt)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi thêm khoản nợ khẩn cấp"
            }
        }
    }

    fun toggleDebtSettled(debtId: String, currentSettled: Boolean) {
        viewModelScope.launch {
            try {
                urgentDebtRepository.toggleSettled(debtId, currentSettled)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi cập nhật trạng thái nợ"
            }
        }
    }

    fun deleteUrgentDebt(debtId: String) {
        viewModelScope.launch {
            try {
                urgentDebtRepository.deleteUrgentDebt(debtId)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Lỗi xóa khoản nợ"
            }
        }
    }

    // --- SETTINGS ---

    fun updateMonthlyBudget(budget: Double) {
        viewModelScope.launch {
            userSettingsRepository.updateBudget(budget)
        }
    }

    fun updateReminderTime(hour: Int, minute: Int, enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.updateReminder(hour, minute, enabled)
            if (enabled) {
                ExpenseReminderReceiver.scheduleDailyReminder(getApplication(), hour, minute)
            } else {
                ExpenseReminderReceiver.cancelDailyReminder(getApplication())
            }
        }
    }

    fun updateDarkMode(isDark: Boolean?) {
        viewModelScope.launch {
            userSettingsRepository.updateDarkMode(isDark)
        }
    }

    fun exportData() {
        ExportHelper.exportDataToCsv(
            context = getApplication(),
            transactions = _transactions.value,
            roomSplits = _roomSplits.value,
            roomMembers = _roomMembers.value,
            urgentDebts = _urgentDebts.value
        )
    }

    // --- COMPUTED STATS ---

    fun getThisMonthExpenses(): Double {
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        return _transactions.value
            .filter { it.type == TransactionType.EXPENSE.name }
            .filter {
                val cal = Calendar.getInstance().apply { time = it.timestamp.toDate() }
                cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
            }
            .sumOf { it.amount }
    }

    fun getThisMonthIncome(): Double {
        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)
        val currentYear = now.get(Calendar.YEAR)

        return _transactions.value
            .filter { it.type == TransactionType.INCOME.name }
            .filter {
                val cal = Calendar.getInstance().apply { time = it.timestamp.toDate() }
                cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
            }
            .sumOf { it.amount }
    }

    fun getTotalBalance(): Double {
        var total = 0.0
        for (t in _transactions.value) {
            if (t.type == TransactionType.INCOME.name) {
                total += t.amount
            } else {
                total -= t.amount
            }
        }
        return total
    }

    fun getTotalRoomDebtsOwedToMe(): Double {
        return _roomMembers.value.sumOf { it.totalOwed }
    }

    fun getUnsettledUrgentPayablesCount(): Int {
        return _urgentDebts.value.count { it.isPayable && !it.isSettled }
    }

    fun getCategoryExpensesForMonth(month: Int, year: Int): Map<String, Double> {
        return _transactions.value
            .filter { it.type == TransactionType.EXPENSE.name }
            .filter {
                val cal = Calendar.getInstance().apply { time = it.timestamp.toDate() }
                cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
            }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }
}
