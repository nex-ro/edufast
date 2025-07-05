package com.example.tampilansiswa.GuruPage.Ulasan

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.Data.Review
import com.example.tampilansiswa.GuruPage.Dashboard_guru
import com.example.tampilansiswa.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class guru_ulasan : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private val reviewsList = mutableListOf<Review>()
    private var teacherId: String = ""

    companion object {
        private const val TAG = "GuruUlasan"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            val view = inflater.inflate(R.layout.fragment_guru_ulasan, container, false)
            db = FirebaseFirestore.getInstance()
            getTeacherIdFromPreferences()
            initViews(view)
            loadReviews()
            view.findViewById<ImageView>(R.id.btn_back).setOnClickListener{
                navigateToFragment(Dashboard_guru())
            }

            view
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreateView: ${e.message}", e)
            Toast.makeText(requireContext(), "Error loading fragment: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        try {
            requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit()
        } catch (e: Exception) {
            Toast.makeText(context, "Error navigating to page", Toast.LENGTH_SHORT).show()
        }
    }
    private fun initViews(view: View) {
        try {
            recyclerView = view.findViewById(R.id.recyclerViewReviews)
            progressBar = view.findViewById(R.id.progressBar)
            tvEmptyState = view.findViewById(R.id.tvEmptyState)

            // Setup RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            adapter = ReviewAdapter(reviewsList)
            recyclerView.adapter = adapter

            Log.d(TAG, "Views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}", e)
            Toast.makeText(requireContext(), "Error initializing views", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTeacherIdFromPreferences() {
        try {
            val sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            teacherId = sharedPreferences.getString("uid", "") ?: ""

            Log.d(TAG, "Teacher ID from preferences: $teacherId")

            if (teacherId.isEmpty()) {
                Log.w(TAG, "Teacher ID is empty")
                Toast.makeText(requireContext(), "Teacher ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting teacher ID: ${e.message}", e)
            Toast.makeText(requireContext(), "Error getting teacher ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadReviews() {
        if (teacherId.isEmpty()) {
            Log.w(TAG, "Cannot load reviews - teacher ID is empty")
            showEmptyState()
            return
        }

        try {
            showLoading(true)
            Log.d(TAG, "Loading reviews for teacher: $teacherId")

            db.collection("reviews")
                .whereEqualTo("teacherId", teacherId)
                .addSnapshotListener { snapshot, e ->
                    try {
                        showLoading(false)

                        if (e != null) {
                            Log.e(TAG, "Error loading reviews: ${e.message}", e)
                            Toast.makeText(requireContext(), "Error loading reviews: ${e.message}", Toast.LENGTH_SHORT).show()
                            showEmptyState()
                            return@addSnapshotListener
                        }

                        if (snapshot != null && !snapshot.isEmpty) {
                            reviewsList.clear()
                            var validReviews = 0

                            for (document in snapshot.documents) {
                                try {
                                    val review = document.toObject(Review::class.java)
                                    review?.let {
                                        reviewsList.add(it)
                                        validReviews++
                                    }
                                } catch (docError: Exception) {
                                    Log.e(TAG, "Error parsing document ${document.id}: ${docError.message}", docError)
                                }
                            }

                            Log.d(TAG, "Loaded $validReviews reviews")

                            if (reviewsList.isEmpty()) {
                                showEmptyState()
                            } else {
                                showReviews()
                                adapter.notifyDataSetChanged()
                            }
                        } else {
                            Log.d(TAG, "No reviews found")
                            showEmptyState()
                        }
                    } catch (processError: Exception) {
                        Log.e(TAG, "Error processing reviews: ${processError.message}", processError)
                        showEmptyState()
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up reviews listener: ${e.message}", e)
            showLoading(false)
            showEmptyState()
        }
    }

    private fun showLoading(show: Boolean) {
        try {
            progressBar.visibility = if (show) View.VISIBLE else View.GONE
            recyclerView.visibility = if (show) View.GONE else View.VISIBLE
            tvEmptyState.visibility = View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Error in showLoading: ${e.message}", e)
        }
    }

    private fun showEmptyState() {
        try {
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.GONE

            val view = requireView()
            val emptyStateLayout = view.findViewById<View>(R.id.emptyStateLayout)
            emptyStateLayout?.visibility = View.VISIBLE

            // Update header with zero values
            updateHeaderStatistics()
        } catch (e: Exception) {
            Log.e(TAG, "Error in showEmptyState: ${e.message}", e)
        }
    }

    private fun showReviews() {
        try {
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE

            val view = requireView()
            val emptyStateLayout = view.findViewById<View>(R.id.emptyStateLayout)
            emptyStateLayout?.visibility = View.GONE

            // Update header statistics
            updateHeaderStatistics()
        } catch (e: Exception) {
            Log.e(TAG, "Error in showReviews: ${e.message}", e)
        }
    }

    private fun updateHeaderStatistics() {
        try {
            val view = requireView()
            val tvAverageRating = view.findViewById<TextView>(R.id.tvAverageRating)
            val tvAverageStars = view.findViewById<TextView>(R.id.tvAverageStars)
            val tvTotalReviews = view.findViewById<TextView>(R.id.tvTotalReviews)

            if (reviewsList.isNotEmpty()) {
                // Calculate average rating
                val totalRating = reviewsList.sumOf { it.rating }
                val averageRating = totalRating.toDouble() / reviewsList.size

                // Update UI
                tvAverageRating?.text = String.format("%.1f", averageRating)
                tvAverageStars?.text = getStarRating(averageRating.toInt())
                tvTotalReviews?.text = reviewsList.size.toString()

                Log.d(TAG, "Updated header: avg=$averageRating, total=${reviewsList.size}")
            } else {
                tvAverageRating?.text = "0.0"
                tvAverageStars?.text = "☆☆☆☆☆"
                tvTotalReviews?.text = "0"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating header statistics: ${e.message}", e)
        }
    }

    private fun getStarRating(rating: Int): String {
        val maxStars = 5
        val filledStar = "★"
        val emptyStar = "☆"

        val stars = StringBuilder()
        for (i in 1..maxStars) {
            if (i <= rating) {
                stars.append(filledStar)
            } else {
                stars.append(emptyStar)
            }
        }
        return stars.toString()
    }

    // Method to refresh data manually if needed
    fun refreshReviews() {
        loadReviews()
    }
}