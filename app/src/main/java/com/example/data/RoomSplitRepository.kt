package com.example.data

import android.content.Context
import com.example.R
import com.example.model.ExpenseCategory
import com.example.model.RoomMember
import com.example.model.RoomSplit
import com.example.model.Transaction
import com.example.model.TransactionType
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class RoomSplitRepository(
    private val db: FirebaseFirestore,
    private val expenseRepository: ExpenseRepository
) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        ExpenseRepository(context)
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Người dùng cần đăng nhập Google trước khi thao tác.")
    }

    // --- ROOM MEMBERS ---

    fun observeMembers(userId: String): Flow<List<RoomMember>> {
        return db.collection("users").document(userId).collection("room_members")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(RoomMember::class.java)
            }
    }

    suspend fun addMember(name: String, phone: String = "", avatarColor: Long = 0xFF00685FL): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("room_members").document()
        val payload = hashMapOf(
            "userId" to uid,
            "name" to name.trim(),
            "phone" to phone.trim(),
            "avatarColor" to avatarColor,
            "totalOwed" to 0.0,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        return docRef.id
    }

    suspend fun updateMember(member: RoomMember) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("room_members").document(member.id)
        val payload = hashMapOf(
            "name" to member.name.trim(),
            "phone" to member.phone.trim(),
            "avatarColor" to member.avatarColor,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.update(payload).await()
    }

    suspend fun deleteMember(memberId: String) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("room_members").document(memberId).delete().await()
    }

    suspend fun settleMemberDebt(memberId: String, amountToSettle: Double, currentOwed: Double) {
        val uid = requireUserId()
        val newOwed = (currentOwed - amountToSettle).coerceAtLeast(0.0)
        val docRef = db.collection("users").document(uid).collection("room_members").document(memberId)
        docRef.update(
            mapOf(
                "totalOwed" to newOwed,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    // --- ROOM SPLIT EXPENSES ---

    fun observeSplits(userId: String): Flow<List<RoomSplit>> {
        return db.collection("users").document(userId).collection("room_splits")
            .orderBy("date", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(RoomSplit::class.java)
            }
    }

    suspend fun addRoomSplit(
        title: String,
        totalAmount: Double,
        participantIds: List<String>,
        allMembers: List<RoomMember>,
        includeMeInSplit: Boolean,
        syncToPersonalExpenses: Boolean
    ): String {
        val uid = requireUserId()
        require(participantIds.isNotEmpty() || includeMeInSplit) { "Cần ít nhất 1 người tham gia chia tiền" }

        val totalPeople = participantIds.size + if (includeMeInSplit) 1 else 0
        val splitAmountPerPerson = if (totalPeople > 0) totalAmount / totalPeople else totalAmount

        val docRef = db.collection("users").document(uid).collection("room_splits").document()
        val payload = hashMapOf(
            "userId" to uid,
            "title" to title.trim(),
            "totalAmount" to totalAmount,
            "payerId" to "me",
            "participantIds" to participantIds,
            "splitAmountPerPerson" to splitAmountPerPerson,
            "paidMemberIds" to emptyList<String>(),
            "date" to Timestamp.now(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()

        // Update each participating member's debt balance (totalOwed)
        for (memberId in participantIds) {
            val member = allMembers.find { it.id == memberId }
            val currentOwed = member?.totalOwed ?: 0.0
            val updatedOwed = currentOwed + splitAmountPerPerson
            db.collection("users").document(uid).collection("room_members").document(memberId)
                .update(
                    mapOf(
                        "totalOwed" to updatedOwed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
        }

        // If requested, record personal money spent
        if (syncToPersonalExpenses) {
            val transaction = Transaction(
                userId = uid,
                title = "[Đi chợ phòng] $title",
                amount = totalAmount,
                type = TransactionType.EXPENSE.name,
                category = ExpenseCategory.FOOD.displayName,
                note = "Chia $totalPeople người, mỗi người ${splitAmountPerPerson.toLong()} đ",
                timestamp = Timestamp.now()
            )
            expenseRepository.addTransaction(transaction)
        }

        return docRef.id
    }

    suspend fun deleteRoomSplit(split: RoomSplit, allMembers: List<RoomMember>) {
        val uid = requireUserId()
        // Deduct split amount from participants' totalOwed
        for (memberId in split.participantIds) {
            val member = allMembers.find { it.id == memberId }
            if (member != null) {
                val newOwed = (member.totalOwed - split.splitAmountPerPerson).coerceAtLeast(0.0)
                db.collection("users").document(uid).collection("room_members").document(memberId)
                    .update(
                        mapOf(
                            "totalOwed" to newOwed,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
            }
        }
        db.collection("users").document(uid).collection("room_splits").document(split.id).delete().await()
    }
}
