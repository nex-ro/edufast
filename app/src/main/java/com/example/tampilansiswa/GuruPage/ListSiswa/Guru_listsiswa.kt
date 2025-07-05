package com.example.tampilansiswa.GuruPage.ListSiswa

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.Data.enrollments
import com.example.tampilansiswa.GuruPage.Dashboard_guru
import com.example.tampilansiswa.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Guru_listsiswa : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SiswaAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val enrollmentsList = mutableListOf<enrollments>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_guru_listsiswa, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        recyclerView = view.findViewById(R.id.recyclerViewSiswa)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SiswaAdapter(enrollmentsList)
        recyclerView.adapter = adapter
        view.findViewById<ImageView>(R.id.btn_back).setOnClickListener{
            navigateToFragment(Dashboard_guru())
        }

        // Load data
        loadEnrollments()

        return view
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
    private fun loadEnrollments() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User tidak terautentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        val teacherId = currentUser.uid

        // Query enrollments where teacherId matches current user and status is "upcoming" or "selesai"
        db.collection("enrollments")
            .whereEqualTo("teacherId", teacherId)
            .whereIn("status", listOf("upcoming", "selesai"))
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    enrollmentsList.clear()
                    for (document in snapshot.documents) {
                        val enrollment = document.toObject(enrollments::class.java)
                        enrollment?.let { enrollmentsList.add(it) }
                    }
                    adapter.notifyDataSetChanged()

                    // Show message if no data
                    if (enrollmentsList.isEmpty()) {
                        Toast.makeText(requireContext(), "Tidak ada data siswa", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    // Method to refresh data manually if needed
    fun refreshData() {
        loadEnrollments()
    }
}