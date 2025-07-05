package com.example.tampilansiswa.Data


data class Guru(
    val nama: String,
    val rating: Double,
    val avatar: Int,
    val email: String = "",
    val phone: String = "",
    val uid: String = "",
    val imagePath: String = "",
    val domisili: String = "",
    val subjek:String="",
    val ratingText: String = if (rating > 0) rating.toString() else "Unrated",
    val bio: String = "",
    val education: String = "",
    val gender: String = "",
    val averageRating: Double = 0.0,
    val totalRating: Int = 0,
    val siswa: Int = 0,
    val studentCount: Int = 0,
    val isActive: Boolean = true,
    val profileImageUrl: String = "",
    val role: String = "guru",
    val createdAt: Long = 0,
    val updatedAt: Any? = null
)