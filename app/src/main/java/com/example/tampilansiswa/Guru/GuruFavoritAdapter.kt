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

class GuruFavoritAdapter(
    private var listGuru: List<Guru>,
    private val onItemClick: (Guru) -> Unit
) : RecyclerView.Adapter<GuruFavoritAdapter.GuruViewHolder>() {

    inner class GuruViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvMapel: TextView = itemView.findViewById(R.id.tvMapel)
        val tvDomisili: TextView = itemView.findViewById(R.id.tvdomisili)
        val tvUniversitas: TextView = itemView.findViewById(R.id.tvUniversitas)
        val tvFavorit: TextView = itemView.findViewById(R.id.tvFavorit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuruViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guru_favorit, parent, false)
        return GuruViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuruViewHolder, position: Int) {
        val guru = listGuru[position]

        holder.tvNama.text = guru.nama
        holder.tvMapel.text = guru.subjek
        holder.tvDomisili.text = guru.domisili
        holder.tvUniversitas.text = guru.education

        // Display total student count (siswa + studentCount)
        val totalStudents = guru.siswa + guru.studentCount
        holder.tvFavorit.text = totalStudents.toString()

        // Load profile image
        if (guru.imagePath.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(guru.imagePath)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.avatar1)
                .error(R.drawable.avatar1)
                .into(holder.imgAvatar)
        } else if (guru.profileImageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(guru.profileImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.avatar1)
                .error(R.drawable.avatar1)
                .into(holder.imgAvatar)
        } else {
            // Use default avatar based on gender or position
            val defaultAvatar = when {
                guru.gender.equals("female", ignoreCase = true) -> R.drawable.avatar3
                guru.gender.equals("male", ignoreCase = true) -> R.drawable.avatar2
                else -> getDefaultAvatarByPosition(position)
            }
            holder.imgAvatar.setImageResource(defaultAvatar)
        }

        // Set click listener untuk navigasi ke detail
        holder.itemView.setOnClickListener {
            onItemClick(guru)
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