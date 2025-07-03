package com.example.tampilansiswa.Data

import java.io.Serializable

data class Course(
    val id: String = "",
    val courseName: String = "",
    val subject: String = "",
    val level: String = "",
    val courseType: String = "",
    val duration: Int = 0,
    val location: String = "",
    val city: String = "",
    val date: String = "",
    val uid: String = "",
    val startTime: String = "",
    val price: Double = 0.0,
    val status: String = "",
    val poster: String = "",
    val description: String = "",
    val active: Boolean = true
) : Serializable {

    // Properties yang bisa langsung diakses
    val formattedDuration: String
        get() = if (duration > 0) "$duration jam" else "Tidak ditentukan"

    val fullLocation: String
        get() = if (location.isNotEmpty() && city.isNotEmpty()) {
            "$location, $city"
        } else {
            location.ifEmpty { city }
        }

    val formattedPrice: String
        get() = if (price > 0) "Rp ${String.format("%,.0f", price)}" else "Gratis"

    val statusText: String
        get() = when (status.lowercase()) {
            "available" -> "Tersedia"
            "full" -> "Penuh"
            "cancelled" -> "Dibatalkan"
            else -> status
        }
}