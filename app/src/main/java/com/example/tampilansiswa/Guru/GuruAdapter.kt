package com.example.tampilansiswa.Guru

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.tampilansiswa.Data.Guru
import com.example.tampilansiswa.Detail.DetailGuru
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.ItemGuruSiswaBinding
import java.io.File

class GuruAdapter(private val listGuru: List<Guru>) :
    RecyclerView.Adapter<GuruAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemGuruSiswaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(guru: Guru) {
            try {
                // Set nama guru
                binding.txtNama.text = guru.nama.ifEmpty { "Nama tidak tersedia" }

                // Set mata pelajaran/education
                binding.txtMapel.text = guru.education.ifEmpty { "Pendidikan tidak tersedia" }

                // Set jumlah siswa
                val studentCount = when {
                    guru.siswa > 0 -> guru.siswa
                    guru.studentCount > 0 -> guru.studentCount
                    else -> 0
                }
                binding.siswa.text = studentCount.toString()

                // Load image dengan error handling
                loadGuruImage(guru)

                // Set click listener dengan null safety
                setupClickListener(guru, studentCount)

            } catch (e: Exception) {
                Log.e("GuruAdapter", "Error binding guru data: ${e.message}", e)
                // Set default values in case of error
                binding.txtNama.text = "Nama tidak tersedia"
                binding.txtMapel.text = "Pendidikan tidak tersedia"
                binding.siswa.text = "0"
                binding.imgGuru.setImageResource(R.drawable.avatar3)
            }
        }

        private fun loadGuruImage(guru: Guru) {
            try {
                when {
                    guru.imagePath.isNotEmpty() -> {
                        Glide.with(itemView.context)
                            .load(guru.imagePath)
                            .placeholder(guru.avatar)
                            .error(guru.avatar)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .circleCrop()
                            .into(binding.imgGuru)
                    }

                    // Priority 2: imagePath (local file path)
                    guru.imagePath.isNotEmpty() -> {
                        val file = File(guru.imagePath)
                        if (file.exists()) {
                            Glide.with(itemView.context)
                                .load(file)
                                .placeholder(guru.avatar)
                                .error(guru.avatar)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .circleCrop()
                                .into(binding.imgGuru)
                        } else {
                            // Try loading as URL if file doesn't exist
                            Glide.with(itemView.context)
                                .load(guru.imagePath)
                                .placeholder(guru.avatar)
                                .error(guru.avatar)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .circleCrop()
                                .into(binding.imgGuru)
                        }
                    }

                    // Priority 3: Default avatar resource
                    else -> {
                        binding.imgGuru.setImageResource(guru.avatar)
                    }
                }
            } catch (e: Exception) {
                Log.e("GuruAdapter", "Error loading guru image: ${e.message}", e)
                // Fallback to default avatar
                binding.imgGuru.setImageResource(guru.avatar)
            }
        }

        private fun setupClickListener(guru: Guru, studentCount: Int) {
            binding.root.setOnClickListener {
                try {
                    val context = itemView.context

                    // Ensure context is FragmentActivity
                    if (context is FragmentActivity) {
                        if (!context.isDestroyed && !context.isFinishing) {
                            val detailFragment = DetailGuru.newInstance(
                                guruId = guru.uid,
                                nama = guru.nama.ifEmpty { "Nama tidak tersedia" },
                                mapel = guru.education.ifEmpty { "Pendidikan tidak tersedia" },
                                universitas = guru.education.ifEmpty { "Universitas tidak tersedia" },
                                gender = guru.gender.ifEmpty { "Tidak diketahui" },
                                phone = guru.phone.ifEmpty { "Nomor tidak tersedia" },
                                email = guru.email.ifEmpty { "Email tidak tersedia" },
                                tentang = guru.bio.ifEmpty { "Belum ada deskripsi" },
                                siswa = studentCount,
                                pengalaman = calculateExperience(guru.createdAt),
                                rating = if (guru.averageRating > 0) guru.averageRating else guru.rating,
                                ulasanCount = guru.totalRating,
                                gambar = guru.avatar
                            )

                            // Navigate to DetailGuru fragment
                            try {
                                context.supportFragmentManager
                                    .beginTransaction()
                                    .replace(R.id.frame_container, detailFragment)
                                    .addToBackStack(null)
                                    .commit()
                            } catch (e: Exception) {
                                Log.e("GuruAdapter", "Error navigating to DetailGuru: ${e.message}", e)
                            }
                        } else {
                            Log.w("GuruAdapter", "Activity is destroyed or finishing, cannot navigate")
                        }
                    } else {
                        Log.w("GuruAdapter", "Context is not FragmentActivity")
                    }
                } catch (e: Exception) {
                    Log.e("GuruAdapter", "Error in click listener: ${e.message}", e)
                }
            }
        }

        // Helper function to calculate experience in years from createdAt timestamp
        private fun calculateExperience(createdAt: Long): Int {
            return try {
                if (createdAt == 0L) return 1 // Default 1 year if no data

                val currentTime = System.currentTimeMillis()
                val timeDifference = currentTime - createdAt
                val yearsExperience = (timeDifference / (1000L * 60 * 60 * 24 * 365)).toInt()

                // Minimum 1 year, maximum 50 years (reasonable limit)
                when {
                    yearsExperience < 1 -> 1
                    yearsExperience > 50 -> 50
                    else -> yearsExperience
                }
            } catch (e: Exception) {
                Log.e("GuruAdapter", "Error calculating experience: ${e.message}", e)
                1 // Default to 1 year on error
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return try {
            val binding = ItemGuruSiswaBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ViewHolder(binding)
        } catch (e: Exception) {
            Log.e("GuruAdapter", "Error creating ViewHolder: ${e.message}", e)
            throw e
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            if (position >= 0 && position < listGuru.size) {
                val guru = listGuru[position]
                holder.bind(guru)
            } else {
                Log.w("GuruAdapter", "Invalid position: $position, list size: ${listGuru.size}")
            }
        } catch (e: Exception) {
            Log.e("GuruAdapter", "Error binding ViewHolder at position $position: ${e.message}", e)
        }
    }

    override fun getItemCount(): Int {
        return try {
            listGuru.size
        } catch (e: Exception) {
            Log.e("GuruAdapter", "Error getting item count: ${e.message}", e)
            0
        }
    }
}