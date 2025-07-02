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
    options: FirestorePagingOptions<Course>
) : FirestorePagingAdapter<Course, CoursePagingAdapter.CourseViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_pelajaran, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int, model: Course) {
        holder.courseName.text = model.courseName
        holder.subject.text = model.subject
        holder.level.text = model.level
        holder.courseType.text = model.courseType
        holder.duration.text = model.formattedDuration
        holder.location.text = model.fullLocation
        holder.date.text = model.date
        holder.startTime.text = model.startTime
        holder.price.text = model.formattedPrice
        holder.status.text = model.statusText

        val context = holder.itemView.context
        if (!model.poster.isNullOrEmpty()) {
            Glide.with(context)
                .load(model.poster)
                .placeholder(R.drawable.avatar1)
                .into(holder.image)
        } else {
            holder.image.setImageResource(R.drawable.avatar1)
        }
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseName: TextView = itemView.findViewById(R.id.tv_course_name)
        val subject: TextView = itemView.findViewById(R.id.tv_subject)
        val level: TextView = itemView.findViewById(R.id.tv_level)
        val courseType: TextView = itemView.findViewById(R.id.tv_course_type)
        val duration: TextView = itemView.findViewById(R.id.tv_duration)
        val location: TextView = itemView.findViewById(R.id.tv_location)
        val date: TextView = itemView.findViewById(R.id.tv_date)
        val startTime: TextView = itemView.findViewById(R.id.tv_start_time)
        val price: TextView = itemView.findViewById(R.id.tv_price)
        val status: TextView = itemView.findViewById(R.id.tv_status)
        val image: ImageView = itemView.findViewById(R.id.iv_course_image)
    }
}
