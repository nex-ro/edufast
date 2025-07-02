package com.example.tampilansiswa.guru

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.R
import com.example.tampilansiswa.Data.GuruTerbaik

class GuruTerbaikAdapter(private val listGuru: List<GuruTerbaik>) : RecyclerView.Adapter<GuruTerbaikAdapter.GuruViewHolder>() {

    inner class GuruViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvMapel: TextView = itemView.findViewById(R.id.tvMapel)
        val tvUniversitas: TextView = itemView.findViewById(R.id.tvUniversitas)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val tvJarak: TextView = itemView.findViewById(R.id.tvJarak)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuruViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guru_terbaik, parent, false)
        return GuruViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuruViewHolder, position: Int) {
        val guru = listGuru[position]
        holder.tvNama.text = guru.nama
        holder.tvMapel.text = guru.mapel
        holder.tvUniversitas.text = guru.universitas
        holder.tvRating.text = "⭐ ${guru.rating}"
        holder.tvJarak.text = guru.jarak
        holder.imgAvatar.setImageResource(guru.avatarResId)
    }

    override fun getItemCount(): Int = listGuru.size
}
