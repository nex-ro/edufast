package com.example.tampilansiswa.Onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tampilansiswa.R

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_onboarding, Onboarding1Fragment())
                .commit()
        }
    }
}
