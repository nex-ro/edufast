package com.example.tampilansiswa.GuruPage.kursus

data class Course(
    var id: String = "",
    val courseName: String = "",
    val subject: String = "",
    val date: String = "",
    val startTime: String = "",
    val duration: Int = 0,
    val courseType: String = "", // privat, ke tempat
    val region: String = "",
    val location: String = "",
    val level: String = "", // SD, SMP, SMA
    val description: String = "",
    val price: Long = 0,
    val active: Boolean = true,
    val uid: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    // Constructor kosong diperlukan untuk Firestore
    constructor() : this("", "", "", "", "", 0, "", "", "", "", "", 0, true, "", 0)

    fun getFormattedPrice(): String {
        return "Rp ${String.format("%,d", price)}"
    }

    // Fungsi untuk mendapatkan durasi dalam format jam dan menit
    fun getFormattedDuration(): String {
        val hours = duration / 60
        val minutes = duration % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}j ${minutes}m"
            hours > 0 -> "${hours} jam"
            else -> "${minutes} menit"
        }
    }

    // Fungsi untuk mendapatkan lokasi lengkap
    fun getFullLocation(): String {
        return "$location, $region"
    }

    // Fungsi untuk mendapatkan status dalam bahasa Indonesia
    fun getStatusText(): String {
        return if (active) "Aktif" else "Non-Aktif"
    }
}