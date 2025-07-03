package com.example.tampilansiswa.Data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.util.Date

data class enrollments(
    val id: String = "",
    val accountNumber: String = "",
    val amount: Long = 0,
    val bankName: String = "",
    val courseId: String = "",
    val courseName: String = "",
    @PropertyName("createdAt")
    val createdAt: Any? = null, // Can be either Timestamp or Long
    val paymentProofPath: String = "",
    val status: String = "",
    val studentId: String = "",
    val teacherId: String = "",
    @PropertyName("updatedAt")
    val updatedAt: Any? = null // Can be either Timestamp or Long
) {
    // Helper methods to get consistent Date objects
    val createdAtDate: Date?
        get() = when (createdAt) {
            is Timestamp -> createdAt.toDate()
            is Long -> if (createdAt > 0) Date(createdAt) else null
            is Number -> if (createdAt.toLong() > 0) Date(createdAt.toLong()) else null
            else -> null
        }

    val updatedAtDate: Date?
        get() = when (updatedAt) {
            is Timestamp -> updatedAt.toDate()
            is Long -> if (updatedAt > 0) Date(updatedAt) else null
            is Number -> if (updatedAt.toLong() > 0) Date(updatedAt.toLong()) else null
            else -> null
        }

    // Helper methods to get Long values
    val createdAtLong: Long
        get() = when (createdAt) {
            is Timestamp -> createdAt.seconds
            is Long -> createdAt
            is Number -> createdAt.toLong()
            else -> 0L
        }

    val updatedAtLong: Long
        get() = when (updatedAt) {
            is Timestamp -> updatedAt.seconds
            is Long -> updatedAt
            is Number -> updatedAt.toLong()
            else -> 0L
        }
}