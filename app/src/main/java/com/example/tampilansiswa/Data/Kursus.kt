package com.example.tampilansiswa.Data

data class Kursus(
    val namaGuru: String,
    val waktu: String,
    val tanggal: String,
    val avatar: Int,
    val status: String // "Selesai" atau "Belum"
)