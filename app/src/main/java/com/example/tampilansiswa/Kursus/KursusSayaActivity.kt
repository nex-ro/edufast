package com.example.tampilansiswa.Kursus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tampilansiswa.R

class KursusSayaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kursus_saya)

        // Tampilkan fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.containerKursus, KursusSayaFragment())
            .commit()
    }
}
