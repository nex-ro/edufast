package com.example.tampilansiswa.Data

import com.google.firebase.Timestamp

data class Review(
    val studentId: String = "",
    val teacherId: String = "",
    val courseId: String = "",
    val enrollmentId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val timestamp: Timestamp? = null
)