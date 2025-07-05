package com.example.tampilansiswa.Ulasan

import android.util.Log
import com.example.tampilansiswa.Data.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReviewManager {

    private val db = FirebaseFirestore.getInstance()

    interface ReviewCallback {
        fun onReviewsLoaded(reviews: List<Review>)
        fun onError(error: String)
    }

    /**
     * Load reviews for a specific teacher
     */
    fun loadTeacherReviews(teacherId: String, callback: ReviewCallback) {
        db.collection("reviews")
            .whereEqualTo("teacherId", teacherId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val reviews = documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Review::class.java)
                    } catch (e: Exception) {
                        Log.e("ReviewManager", "Error parsing review: ", e)
                        null
                    }
                }
                callback.onReviewsLoaded(reviews)
            }
            .addOnFailureListener { exception ->
                Log.e("ReviewManager", "Error loading teacher reviews: ", exception)
                callback.onError("Failed to load reviews")
            }
    }

    /**
     * Load reviews for a specific course
     */
    fun loadCourseReviews(courseId: String, callback: ReviewCallback) {
        db.collection("reviews")
            .whereEqualTo("courseId", courseId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val reviews = documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Review::class.java)
                    } catch (e: Exception) {
                        Log.e("ReviewManager", "Error parsing review: ", e)
                        null
                    }
                }
                callback.onReviewsLoaded(reviews)
            }
            .addOnFailureListener { exception ->
                Log.e("ReviewManager", "Error loading course reviews: ", exception)
                callback.onError("Failed to load reviews")
            }
    }

    /**
     * Get teacher's rating statistics
     */
    fun getTeacherRatingStats(teacherId: String, callback: (studentCount: Int, averageRating: Double, totalRating: Int) -> Unit) {
        db.collection("users")
            .document(teacherId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val studentCount = document.getLong("studentCount")?.toInt() ?: 0
                    val averageRating = document.getDouble("averageRating") ?: 0.0
                    val totalRating = document.getLong("totalRating")?.toInt() ?: 0

                    callback(studentCount, averageRating, totalRating)
                } else {
                    callback(0, 0.0, 0)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ReviewManager", "Error getting teacher stats: ", exception)
                callback(0, 0.0, 0)
            }
    }

    /**
     * Check if a student has already reviewed a specific course
     */
    fun hasStudentReviewedCourse(studentId: String, courseId: String, callback: (hasReviewed: Boolean) -> Unit) {
        db.collection("reviews")
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener { documents ->
                callback(!documents.isEmpty)
            }
            .addOnFailureListener { exception ->
                Log.e("ReviewManager", "Error checking review status: ", exception)
                callback(false) // Assume not reviewed on error
            }
    }

    /**
     * Get recent reviews (last 10)
     */
    fun getRecentReviews(callback: ReviewCallback) {
        db.collection("reviews")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val reviews = documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Review::class.java)
                    } catch (e: Exception) {
                        Log.e("ReviewManager", "Error parsing review: ", e)
                        null
                    }
                }
                callback.onReviewsLoaded(reviews)
            }
            .addOnFailureListener { exception ->
                Log.e("ReviewManager", "Error loading recent reviews: ", exception)
                callback.onError("Failed to load recent reviews")
            }
    }
}