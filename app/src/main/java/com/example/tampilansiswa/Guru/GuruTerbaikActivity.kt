package com.example.tampilansiswa.Guru

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.R
import com.example.tampilansiswa.Data.GuruTerbaik
import com.example.tampilansiswa.guru.GuruTerbaikAdapter
import com.google.android.material.appbar.MaterialToolbar

class GuruTerbaikActivity : AppCompatActivity() {

    private lateinit var rvGuruTerbaik: RecyclerView
    private lateinit var guruAdapter: GuruTerbaikAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guru_terbaik)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarGuruTerbaik)
        toolbar.setNavigationOnClickListener { finish() }

        rvGuruTerbaik = findViewById(R.id.rvGuruTerbaik)
        rvGuruTerbaik.layoutManager = LinearLayoutManager(this)

        val dataGuru = listOf(
            GuruTerbaik("Anika Rahman", "Bahasa Inggris", "Universitas Indonesia", 4.9, "1.3 KM", R.drawable.avatar1),
            GuruTerbaik("Muhammad", "Fisika", "Universitas Andalas", 4.9, "0.5 KM", R.drawable.avatar2),
            GuruTerbaik("Shintiya", "Matematika", "Universitas Riau", 4.8, "1.1 KM", R.drawable.avatar3),
            GuruTerbaik("Yohana", "Biologi", "Universitas Riau", 4.8, "1.3 KM", R.drawable.avatar4)
        )

        guruAdapter = GuruTerbaikAdapter(dataGuru)
        rvGuruTerbaik.adapter = guruAdapter
    }
}
