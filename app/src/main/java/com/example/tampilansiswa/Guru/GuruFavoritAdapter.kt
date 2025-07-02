package com.example.tampilansiswa.Guru

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.R
import com.example.tampilansiswa.Data.GuruFavorit

class GuruFavoritAdapter(private val listGuru: List<GuruFavorit>) :
    RecyclerView.Adapter<GuruFavoritAdapter.GuruViewHolder>() {

    inner class GuruViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNama)
        val tvMapel: TextView = itemView.findViewById(R.id.tvMapel)
        val tvUniversitas: TextView = itemView.findViewById(R.id.tvUniversitas)
        val tvJarak: TextView = itemView.findViewById(R.id.tvJarak)
        val tvFavorit: TextView = itemView.findViewById(R.id.tvFavorit)
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuruViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guru_favorit, parent, false)
        return GuruViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuruViewHolder, position: Int) {
        val guru = listGuru[position]
        holder.tvNama.text = guru.nama
        holder.tvMapel.text = guru.mapel
        holder.tvUniversitas.text = guru.universitas
        holder.tvJarak.text = guru.jarak
        holder.tvFavorit.text = guru.totalFavorit.toString()
        holder.imgAvatar.setImageResource(guru.avatarResId)
    }

    override fun getItemCount(): Int = listGuru.size
}
