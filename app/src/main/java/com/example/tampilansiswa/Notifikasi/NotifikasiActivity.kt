package com.example.tampilansiswa.Notifikasi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tampilansiswa.Dashboard.HomeFragment
import com.example.tampilansiswa.Profile.AkunFragment
import com.example.tampilansiswa.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class NotifikasiActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifikasi)

        bottomNav.selectedItemId = R.id.menu_notifikasi

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> {
                    startActivity(Intent(this, HomeFragment::class.java))
                    finish()
                    true
                }
                R.id.menu_notifikasi -> true
                R.id.menu_akun -> {
                    startActivity(Intent(this, AkunFragment::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
