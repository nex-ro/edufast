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

        // Load profile image
        if (!user?.profileImageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user?.profileImageUrl)
                .placeholder(R.drawable.avatar1)
                .error(R.drawable.avatar1)
                .into(holder.imgProfile)
        } else {
            holder.imgProfile.setImageResource(R.drawable.avatar1)
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
                holder.btnSecond.setOnClickListener { onButtonClick(course, "accept") }
                holder.btnThird.setOnClickListener { onButtonClick(course, "reject") }
            }
            "upcoming", "approved", "confirmed" -> {
                holder.btnFirst.text = "Ubah Jadwal"
                holder.btnSecond.text = "Selesaikan"
                holder.btnThird.visibility = View.GONE
                holder.btnFirst.visibility = View.VISIBLE
                holder.btnSecond.visibility = View.VISIBLE

                holder.btnFirst.setOnClickListener { onButtonClick(course, "reschedule") }
                holder.btnSecond.setOnClickListener { onButtonClick(course, "complete") }
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