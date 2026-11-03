package com.example.data

import android.content.Context
import com.example.R
import com.example.model.Transaction
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ExpenseRepository(private val db: FirebaseFirestore) {

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

    fun observeTransactions(userId: String): Flow<List<Transaction>> {
        return db.collection("users").document(userId).collection("transactions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Transaction::class.java)
            }
    }

    suspend fun addTransaction(transaction: Transaction): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("transactions").document()
        val payload = hashMapOf(
            "userId" to uid,
            "title" to transaction.title,
            "amount" to transaction.amount,
            "type" to transaction.type,
            "category" to transaction.category,
            "note" to transaction.note,
            "timestamp" to transaction.timestamp,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        return docRef.id
    }

    suspend fun updateTransaction(transaction: Transaction) {
        val uid = requireUserId()
        require(transaction.id.isNotBlank()) { "Transaction ID must not be blank" }
        val docRef = db.collection("users").document(uid).collection("transactions").document(transaction.id)
        val payload = hashMapOf(
            "title" to transaction.title,
            "amount" to transaction.amount,
            "type" to transaction.type,
            "category" to transaction.category,
            "note" to transaction.note,
            "timestamp" to transaction.timestamp,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.update(payload).await()
    }

    suspend fun deleteTransaction(transactionId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("transactions").document(transactionId).delete().await()
    }
}
