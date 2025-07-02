package com.example.tampilansiswa.Ulasan

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tampilansiswa.R
import com.google.firebase.firestore.FirebaseFirestore

class UlasanActivity : AppCompatActivity() {

    private lateinit var layoutStars: LinearLayout
    private lateinit var etKomentar: EditText

    private var rating = 0
    private lateinit var stars: Array<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ulasan)

        layoutStars = findViewById(R.id.layoutStars)
        etKomentar = findViewById(R.id.etKomentar)
        val btnSimpan = findViewById<android.widget.Button>(R.id.btnSimpan)

        stars = Array(5) { ImageView(this) }
        for (i in 0 until 5) {
            val star = ImageView(this).apply {
                setImageResource(R.drawable.ic_bintang)
                val size = (36 * resources.displayMetrics.density).toInt()
                val params = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(8, 0, 8, 0)
                }
                layoutParams = params
                setOnClickListener {
                    rating = i + 1
                    updateStars()
                }
            }
            stars[i] = star
            layoutStars.addView(star)
        }

        val guruId = intent.getStringExtra("GURU_ID") ?: ""
        val reviewer = intent.getStringExtra("REVIEWER_NAME") ?: "Anonim"

        btnSimpan.setOnClickListener {
            val komentar = etKomentar.text.toString().trim()
            if (rating == 0 || komentar.isEmpty()) {
                Toast.makeText(this, "Pilih bintang dan tulis komentar terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            val review = hashMapOf(
                "reviewer" to reviewer,
                "rating" to rating,
                "comment" to komentar,
                "guruId" to guruId
            )

            db.collection("Reviews")
                .add(review)
                .addOnSuccessListener {
                    Toast.makeText(this, "Ulasan berhasil dikirim", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal mengirim ulasan", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateStars() {
        for (i in stars.indices) {
            stars[i].setImageResource(if (i < rating) R.drawable.ic_bintang else R.drawable.ic_bintang)
        }
    }
}