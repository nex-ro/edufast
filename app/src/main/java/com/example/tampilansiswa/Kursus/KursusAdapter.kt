// KursusAdapter.kt
package com.example.tampilansiswa.Kursus

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.Data.Kursus
import com.example.tampilansiswa.Ulasan.UlasanActivity
import com.example.tampilansiswa.databinding.ItemKursusBinding

class KursusAdapter(private val list: MutableList<Kursus>) :
    RecyclerView.Adapter<KursusAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemKursusBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Kursus, position: Int) {
            binding.txtNama.text = data.namaGuru
            binding.txtWaktu.text = "${data.waktu} - ${data.tanggal}"
            binding.imgGuru.setImageResource(data.avatar)

            binding.txtStatus.text = if (data.status == "Selesai") "Selesai" else "Batalkan"
            binding.txtStatus.setTextColor(
                binding.root.context.getColor(
                    if (data.status == "Selesai") android.R.color.holo_green_dark
                    else android.R.color.holo_red_dark
                )
            )

            if (data.status == "Selesai") {
                binding.viewLine.visibility = View.VISIBLE
                binding.layoutTombol.visibility = View.VISIBLE

                binding.btnBeriUlasan.setOnClickListener {
                    val context = binding.root.context
                    val intent = Intent(context, UlasanActivity::class.java)
                    context.startActivity(intent)
                }

                binding.btnJadwalUlang.setOnClickListener {
                    val context = binding.root.context
                    val intent = Intent(context, com.example.tampilansiswa.Pemesanan.PemesananActivity::class.java)
                    context.startActivity(intent)
                }
            } else {
                binding.viewLine.visibility = View.GONE
                binding.layoutTombol.visibility = View.GONE

                binding.txtStatus.setOnClickListener {
                    val context = binding.root.context
                    AlertDialog.Builder(context)
                        .setTitle("Konfirmasi")
                        .setMessage("Batalkan kursus ini?")
                        .setPositiveButton("Ya") { _, _ ->
                            list.removeAt(position)
                            notifyItemRemoved(position)
                        }
                        .setNegativeButton("Tidak", null)
                        .show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKursusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount(): Int = list.size
}
