package com.example.tampilansiswa.guru

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.R
import com.example.tampilansiswa.Data.Guru
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class GuruTerbaikAdapter(
    private var listGuru: List<Guru>,
    private val onItemClick: (Guru) -> Unit
) : RecyclerView.Adapter<GuruTerbaikAdapter.GuruViewHolder>() {

    inner class GuruViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvMapel: TextView = itemView.findViewById(R.id.tvMapel)
        val tvUniversitas: TextView = itemView.findViewById(R.id.tvUniversitas)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(listGuru[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuruViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guru_terbaik, parent, false)
        return GuruViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuruViewHolder, position: Int) {
        val guru = listGuru[position]

        holder.tvNama.text = guru.nama
        holder.tvMapel.text = guru.subjek
        holder.tvUniversitas.text = guru.education

        // Handle rating display
        if (guru.rating > 0) {
            holder.tvRating.text = "⭐ ${String.format("%.1f", guru.rating)}"
        } else {
            holder.tvRating.text = "⭐ Unrated"
        }

        // Load profile image
        if (guru.imagePath.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(guru.imagePath)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.avatar1)
                .error(R.drawable.avatar1)
                .into(holder.imgAvatar)
        } else {
            // Use default avatar based on position or gender
            val defaultAvatar = when {
                guru.gender.equals("female", ignoreCase = true) -> R.drawable.avatar3
                guru.gender.equals("male", ignoreCase = true) -> R.drawable.avatar2
                else -> getDefaultAvatarByPosition(position)
            }
            holder.imgAvatar.setImageResource(defaultAvatar)
        }
    }

    override fun getItemCount(): Int = listGuru.size

    fun updateData(newGuruList: List<Guru>) {
        listGuru = newGuruList
        notifyDataSetChanged()
    }

    private fun getDefaultAvatarByPosition(position: Int): Int {
        val avatars = listOf(
            R.drawable.avatar1,
            R.drawable.avatar2,
            R.drawable.avatar3,
            R.drawable.avatar4
        )
        return avatars[position % avatars.size]
    }
}