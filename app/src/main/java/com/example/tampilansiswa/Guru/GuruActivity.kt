package com.example.tampilansiswa.Guru

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.Data.Guru
import com.example.tampilansiswa.Dashboard.GuruAdapter
import com.example.tampilansiswa.R

class GuruActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_guru)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val listGuru = listOf(
            Guru("Yohana", "Biologi", 4.9, R.drawable.avatar1),
            Guru("Rahmawati", "Matematika", 4.8, R.drawable.avatar2),
            Guru("Anika", "Bahasa Inggris", 4.9, R.drawable.avatar3)
        )

        val rvGuru = findViewById<RecyclerView>(R.id.rvGuru)
        rvGuru.layoutManager = GridLayoutManager(this, 2)
        rvGuru.adapter = GuruAdapter(listGuru)

        val layoutFavorit = findViewById<LinearLayout>(R.id.layoutFavorit)
        layoutFavorit.setOnClickListener {
            Toast.makeText(this, "Klik Favorit", Toast.LENGTH_SHORT).show() // uji listener
            startActivity(Intent(this, GuruFavoritActivity::class.java))
        }

        val layoutTerbaik = findViewById<LinearLayout>(R.id.layoutTerbaik)
        layoutTerbaik.setOnClickListener {
            startActivity(Intent(this, GuruTerbaikActivity::class.java))
        }
    }
}
