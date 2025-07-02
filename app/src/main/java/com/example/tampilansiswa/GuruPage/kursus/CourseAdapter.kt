package com.example.tampilansiswa.GuruPage.kursus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.R

class CourseAdapter(
    private var courses: MutableList<Course> = mutableListOf(),
    private val onItemClick: (Course) -> Unit = {},
    private val onEditClick: (Course) -> Unit = {},
    private val onToggleStatusClick: (Course) -> Unit = {},
    private val onDeleteClick: (Course) -> Unit = {}
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCourseName: TextView = itemView.findViewById(R.id.tvCourseName)
        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnToggleStatus: Button = itemView.findViewById(R.id.btnToggleStatus)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]

        with(holder) {
            tvCourseName.text = course.courseName
            tvSubject.text = course.subject
            tvDate.text = course.date
            tvTime.text = course.startTime
            tvDuration.text = course.getFormattedDuration()
            tvLocation.text = course.getFullLocation()
            tvPrice.text = course.getFormattedPrice()

            // Status
            tvStatus.text = course.getStatusText()
            val statusColor = if (course.active) {
                ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark)
            } else {
                ContextCompat.getColor(itemView.context, android.R.color.darker_gray)
            }
            tvStatus.setBackgroundColor(statusColor)

            // Toggle status button text
            btnToggleStatus.text = if (course.active) "Non-Aktifkan" else "Aktifkan"

            // Listeners
            itemView.setOnClickListener { onItemClick(course) }
            btnEdit.setOnClickListener { onEditClick(course) }
            btnToggleStatus.setOnClickListener { onToggleStatusClick(course) }
            btnDelete.setOnClickListener { onDeleteClick(course) }
        }
    }

    override fun getItemCount(): Int = courses.size

    fun updateCourses(newCourses: List<Course>) {
        courses.clear()
        courses.addAll(newCourses)
        notifyDataSetChanged()
    }

    fun addCourse(course: Course) {
        courses.add(0, course)
        notifyItemInserted(0)
    }

    fun removeCourse(courseId: String) {
        val index = courses.indexOfFirst { it.id == courseId }
        if (index != -1) {
            courses.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun updateCourse(updatedCourse: Course) {
        val index = courses.indexOfFirst { it.id == updatedCourse.id }
        if (index != -1) {
            courses[index] = updatedCourse
            notifyItemChanged(index)
        }
    }
}
