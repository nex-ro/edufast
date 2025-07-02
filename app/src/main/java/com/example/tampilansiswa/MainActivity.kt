package com.example.tampilansiswa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.tampilansiswa.Dashboard.HomeFragment
import com.example.tampilansiswa.GuruPage.Dashboard_guru
import com.example.tampilansiswa.GuruPage.profil.guru_profil
//import com.example.tampilansiswa.Dashboard.DashboardGuruFragment
import com.example.tampilansiswa.Notifikasi.NotifikasiFragment
import com.example.tampilansiswa.Profile.AkunFragment
import com.example.tampilansiswa.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPref: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val role = sharedPref.getString("role", null)

        // Default fragment berdasarkan role
        if (role == "guru") {
            replaceFragment(Dashboard_guru())
        } else {
            replaceFragment(HomeFragment())
        }
        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> {
                    if (role == "guru") {
                        replaceFragment(Dashboard_guru())
                    } else {
                        replaceFragment(HomeFragment())
                    }
                }
                R.id.menu_notifikasi ->
                    replaceFragment(NotifikasiFragment())
                R.id.menu_akun -> {
                    if (role == "guru") {
                        replaceFragment(guru_profil())
                    } else {
                        replaceFragment(AkunFragment())
                    }
                }

            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}
