package com.example.tampilansiswa.Data

import com.google.firebase.firestore.Exclude

data class Pengalaman(
    @get:Exclude var id: String = "",
    var namaPengalaman: String = "",
    var jabatan: String = "",
    var tahunMulai: String = "",
    var tahunBerakhir: String = "",
    var masihBekerja: Boolean = false,
    var jobDesc: String = "",
    var uid: String = ""
) {
    constructor() : this(
        id = "",
        namaPengalaman = "",
        jabatan = "",
        tahunMulai = "",
        tahunBerakhir = "",
        masihBekerja = false,
        jobDesc = "",
        uid = ""
    )
}
