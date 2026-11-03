package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class RoomSplit(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val totalAmount: Double = 0.0,
    val payerId: String = "me",
    val participantIds: List<String> = emptyList(),
    val splitAmountPerPerson: Double = 0.0,
    val paidMemberIds: List<String> = emptyList(),
    val date: Timestamp = Timestamp.now(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
