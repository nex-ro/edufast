package com.example.tampilansiswa.pelajaran

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tampilansiswa.R
import com.example.tampilansiswa.Data.Course
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions

class CoursePagingAdapter(
    options: FirestorePagingOptions<Course>,
    private val onItemClick: (Course) -> Unit
) : FirestorePagingAdapter<Course, CoursePagingAdapter.CourseViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_pelajaran, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int, model: Course) {
        holder.bind(model, onItemClick)
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val courseName: TextView = itemView.findViewById(R.id.tv_course_name)
        private val subject: TextView = itemView.findViewById(R.id.tv_subject)
        private val level: TextView = itemView.findViewById(R.id.tv_level)
        private val courseType: TextView = itemView.findViewById(R.id.tv_course_type)
        private val duration: TextView = itemView.findViewById(R.id.tv_duration)
        private val location: TextView = itemView.findViewById(R.id.tv_location)
        private val date: TextView = itemView.findViewById(R.id.tv_date)
        private val startTime: TextView = itemView.findViewById(R.id.tv_start_time)
        private val price: TextView = itemView.findViewById(R.id.tv_price)
        private val image: ImageView = itemView.findViewById(R.id.iv_course_image)

        fun bind(course: Course, onItemClick: (Course) -> Unit) {
            courseName.text = course.courseName
            subject.text = course.subject
            level.text = course.level
            courseType.text = course.courseType
            duration.text = course.formattedDuration
            location.text = course.fullLocation
            date.text = course.date
            startTime.text = course.startTime
            price.text = course.formattedPrice

            // Load image with Glide
            if (course.poster.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(course.poster)
                    .placeholder(R.drawable.avatar1)
                    .error(R.drawable.avatar1)
                    .centerCrop()
                    .into(image)
            } else {
                image.setImageResource(R.drawable.avatar1)
            }

            // Set click listener
            itemView.setOnClickListener {
                if (course.id.isNotEmpty()) {
                    onItemClick(course)
                }
            }

            // Add ripple effect
            itemView.isClickable = true
            itemView.isFocusable = true
        }
    }
}