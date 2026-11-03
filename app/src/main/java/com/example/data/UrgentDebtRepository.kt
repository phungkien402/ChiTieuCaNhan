package com.example.data

import android.content.Context
import com.example.R
import com.example.model.UrgentDebt
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UrgentDebtRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Người dùng cần đăng nhập Google trước khi thao tác.")
    }

    fun observeUrgentDebts(userId: String): Flow<List<UrgentDebt>> {
        return db.collection("users").document(userId).collection("urgent_debts")
            .orderBy("dueDate", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(UrgentDebt::class.java)
            }
    }

    suspend fun addUrgentDebt(debt: UrgentDebt): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("urgent_debts").document()
        val payload = hashMapOf(
            "userId" to uid,
            "title" to debt.title.trim(),
            "amount" to debt.amount,
            "isPayable" to debt.isPayable,
            "contactName" to debt.contactName.trim(),
            "dueDate" to debt.dueDate,
            "isSettled" to debt.isSettled,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        return docRef.id
    }

    suspend fun toggleSettled(debtId: String, currentSettled: Boolean) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("urgent_debts").document(debtId)
        docRef.update(
            mapOf(
                "isSettled" to !currentSettled,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun deleteUrgentDebt(debtId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("urgent_debts").document(debtId).delete().await()
    }
}
