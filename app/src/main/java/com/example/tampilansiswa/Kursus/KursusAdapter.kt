package com.example.tampilansiswa.Kursus

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.Data.Kursus
import com.example.tampilansiswa.Ulasan.UlasanActivity
import com.example.tampilansiswa.databinding.ItemKursusBinding
import com.example.tampilansiswa.R

class KursusAdapter(private val list: MutableList<Kursus>) :
    RecyclerView.Adapter<KursusAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemKursusBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Kursus, position: Int) {
            // Set basic course info
            binding.txtCourseName.text = data.courseName
            binding.txtNama.text = data.namaGuru
            binding.txtWaktu.text = "${data.waktu} - ${data.tanggal}"
            binding.txtLocation.text = data.fullLocation
            binding.txtPrice.text = data.formattedPrice
            binding.txtDuration.text = data.formattedDuration
            binding.txtCourseType.text = data.courseType
            binding.imgGuru.setImageResource(data.avatar)

            // Set status
            binding.txtStatus.text = data.status
            setStatusColor(data.status)

            // Configure buttons based on status
            configureButtons(data.status)

            // Set button click listeners
            setupButtonListeners(data)
        }

        private fun setStatusColor(status: String) {
            val context = binding.root.context
            when (status.lowercase()) {
                "pending" -> {
                    binding.txtStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark))
                    binding.txtStatus.setBackgroundResource(R.drawable.bg_status_orange)
                }
                "approved", "confirmed" -> {
                    binding.txtStatus.setTextColor(context.getColor(android.R.color.holo_blue_dark))
                    binding.txtStatus.setBackgroundResource(R.drawable.bg_status_blue)
                }
                "completed", "selesai" -> {
                    binding.txtStatus.setTextColor(context.getColor(android.R.color.holo_green_dark))
                    binding.txtStatus.setBackgroundResource(R.drawable.bg_status_green)
                }
                "cancelled", "cancel" -> {
                    binding.txtStatus.setTextColor(context.getColor(android.R.color.holo_red_dark))
                    binding.txtStatus.setBackgroundResource(R.drawable.bg_status_red)
                }
            }
        }

        private fun configureButtons(status: String) {
            when (status.lowercase()) {
                "pending", "approved", "confirmed", "cancelled", "cancel" -> {
                    // Show only "Lihat Bukti" button
                    binding.btnLihatBukti.visibility = View.VISIBLE
                    binding.btnJadwalUlang.visibility = View.GONE
                    binding.btnBeriUlasan.visibility = View.GONE
                    binding.viewLine.visibility = View.VISIBLE
                    binding.layoutTombol.visibility = View.VISIBLE
                }
                "completed", "selesai" -> {
                    // Show "Jadwal Ulang" and "Beri Ulasan" buttons
                    binding.btnLihatBukti.visibility = View.GONE
                    binding.btnJadwalUlang.visibility = View.VISIBLE
                    binding.btnBeriUlasan.visibility = View.VISIBLE
                    binding.viewLine.visibility = View.VISIBLE
                    binding.layoutTombol.visibility = View.VISIBLE
                }
                else -> {
                    // Hide all buttons
                    binding.viewLine.visibility = View.GONE
                    binding.layoutTombol.visibility = View.GONE
                }
            }
        }

        private fun setupButtonListeners(data: Kursus) {
            val context = binding.root.context

            // Lihat Bukti button
            binding.btnLihatBukti.setOnClickListener {
                // Navigate to payment proof activity
                // You can create a new activity to show payment proof
                Toast.makeText(context, "Lihat Bukti: ${data.courseName}", Toast.LENGTH_SHORT).show()

                // Example: Navigate to payment proof activity
                // val intent = Intent(context, PaymentProofActivity::class.java)
                // intent.putExtra("enrollmentId", data.enrollmentId)
                // context.startActivity(intent)
            }

            // Jadwal Ulang button (only visible for completed courses)
            binding.btnJadwalUlang.setOnClickListener {
                val intent = Intent(context, com.example.tampilansiswa.Pemesanan.PemesananActivity::class.java)
                intent.putExtra("courseId", data.courseId)
                intent.putExtra("teacherId", data.teacherId)
                context.startActivity(intent)
            }

            // Beri Ulasan button (only visible for completed courses)
            binding.btnBeriUlasan.setOnClickListener {
                val intent = Intent(context, UlasanActivity::class.java)
                intent.putExtra("courseId", data.courseId)
                intent.putExtra("teacherId", data.teacherId)
                intent.putExtra("enrollmentId", data.enrollmentId)
                context.startActivity(intent)
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