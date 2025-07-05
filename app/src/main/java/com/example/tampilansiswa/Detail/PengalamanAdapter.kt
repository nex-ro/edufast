package com.example.tampilansiswa.Detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.Data.Pengalaman
import com.example.tampilansiswa.R

class PengalamanAdapter(
    private val pengalamanList: List<Pengalaman>
) : RecyclerView.Adapter<PengalamanAdapter.PengalamanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PengalamanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pengalamans, parent, false)
        return PengalamanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PengalamanViewHolder, position: Int) {
        holder.bind(pengalamanList[position])
    }

    override fun getItemCount(): Int = pengalamanList.size

    class PengalamanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val namaPengalaman: TextView = itemView.findViewById(R.id.tvNamaPengalaman)
        private val jabatan: TextView = itemView.findViewById(R.id.tvJabatan)
        private val tahun: TextView = itemView.findViewById(R.id.tvTahun)

        fun bind(pengalaman: Pengalaman) {
            namaPengalaman.text = pengalaman.namaPengalaman
            jabatan.text = pengalaman.jabatan

            // Format tahun
            val tahunText = if (pengalaman.masihBekerja) {
                "${pengalaman.tahunMulai} - Sekarang"
            } else {
                "${pengalaman.tahunMulai} - ${pengalaman.tahunBerakhir}"
            }
            tahun.text = tahunText
        }
    }
}