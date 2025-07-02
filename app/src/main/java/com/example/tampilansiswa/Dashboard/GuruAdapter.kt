package com.example.tampilansiswa.Dashboard


import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tampilansiswa.Data.Guru
import com.example.tampilansiswa.Detail.DetailGuruActivity
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.ItemGuruBinding
import java.io.File

class GuruAdapter(private val listGuru: List<Guru>) :
    RecyclerView.Adapter<GuruAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemGuruBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(guru: Guru) {
            binding.txtNama.text = guru.nama
            binding.txtMapel.text = guru.subject

            // Display rating with star emoji or "Unrated"
            if (guru.rating > 0) {
                binding.txtRating.text = "⭐ ${String.format("%.1f", guru.rating)}"
            } else {
                binding.txtRating.text = "⭐ Unrated"
            }

            // Load image using Glide if imagePath exists, otherwise use default avatar
            if (guru.imagePath.isNotEmpty() && File(guru.imagePath).exists()) {
                Glide.with(itemView.context)
                    .load(File(guru.imagePath))
                    .placeholder(guru.avatar)
                    .error(guru.avatar)
                    .circleCrop()
                    .into(binding.imgGuru)
            } else {
                // Use default avatar resource
                binding.imgGuru.setImageResource(guru.avatar)
            }

            // Event klik item
            binding.root.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, DetailGuruActivity::class.java).apply {
                    // Pass all guru data to detail activity
                    putExtra("nama", guru.nama)
                    putExtra("subject", guru.subject)
                    putExtra("rating", guru.rating)
                    putExtra("avatar", guru.avatar)
                    putExtra("email", guru.email)
                    putExtra("phone", guru.phone)
                    putExtra("uid", guru.uid)
                    putExtra("imagePath", guru.imagePath)
                    putExtra("ratingText", guru.ratingText)

                    // For backward compatibility, also pass as "mapel" and "gambar"
                    putExtra("mapel", guru.subject)
                    putExtra("gambar", guru.avatar)
                }
                context.startActivity(intent)
            }
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