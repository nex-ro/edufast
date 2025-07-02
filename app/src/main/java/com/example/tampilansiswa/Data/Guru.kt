package com.example.tampilansiswa.Data

data class Guru(
    val nama: String,
    val subject: String,
    val rating: Double,
    val avatar: Int,
    val email: String = "",
    val phone: String = "",
    val uid: String = "",
    val imagePath: String = "",
    val ratingText: String = if (rating > 0) rating.toString() else "Unrated"
)
