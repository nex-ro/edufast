package com.example.tampilansiswa.GuruPage.ListSiswa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tampilansiswa.Data.enrollments
import com.example.tampilansiswa.R
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class StudentInfo(
    val nama: String = "",
    val imagePath: String = "",
    val gender: String = ""
)

class SiswaAdapter(private val enrollmentsList: List<enrollments>) :
    RecyclerView.Adapter<SiswaAdapter.SiswaViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val studentInfoCache = mutableMapOf<String, StudentInfo>()

    class SiswaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewProfile: ImageView = itemView.findViewById(R.id.imageViewProfile)
        val textViewNama: TextView = itemView.findViewById(R.id.textViewNama)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
        val textViewKelas: TextView = itemView.findViewById(R.id.textViewKelas)
        val textViewTanggal: TextView = itemView.findViewById(R.id.textViewTanggal)
        val textViewJumlah: TextView = itemView.findViewById(R.id.textViewJumlah)
        val textViewGender: TextView = itemView.findViewById(R.id.textViewGender)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiswaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_siswa, parent, false)
        return SiswaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SiswaViewHolder, position: Int) {
        val enrollment = enrollmentsList[position]
        val studentId = enrollment.studentId

        // Check if student info is already cached
        if (studentInfoCache.containsKey(studentId)) {
            bindStudentData(holder, enrollment, studentInfoCache[studentId]!!)
        } else {
            // Load student data from Firestore
            loadStudentData(studentId, holder, enrollment)
        }
    }

    private fun loadStudentData(studentId: String, holder: SiswaViewHolder, enrollment: enrollments) {
        db.collection("users")
            .document(studentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val studentInfo = StudentInfo(
                        nama = document.getString("nama") ?: "Nama tidak tersedia",
                        imagePath = document.getString("imagePath") ?: "",
                        gender = document.getString("gender") ?: ""
                    )

                    // Cache the student info
                    studentInfoCache[studentId] = studentInfo

                    // Bind data to views
                    bindStudentData(holder, enrollment, studentInfo)
                } else {
                    // Student not found, use default values
                    val defaultInfo = StudentInfo(
                        nama = "Nama tidak ditemukan",
                        imagePath = "",
                        gender = ""
                    )
                    bindStudentData(holder, enrollment, defaultInfo)
                }
            }
            .addOnFailureListener {
                // Error loading student data, use default values
                val defaultInfo = StudentInfo(
                    nama = "Error memuat nama",
                    imagePath = "",
                    gender = ""
                )
                bindStudentData(holder, enrollment, defaultInfo)
            }
    }

    private fun bindStudentData(holder: SiswaViewHolder, enrollment: enrollments, studentInfo: StudentInfo) {
        // Set student name
        holder.textViewNama.text = studentInfo.nama

        // Set gender
        holder.textViewGender.text = studentInfo.gender
        holder.textViewGender.visibility = if (studentInfo.gender.isNotEmpty()) View.VISIBLE else View.GONE

        // Load profile image
        if (studentInfo.imagePath.isNotEmpty()) {
            val imageFile = File(studentInfo.imagePath)
            if (imageFile.exists()) {
                Glide.with(holder.itemView.context)
                    .load(imageFile)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(holder.imageViewProfile)
            } else {
                // Set default profile image based on gender
                val defaultImage = when (studentInfo.gender.lowercase()) {
                    "laki-laki" -> R.drawable.avatar1
                    "perempuan" -> R.drawable.avatar2
                    else -> R.drawable.ic_person
                }
                holder.imageViewProfile.setImageResource(defaultImage)
            }
        } else {
            // Set default profile image based on gender
            val defaultImage = when (studentInfo.gender.lowercase()) {
                "laki-laki" -> R.drawable.avatar1
                "perempuan" -> R.drawable.avatar2
                else -> R.drawable.ic_person
            }
            holder.imageViewProfile.setImageResource(defaultImage)
        }

        // Set status with improved styling
        holder.textViewStatus.text = enrollment.status.uppercase()
        when (enrollment.status.lowercase()) {
            "upcoming" -> {
                holder.textViewStatus.setTextColor(
                    holder.itemView.context.getColor(R.color.status_upcoming)
                )
                holder.textViewStatus.setBackgroundResource(R.drawable.status_upcoming_background)
            }
            "selesai" -> {
                holder.textViewStatus.setTextColor(
                    holder.itemView.context.getColor(R.color.status_completed)
                )
                holder.textViewStatus.setBackgroundResource(R.drawable.status_completed_background)
            }
            else -> {
                holder.textViewStatus.setTextColor(
                    holder.itemView.context.getColor(R.color.status_default)
                )
                holder.textViewStatus.setBackgroundResource(R.drawable.status_default_background)
            }
        }

        // Set course name
        holder.textViewKelas.text = enrollment.courseName

        // Set created date
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val createdDate = enrollment.createdAtDate
        holder.textViewTanggal.text = if (createdDate != null) {
            dateFormat.format(createdDate)
        } else {
            "Tanggal tidak tersedia"
        }

        // Set amount with better formatting
        holder.textViewJumlah.text = "Rp ${String.format("%,d", enrollment.amount)}"
    }

    override fun getItemCount(): Int {
        return enrollmentsList.size
    }
}