package com.example.tampilansiswa.Dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tampilansiswa.Data.Guru
import com.example.tampilansiswa.Detail.DetailGuru
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.ItemGuruBinding
import java.io.File

class GuruAdapter(private val listGuru: List<Guru>) :
    RecyclerView.Adapter<GuruAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemGuruBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(guru: Guru) {
            binding.txtNama.text = guru.nama
            binding.txtMapel.text = guru.education.ifEmpty { "Pendidikan tidak tersedia" }

            // Display rating using averageRating if available, otherwise use rating
            val displayRating = if (guru.averageRating > 0) guru.averageRating else guru.rating

            if (displayRating > 0) {
                binding.txtRating.text = "⭐ ${String.format("%.1f", displayRating)}"
            } else {
                binding.txtRating.text = "⭐ Unrated"
            }

            when {
                guru.imagePath.isNotEmpty() -> {
                    Glide.with(itemView.context)
                        .load(guru.imagePath)
                        .placeholder(guru.avatar)
                        .error(guru.avatar)
                        .circleCrop()
                        .into(binding.imgGuru)
                }
                guru.imagePath.isNotEmpty() && File(guru.imagePath).exists() -> {
                    Glide.with(itemView.context)
                        .load(File(guru.imagePath))
                        .placeholder(guru.avatar)
                        .error(guru.avatar)
                        .circleCrop()
                        .into(binding.imgGuru)
                }
                else -> {
                    // Use default avatar resource
                    binding.imgGuru.setImageResource(guru.avatar)
                }
            }

            // Event klik item - Navigate to Fragment instead of Activity
            binding.root.setOnClickListener {
                val context = itemView.context

                // Make sure context is FragmentActivity
                if (context is FragmentActivity) {
                    // Create DetailGuru fragment with all guru data
                    val detailFragment = DetailGuru.newInstance(
                        guruId = guru.uid,
                        nama = guru.nama,
                        mapel = guru.education.ifEmpty { "Pendidikan tidak tersedia" },
                        universitas = guru.education.ifEmpty { "S1 Pendidikan PCR" },
                        gender = guru.gender.ifEmpty { "Laki-laki" },
                        phone = guru.phone,
                        email = guru.email,
                        tentang = guru.bio.ifEmpty { "Saya seorang guru profesional" },
                        siswa = guru.studentCount,
                        pengalaman = calculateExperience(guru.createdAt), // Calculate experience from createdAt
                        rating = if (guru.averageRating > 0) guru.averageRating else guru.rating,
                        ulasanCount = guru.totalRating,
                        gambar = guru.avatar
                    )

                    // Navigate to DetailGuru fragment
                    context.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_container, detailFragment)
                        .addToBackStack(null) // Add to back stack so user can navigate back
                        .commit()
                }
            }
        }

        // Helper function to calculate experience in years from createdAt timestamp
        private fun calculateExperience(createdAt: Long): Int {
            if (createdAt == 0L) return 0

            val currentTime = System.currentTimeMillis()
            val timeDifference = currentTime - createdAt
            val yearsExperience = (timeDifference / (1000L * 60 * 60 * 24 * 365)).toInt()

            return if (yearsExperience < 1) 1 else yearsExperience // Minimum 1 year
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGuruBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listGuru[position])
    }

    override fun getItemCount(): Int = listGuru.size
}