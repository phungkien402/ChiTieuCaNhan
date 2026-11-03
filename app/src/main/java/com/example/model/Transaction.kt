package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class TransactionType {
    EXPENSE,
    INCOME
}

enum class ExpenseCategory(val displayName: String, val iconName: String) {
    FOOD("Ăn uống", "restaurant"),
    SHOPPING("Mua sắm", "shopping_cart"),
    HOUSING("Tiền nhà/trọ", "home"),
    BILLS("Điện nước, Net", "receipt_long"),
    TRANSPORT("Di chuyển", "directions_car"),
    ENTERTAINMENT("Giải trí", "sports_esports"),
    HEALTH("Sức khỏe", "medical_services"),
    SALARY("Lương / Thu nhập", "payments"),
    OTHER("Khác", "category")
}

@IgnoreExtraProperties
data class Transaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = TransactionType.EXPENSE.name,
    val category: String = ExpenseCategory.FOOD.displayName,
    val note: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
