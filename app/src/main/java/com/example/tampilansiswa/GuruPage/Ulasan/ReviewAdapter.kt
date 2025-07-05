package com.example.tampilansiswa.GuruPage.Ulasan

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.Data.Review
import com.example.tampilansiswa.R
import com.google.firebase.firestore.FirebaseFirestore

class ReviewAdapter(private val reviewsList: List<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "ReviewAdapter"
    }

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReviewer: TextView = itemView.findViewById(R.id.tvReviewer)
        val tvStar: TextView = itemView.findViewById(R.id.tvStar)
        val tvComment: TextView = itemView.findViewById(R.id.tvComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        return try {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_review, parent, false)
            ReviewViewHolder(view)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating view holder: ${e.message}", e)
            throw e
        }
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        try {
            if (position < 0 || position >= reviewsList.size) {
                Log.w(TAG, "Invalid position: $position, list size: ${reviewsList.size}")
                return
            }

            val review = reviewsList[position]
            Log.d(TAG, "Binding review at position $position: ${review.studentId}")

            // Load student name from users collection
            loadStudentName(review.studentId, holder.tvReviewer)

            // Set rating stars
            holder.tvStar.text = getStarRating(review.rating)

            // Set comment
            holder.tvComment.text = if (review.comment.isNotEmpty()) {
                review.comment
            } else {
                "Tidak ada komentar"
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error binding view holder at position $position: ${e.message}", e)
            // Set default values in case of error
            holder.tvReviewer.text = "Annoymus"
            holder.tvStar.text = "☆☆☆☆☆"
            holder.tvComment.text = "Error loading review"
        }
    }

    private fun loadStudentName(studentId: String, textView: TextView) {
        if (studentId.isEmpty()) {
            Log.w(TAG, "Student ID is empty")
            textView.text = "Annoymus"
            return
        }

        try {
            Log.d(TAG, "Loading student name for ID: $studentId")

            db.collection("users")
                .document(studentId)
                .get()
                .addOnSuccessListener { document ->
                    try {
                        if (document.exists()) {
                            val name = document.getString("name")
                                ?: document.getString("fullName")
                                ?: "Annoymus"
                            textView.text = name
                            Log.d(TAG, "Loaded student name: $name")
                        } else {
                            Log.w(TAG, "Student document not found for ID: $studentId")
                            textView.text = "Annoymus"
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing student document: ${e.message}", e)
                        textView.text = "Annoymus"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error loading student name: ${exception.message}", exception)
                    textView.text = "Annoymus"
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up student name query: ${e.message}", e)
            textView.text = "Unknown Student"
        }
    }

    private fun getStarRating(rating: Int): String {
        return try {
            val maxStars = 5
            val filledStar = "★"
            val emptyStar = "☆"

            // Ensure rating is within valid range
            val validRating = when {
                rating < 0 -> 0
                rating > maxStars -> maxStars
                else -> rating
            }

            val stars = StringBuilder()
            for (i in 1..maxStars) {
                if (i <= validRating) {
                    stars.append(filledStar)
                } else {
                    stars.append(emptyStar)
                }
            }
            stars.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating star rating: ${e.message}", e)
            "☆☆☆☆☆"
        }
    }

    override fun getItemCount(): Int {
        return reviewsList.size
    }
}