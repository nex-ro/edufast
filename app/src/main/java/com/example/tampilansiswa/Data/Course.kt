package com.example.tampilansiswa.Data

import android.R

data class Course(
    val courseName: String = "",
    val subject: String = "",
    val level: String = "",
    val courseType: String = "",
    val formattedDuration: String = "",
    val fullLocation: String = "",
    val date: String = "",
    val startTime: String = "",
    val formattedPrice: String = "",
    val statusText: String = "",
    val poster: String? = null,
    val active: Boolean = false,
    val createdAt: Long = 0,
    val description: String = "",
    val duration: Int = 0,
    val id: String = "",
    val location: String = "",
    val price: Int = 0,
    val region: String = "",
    val uid: String = ""
)
