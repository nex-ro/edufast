package com.example.tampilansiswa.Detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tampilansiswa.Pemesanan.PemesananActivity
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.ActivityDetailGuruBinding
import com.google.firebase.firestore.FirebaseFirestore

class DetailGuruActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailGuruBinding
    private lateinit var guruId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailGuruBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nama = intent.getStringExtra("nama")
        val mapel = intent.getStringExtra("mapel")
        val rating = intent.getDoubleExtra("rating", 0.0)
        val gambar = intent.getIntExtra("gambar", 0)
        guruId = intent.getStringExtra("guruId") ?: ""

        binding.txtNama.text = nama
        binding.txtMapel.text = mapel
        binding.txtRating.text = "⭐ $rating"
        binding.imgGuru.setImageResource(gambar)

        // Ambil ulasan dari Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("Reviews")
            .whereEqualTo("guruId", guruId)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val reviewer = doc.getString("reviewer") ?: "Anonim"
                    val comment = doc.getString("comment") ?: ""
                    val rate = doc.getLong("rating")?.toInt() ?: 0

                    val item = LayoutInflater.from(this)
                        .inflate(R.layout.item_review, binding.containerReviews, false)
                    item.findViewById<TextView>(R.id.tvReviewer).text = reviewer
                    item.findViewById<TextView>(R.id.tvStar).text = "★".repeat(rate)
                    item.findViewById<TextView>(R.id.tvComment).text = comment
                    binding.containerReviews.addView(item)
                }
            }

        // Saat tombol "Pesan" ditekan
        binding.btnPesan.setOnClickListener {
            val intent = Intent(this, PemesananActivity::class.java)
            startActivity(intent)
        }
    }
}
