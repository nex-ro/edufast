package com.example.tampilansiswa.GuruPage.Pengalaman

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.Data.Pengalaman
import com.example.tampilansiswa.R

class PengalamanAdapter(
    private val pengalamanList: MutableList<Pengalaman>,
    private val onEditClick: (Pengalaman) -> Unit,
    private val onDeleteClick: (Pengalaman) -> Unit
) : RecyclerView.Adapter<PengalamanAdapter.PengalamanViewHolder>() {

    override fun getItemCount(): Int {
        return pengalamanList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PengalamanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pengalaman, parent, false)
        return PengalamanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PengalamanViewHolder, position: Int) {
        if (position >= pengalamanList.size) {
            return
        }

        val pengalaman = pengalamanList[position]

        holder.tvNamaPengalaman.text = pengalaman.namaPengalaman
        holder.tvJabatan.text = pengalaman.jabatan

        val tahunText = if (pengalaman.masihBekerja) {
            "${pengalaman.tahunMulai} - sekarang"
        } else {
            "${pengalaman.tahunMulai} - ${pengalaman.tahunBerakhir}"
        }
        holder.tvTahun.text = tahunText

        holder.btnEdit.setOnClickListener {
            onEditClick(pengalaman)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(pengalaman)
        }
    }

    class PengalamanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaPengalaman: TextView = itemView.findViewById(R.id.tvNamaPengalaman)
        val tvJabatan: TextView = itemView.findViewById(R.id.tvJabatan)
        val tvTahun: TextView = itemView.findViewById(R.id.tvTahun)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }
}