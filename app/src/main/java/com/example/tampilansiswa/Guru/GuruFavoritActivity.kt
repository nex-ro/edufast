package com.example.tampilansiswa.Guru

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.R
import com.example.tampilansiswa.Data.GuruFavorit
import com.google.android.material.appbar.MaterialToolbar

class GuruFavoritActivity : AppCompatActivity() {

    private lateinit var rvGuru: RecyclerView
    private lateinit var adapter: GuruFavoritAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guru_favorit)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvGuru = findViewById(R.id.rvGuruFavorit)
        rvGuru.layoutManager = LinearLayoutManager(this)

        val guruList = listOf(
            GuruFavorit("Yohana", "Biologi", "Universitas Riau", "1.3 KM", 64, R.drawable.avatar1),
            GuruFavorit("Rahmawati", "Matematika", "Universitas Gadjah Mada", "1.3 KM", 42, R.drawable.avatar2),
            GuruFavorit("Anika Rahman", "Bahasa Inggris", "Universitas Indonesia", "1.3 KM", 30, R.drawable.avatar3),
            GuruFavorit("Muhammad", "Fisika", "Universitas Andalas", "0.5 KM", 24, R.drawable.avatar4)
        )

        adapter = GuruFavoritAdapter(guruList)
        rvGuru.adapter = adapter
    }
}
