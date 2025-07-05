package com.example.tampilansiswa.GuruPage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tampilansiswa.GuruPage.ListSiswa.Guru_listsiswa
import com.example.tampilansiswa.GuruPage.Pengalaman.guru_pengalaman
import com.example.tampilansiswa.GuruPage.Ulasan.guru_ulasan
import com.example.tampilansiswa.GuruPage.kursus.guru_kursus
import com.example.tampilansiswa.GuruPage.siswa.guru_siswa
import com.example.tampilansiswa.databinding.FragmentDashboardGuruBinding
import com.example.tampilansiswa.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class Dashboard_guru : Fragment() {
    private var _binding: FragmentDashboardGuruBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var guruDataListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardGuruBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Load guru data
        loadGuruData()

        // Set click listeners
        binding.cardkursus.setOnClickListener {
            navigateToFragment(guru_kursus())
        }

        binding.cardjadwal.setOnClickListener {
            navigateToFragment(guru_siswa())
        }

        binding.siswabutton.setOnClickListener {
            navigateToFragment(Guru_listsiswa())
        }

        binding.pengalaman.setOnClickListener {
            navigateToFragment(guru_pengalaman())
        }

        binding.ulasan.setOnClickListener {
            navigateToFragment(guru_ulasan())
        }
    }

    private fun loadGuruData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Listen for real-time updates on guru data
        guruDataListener = db.collection("users")
            .document(currentUser.uid)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Log.e("Dashboard_guru", "Error listening to guru data: ${error.message}")
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    try {
                        // Update greeting with guru name
                        val nama = document.getString("nama") ?: "Guru"
                        binding.tvGreeting.text = "Hai, $nama!"

                        // Update statistics from document
                        val studentCount = document.getLong("studentCount")?.toInt() ?:
                        document.getLong("siswa")?.toInt() ?: 1
                        val averageRating = document.getDouble("averageRating") ?: 5.0

                        binding.tvSiswaAktif.text = studentCount.toString()
                        binding.tvRating.text = String.format("%.1f", averageRating)

                        // Load course count from courses collection
                        loadCourseCount(currentUser.uid)

                    } catch (e: Exception) {
                        Log.e("Dashboard_guru", "Error updating UI: ${e.message}")
                    }
                } else {
                    Log.e("Dashboard_guru", "Document not found")
                }
            }
    }

    private fun loadCourseCount(guruId: String) {
        db.collection("courses")
            .whereEqualTo("uid", guruId)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { documents, error ->
                if (error != null) {
                    Log.e("Dashboard_guru", "Error loading courses: ${error.message}")
                    return@addSnapshotListener
                }

                val courseCount = documents?.size() ?: 1
                binding.tvTotalKursus.text = courseCount.toString()
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
            Log.e("Dashboard_guru", "Error navigating to fragment: ${e.message}")
            Toast.makeText(context, "Error navigating to page", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove listener to prevent memory leaks
        guruDataListener?.remove()
        _binding = null
    }
}