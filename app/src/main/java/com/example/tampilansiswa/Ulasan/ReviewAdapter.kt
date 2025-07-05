package com.example.tampilansiswa.Ulasan

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.R
import com.example.tampilansiswa.Data.Review
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ReviewAdapter(private val items: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val studentNamesCache = mutableMapOf<String, String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvReviewer: TextView = itemView.findViewById(R.id.tvReviewer)
        private val tvStar: TextView = itemView.findViewById(R.id.tvStar)
        private val tvComment: TextView = itemView.findViewById(R.id.tvComment)
        private val tvDate: TextView? = itemView.findViewById(R.id.tvDate) // Optional date field

        fun bind(review: Review) {
            // Set star rating with color
            val starText = "★".repeat(review.rating) + "☆".repeat(5 - review.rating)
            tvStar.text = starText
            tvStar.setTextColor(getStarColor(review.rating))

            // Set comment
            tvComment.text = review.comment

            // Set date if available
            tvDate?.let { dateView ->
                review.timestamp?.let { timestamp ->
                    val date = timestamp.toDate()
                    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    dateView.text = formatter.format(date)
                    dateView.visibility = View.VISIBLE
                } ?: run {
                    dateView.visibility = View.GONE
                }
            }

            // Load student name
            loadStudentName(review.studentId)
        }

        private fun loadStudentName(studentId: String) {
            // Check cache first
            if (studentNamesCache.containsKey(studentId)) {
                tvReviewer.text = studentNamesCache[studentId]
                return
            }

            // Show loading state
            tvReviewer.text = "Loading..."

            // Fetch from Firestore
            db.collection("users")
                .document(studentId)
                .get()
                .addOnSuccessListener { document ->
                    val studentName = if (document.exists()) {
                        document.getString("nama") ?: "Anonymous User"
                    } else {
                        "Anonymous User"
                    }

                    // Cache the result
                    studentNamesCache[studentId] = studentName

                    // Update UI
                    tvReviewer.text = studentName
                }
                .addOnFailureListener { exception ->
                    Log.e("ReviewAdapter", "Error loading student name", exception)
                    tvReviewer.text = "Anonymous User"
                    studentNamesCache[studentId] = "Anonymous User"
                }
        }

        private fun getStarColor(rating: Int): Int {
            return when {
                rating >= 4 -> Color.parseColor("#4CAF50") // Green for good ratings
                rating >= 3 -> Color.parseColor("#FF9800") // Orange for average ratings
                else -> Color.parseColor("#F44336") // Red for poor ratings
            }
        }
    }

    fun updateReviews(newReviews: List<Review>) {
        (items as MutableList).clear()
        items.addAll(newReviews)
        notifyDataSetChanged()
    }

    fun clearCache() {
        studentNamesCache.clear()
    }
}