package com.example.tampilansiswa.GuruPage.siswa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tampilansiswa.Data.enrollments
import com.example.tampilansiswa.Data.users
import com.example.tampilansiswa.R
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import android.util.Log
class CourseAdapter(
    private var courses: List<enrollments>,
    private var users: Map<String, users> = emptyMap(),
    private val onButtonClick: (enrollments, String) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.imgGuru)
        val txtName: TextView = itemView.findViewById(R.id.txtNama)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val txtTime: TextView = itemView.findViewById(R.id.txtWaktu)
        val txtPhone: TextView = itemView.findViewById(R.id.txtPhone)
        val txtCourseName: TextView = itemView.findViewById(R.id.txtCourseName)
        val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
        val layoutButtons: LinearLayout = itemView.findViewById(R.id.layoutTombol)
        val btnFirst: Button = itemView.findViewById(R.id.btnFirst)
        val btnSecond: Button = itemView.findViewById(R.id.btnSecond)
        val btnThird: Button = itemView.findViewById(R.id.btnThird)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kursus_guru, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        val user = users[course.studentId]

        // Set user data
        holder.txtName.text = user?.nama ?: "Unknown User"
        holder.txtPhone.text = user?.phone ?: "No Phone"

        // Load profile image dengan prioritas: profileImageUrl -> imagePath -> default
        val context = holder.itemView.context
        when {
            // Prioritas 1: profileImageUrl (URL dari internet)
            !user?.imagePath.isNullOrEmpty() -> {
                Glide.with(context)
                    .load(user?.imagePath)
                    .placeholder(R.drawable.avatar1)
                    .error(R.drawable.avatar1)
                    .into(holder.imgProfile)
            }
            // Prioritas 2: imagePath (path lokal)
            !user?.imagePath.isNullOrEmpty() -> {
                val imagePath = user?.imagePath
                if (imagePath?.startsWith("/data/user/") == true) {
                    // Jika path lokal, gunakan File
                    val file = java.io.File(imagePath)
                    if (file.exists()) {
                        Glide.with(context)
                            .load(file)
                            .placeholder(R.drawable.avatar1)
                            .error(R.drawable.avatar1)
                            .into(holder.imgProfile)
                    } else {
                        // File tidak ada, gunakan default
                        holder.imgProfile.setImageResource(R.drawable.avatar1)
                    }
                } else {
                    // Jika bukan path lokal, mungkin URL
                    Glide.with(context)
                        .load(imagePath)
                        .placeholder(R.drawable.avatar1)
                        .error(R.drawable.avatar1)
                        .into(holder.imgProfile)
                }
            }
            // Prioritas 3: Default avatar
            else -> {
                holder.imgProfile.setImageResource(R.drawable.avatar1)
            }
        }

        // Set course data
        holder.txtCourseName.text = course.courseName
        holder.txtAmount.text = "Rp ${formatCurrency(course.amount)}"

        // Format date using helper method from enrollments data class
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val formattedDate = course.createdAtDate?.let { date ->
            dateFormat.format(date)
        } ?: "Unknown Date"
        holder.txtTime.text = formattedDate

        // Set status
        holder.txtStatus.text = course.status.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        setStatusBackground(holder.txtStatus, course.status)

        // Setup buttons based on status
        setupButtons(holder, course)
    }

    private fun setupButtons(holder: CourseViewHolder, course: enrollments) {
        when (course.status.lowercase()) {
            "pending" -> {
                holder.btnFirst.text = "Lihat Bukti"
                holder.btnSecond.text = "Terima"
                holder.btnThird.text = "Tolak"
                holder.btnFirst.visibility = View.VISIBLE
                holder.btnSecond.visibility = View.VISIBLE
                holder.btnThird.visibility = View.VISIBLE

                holder.btnFirst.setOnClickListener { onButtonClick(course, "view_proof") }
                holder.btnSecond.setOnClickListener {
                    onButtonClick(course, "accept")
                    // Tambahkan notifikasi saat menerima
                    createDualNotification(course, "accept", getCurrentUserId())
                }
                holder.btnThird.setOnClickListener {
                    onButtonClick(course, "reject")
                    // Tambahkan notifikasi saat menolak
                    createDualNotification(course, "reject", getCurrentUserId())
                }
            }
            "upcoming", "approved", "confirmed" -> {
                holder.btnFirst.text = "Ubah Jadwal"
                holder.btnSecond.text = "Selesaikan"
                holder.btnThird.visibility = View.GONE
                holder.btnFirst.visibility = View.GONE
                holder.btnSecond.visibility = View.VISIBLE

                holder.btnSecond.setOnClickListener {
                    onButtonClick(course, "complete")
                    // Tambahkan notifikasi saat menyelesaikan
                    createDualNotification(course, "complete", getCurrentUserId())
                }
            }
            "selesai", "completed" -> {
                holder.btnFirst.visibility = View.GONE
                holder.btnSecond.visibility = View.GONE
                holder.btnThird.visibility = View.GONE
            }
            "cancel", "cancelled" -> {
                holder.btnFirst.text = "Lihat Bukti"
                holder.btnSecond.visibility = View.GONE
                holder.btnThird.visibility = View.GONE
                holder.btnFirst.visibility = View.VISIBLE

                holder.btnFirst.setOnClickListener { onButtonClick(course, "view_proof") }
            }
        }
    }
    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
    private fun updateTeacherStudentCount(teacherId: String, increment: Boolean) {
        if (teacherId.isEmpty()) return

        val db = FirebaseFirestore.getInstance()
        val teacherRef = db.collection("users").document(teacherId)

        teacherRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val currentCount = document.getLong("siswa") ?: 0
                    val newCount = if (increment) currentCount + 1 else maxOf(0, currentCount - 1)

                    teacherRef.update("siswa", newCount)
                        .addOnSuccessListener {
                            Log.d("StudentCount", "Successfully updated student count to $newCount")
                        }
                        .addOnFailureListener { e ->
                            Log.e("StudentCount", "Error updating student count: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("StudentCount", "Error getting teacher document: ${e.message}")
            }
    }

    private fun createNotification(
        fromUid: String,
        toUid: String,
        enrollId: String,
        judulPesan: String,
        message: String,
        type: String
    ) {
        val notification = hashMapOf(
            "id" to UUID.randomUUID().toString(),
            "fromUid" to fromUid,
            "toUid" to toUid,
            "enrollId" to enrollId,
            "judulPesan" to judulPesan,
            "message" to message,
            "createdAt" to Timestamp.now(),
            "isRead" to false,
            "type" to type
        )

        FirebaseFirestore.getInstance()
            .collection("notifications")
            .add(notification)
            .addOnSuccessListener {
                Log.d("Notification", "Notification created successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Notification", "Error creating notification: ${e.message}")
            }
    }

    private fun createDualNotification(
        enrollment: enrollments,
        status: String,
        currentUserId: String
    ) {
        val studentId = enrollment.studentId
        val teacherId = enrollment.teacherId

        when (status.lowercase()) {
            "accept", "approved" -> {
                // Update jumlah siswa guru ketika diterima
                updateTeacherStudentCount(teacherId, true)

                // Notifikasi untuk siswa
                createNotification(
                    fromUid = currentUserId,
                    toUid = studentId,
                    enrollId = enrollment.id,
                    judulPesan = "Kelas Diterima",
                    message = "Pesanan kelas ${enrollment.courseName} Anda telah diterima oleh guru. Silakan cek jadwal kelas.",
                    type = "enrollment_confirmation"
                )

                // Notifikasi untuk guru (yang menerima)
                createNotification(
                    fromUid = currentUserId,
                    toUid = teacherId,
                    enrollId = enrollment.id,
                    judulPesan = "Kelas Diterima",
                    message = "Anda telah menerima pesanan kelas ${enrollment.courseName}. Jangan lupa siapkan materi pembelajaran.",
                    type = "enrollment_accepted"
                )
            }

            "reject", "cancelled" -> {
                // Tidak menambah jumlah siswa ketika ditolak

                // Notifikasi untuk siswa
                createNotification(
                    fromUid = currentUserId,
                    toUid = studentId,
                    enrollId = enrollment.id,
                    judulPesan = "Kelas Ditolak",
                    message = "Pesanan kelas ${enrollment.courseName} Anda telah ditolak oleh guru. Coba cari guru lain.",
                    type = "enrollment_rejected"
                )

                // Notifikasi untuk guru (yang menolak)
                createNotification(
                    fromUid = currentUserId,
                    toUid = teacherId,
                    enrollId = enrollment.id,
                    judulPesan = "Kelas Ditolak",
                    message = "Anda telah menolak pesanan kelas ${enrollment.courseName}.",
                    type = "enrollment_rejected"
                )
            }

            "complete", "completed" -> {
                // Notifikasi untuk siswa
                createNotification(
                    fromUid = currentUserId,
                    toUid = studentId,
                    enrollId = enrollment.id,
                    judulPesan = "Kelas Selesai",
                    message = "Kelas ${enrollment.courseName} telah selesai. Terima kasih telah belajar dengan kami!",
                    type = "enrollment_completed"
                )

                // Notifikasi untuk guru (yang menyelesaikan)
                createNotification(
                    fromUid = currentUserId,
                    toUid = teacherId,
                    enrollId = enrollment.id,
                    judulPesan = "Kelas Selesai",
                    message = "Anda telah menyelesaikan kelas ${enrollment.courseName}. Semoga siswa puas dengan pembelajaran.",
                    type = "enrollment_completed"
                )
            }
        }
    }


    private fun setStatusBackground(textView: TextView, status: String) {
        when (status.lowercase()) {
            "pending" -> {
                textView.setBackgroundResource(R.drawable.bg_status_orange)
                textView.setTextColor(textView.context.getColor(R.color.orange))
            }
            "upcoming", "approved", "confirmed" -> {
                textView.setBackgroundResource(R.drawable.bg_status_blue)
                textView.setTextColor(textView.context.getColor(R.color.blue))
            }
            "selesai", "completed" -> {
                textView.setBackgroundResource(R.drawable.bg_status_green)
                textView.setTextColor(textView.context.getColor(R.color.green))
            }
            "cancel", "cancelled" -> {
                textView.setBackgroundResource(R.drawable.bg_status_red)
                textView.setTextColor(textView.context.getColor(R.color.red))
            }
        }
    }

    private fun formatCurrency(amount: Long): String {
        return String.format(Locale("id", "ID"), "%,d", amount)
    }

    fun updateData(newCourses: List<enrollments>, newUsers: Map<String, users>) {
        courses = newCourses
        users = newUsers
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = courses.size
}