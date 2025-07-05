package com.example.tampilansiswa.Data

data class users(
    val uid: String = "",
    val nama: String = "",
    val email: String = "",
    val phone: String = "",
    val imagePath: String = "",
    val role: String = "",
    val gender: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val profilePath: String = "",
    val profileImageUrl: String = ""
)
