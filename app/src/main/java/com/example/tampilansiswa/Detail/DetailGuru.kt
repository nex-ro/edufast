package com.example.tampilansiswa.Detail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tampilansiswa.Data.Course
import com.example.tampilansiswa.Guru.GuruFragment
import com.example.tampilansiswa.Pemesanan.PemesananActivity
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.FragmentDetailGuruBinding
import com.example.tampilansiswa.pelajaran.CourseDetailFragment
import com.example.tampilansiswa.pelajaran.CoursePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class DetailGuru : Fragment() {
    private var _binding: FragmentDetailGuruBinding? = null
    private val binding get() = _binding!!
    private lateinit var guruId: String
    private var coursePagingAdapter: CoursePagingAdapter? = null
    private lateinit var firestore: FirebaseFirestore
    private var reviewsListener: ListenerRegistration? = null
    private var coursesListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailGuruBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isAdded || activity == null) return

        firestore = FirebaseFirestore.getInstance()

        val nama = arguments?.getString("nama") ?: ""
        val mapel = arguments?.getString("mapel") ?: ""
        val universitas = arguments?.getString("universitas") ?: ""
        val gender = arguments?.getString("gender") ?: ""
        val phone = arguments?.getString("phone") ?: ""
        val email = arguments?.getString("email") ?: ""
        val tentang = arguments?.getString("tentang") ?: ""
        val siswa = arguments?.getInt("siswa") ?: 0
        val rating = arguments?.getDouble("rating") ?: 5.0
        val ulasanCount = arguments?.getInt("ulasanCount") ?: 0
        val gambar = arguments?.getInt("gambar") ?: R.drawable.avatar1

        guruId = arguments?.getString("guruId") ?: ""

        // Set data ke views with safety check
        if (_binding != null) {
            binding.txtNama.text = nama
            binding.txtUniversitas.text = universitas
            binding.txtGender.text = gender
            binding.txtPhone.text = phone
            binding.txtEmail.text = email
            binding.txtTentang.text = tentang
            binding.txtSiswa.text = siswa.toString()
            binding. txtRating.text = String.format("%.1f", rating)
            binding.txtUlasanCount.text = ulasanCount.toString()
            binding.imgGuru.setImageResource(gambar)
        }

        // Setup RecyclerView untuk courses dengan paging
        setupCourseRecyclerView()

        // Load reviews
        loadReviews()

        binding.btnBack.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }
        // Setup click listener with safety check
        binding.pengalaman.setOnClickListener {
            if (isAdded && activity != null && _binding != null) {
                navigateToFragment(pengalaman.newInstance(guruId))
            }
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        if (!isAdded || activity == null || _binding == null) return
        try {
            requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit()
        } catch (e: Exception) {
            Log.e("DetailGuru", "Error navigating to fragment: ${e.message}", e)
        }
    }

    private fun setupCourseRecyclerView() {
        if (!isAdded || activity == null || _binding == null) return

        try {
            // Setup RecyclerView
            binding.rvMataPelajaran.layoutManager = LinearLayoutManager(requireContext())

            // First check if there are any courses available
            checkCoursesAvailability()

            // Create query for courses by this teacher
            val query = firestore.collection("courses")
                .whereEqualTo("uid", guruId)
                .whereEqualTo("active", true)

            // Configure paging
            val config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 5,
                enablePlaceholders = false
            )

            val options = FirestorePagingOptions.Builder<Course>()
                .setLifecycleOwner(viewLifecycleOwner)
                .setQuery(query, config, Course::class.java)
                .build()

            // Initialize adapter with click listener
            coursePagingAdapter = CoursePagingAdapter(options) { course ->
                // Handle course item click with safety check
                if (isAdded && activity != null && _binding != null) {
                    navigateToCourseDetail(course)
                }
            }

            if (_binding != null) {
                binding.rvMataPelajaran.adapter = coursePagingAdapter
            }
        } catch (e: Exception) {
            Log.e("DetailGuru", "Error setting up course RecyclerView: ${e.message}", e)
        }
    }

    private fun checkCoursesAvailability() {
        if (!isAdded || activity == null || _binding == null) return

        // Cancel previous listener if exists
        coursesListener?.remove()

        // Query to check if courses exist
        coursesListener = firestore.collection("courses")
            .whereEqualTo("uid", guruId)
            .whereEqualTo("active", true)
            .addSnapshotListener { result, error ->
                if (!isAdded || activity == null || _binding == null) return@addSnapshotListener

                if (error != null) {
                    showCoursesErrorMessage(error.message)
                    return@addSnapshotListener
                }

                if (result != null && result.isEmpty) {
                    // Show empty state message
                    showEmptyCoursesMessage()
                } else {
                    // Hide empty state message if it exists
                    hideEmptyCoursesMessage()
                }
            }
    }

    private fun showEmptyCoursesMessage() {
        if (!isAdded || activity == null || _binding == null) return

        try {
            // Create and show empty state message
            val emptyText = TextView(requireContext()).apply {
                text = "Guru ini belum memiliki kursus yang tersedia"
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
                setPadding(16, 16, 16, 16)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                tag = "empty_courses_message"
            }

            // Add the message to RecyclerView parent or create a container
            val parent = binding.rvMataPelajaran.parent as? ViewGroup
            parent?.let {
                // Check if message already exists
                val existingMessage = it.findViewWithTag<TextView>("empty_courses_message")
                if (existingMessage == null) {
                    // Find the index of RecyclerView to add message after it
                    val index = it.indexOfChild(binding.rvMataPelajaran)
                    it.addView(emptyText, index + 1)
                }
            }

            // Hide RecyclerView
            binding.rvMataPelajaran.visibility = View.GONE
        } catch (e: Exception) {
            Log.e("DetailGuru", "Error showing empty courses message: ${e.message}", e)
        }
    }

    private fun hideEmptyCoursesMessage() {
        if (!isAdded || activity == null || _binding == null) return

        try {
            // Remove empty state message and show RecyclerView
            val parent = binding.rvMataPelajaran.parent as? ViewGroup
            parent?.let {
                val existingMessage = it.findViewWithTag<TextView>("empty_courses_message")
                existingMessage?.let { message ->
                    it.removeView(message)
                }
            }

            // Show RecyclerView
            binding.rvMataPelajaran.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e("DetailGuru", "Error hiding empty courses message: ${e.message}", e)
        }
    }

    private fun showCoursesErrorMessage(errorMessage: String?) {
        if (!isAdded || activity == null || _binding == null) return

        try {
            val errorText = TextView(requireContext()).apply {
                text = "Gagal memuat kursus: ${errorMessage ?: "Unknown error"}"
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                setPadding(16, 16, 16, 16)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                tag = "error_courses_message"
            }

            val parent = binding.rvMataPelajaran.parent as? ViewGroup
            parent?.let {
                // Check if message already exists
                val existingMessage = it.findViewWithTag<TextView>("error_courses_message")
                if (existingMessage == null) {
                    val index = it.indexOfChild(binding.rvMataPelajaran)
                    it.addView(errorText, index + 1)
                }
            }

            // Hide RecyclerView
            binding.rvMataPelajaran.visibility = View.GONE
        } catch (e: Exception) {
            Log.e("DetailGuru", "Error showing courses error message: ${e.message}", e)
        }
    }

    private fun navigateToCourseDetail(course: Course) {
        if (!isAdded || activity == null || _binding == null) return

        try {
            val detailFragment = CourseDetailFragment.newInstance(course)
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, detailFragment)
                .addToBackStack(null)
                .commit()
        } catch (e: Exception) {
            Log.e("DetailGuru", "Error navigating to course detail: ${e.message}", e)
        }
    }

    private fun loadReviews() {
        if (!isAdded || activity == null || _binding == null) return

        // Cancel previous listener if exists
        reviewsListener?.remove()

        // Clear existing reviews first
        binding.containerReviews.removeAllViews()

        reviewsListener = firestore.collection("reviews")
            .whereEqualTo("teacherId", guruId)
            .addSnapshotListener { result, error ->
                if (!isAdded || activity == null || _binding == null) return@addSnapshotListener

                if (error != null) {
                    // Handle error
                    val errorText = TextView(requireContext()).apply {
                        text = "Gagal memuat review: ${error.message}"
                        textSize = 14f
                        setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                        setPadding(16, 8, 16, 8)
                    }
                    binding.containerReviews.addView(errorText)
                    return@addSnapshotListener
                }

                if (result != null) {
                    // Clear existing reviews
                    binding.containerReviews.removeAllViews()

                    // Update jumlah ulasan dengan data yang sebenarnya
                    val actualReviewCount = result.size()
                    binding.txtUlasanCount.text = actualReviewCount.toString()

                    if (result.isEmpty) {
                        // Tampilkan pesan jika tidak ada review
                        val noReviewText = TextView(requireContext()).apply {
                            text = "Belum ada review untuk guru ini"
                            textSize = 14f
                            setTextColor(resources.getColor(android.R.color.darker_gray, null))
                            setPadding(16, 8, 16, 8)
                        }
                        binding.containerReviews.addView(noReviewText)
                    } else {
                        // Hitung rata-rata rating
                        var totalRating = 0.0
                        for (doc in result) {
                            val rating = doc.getLong("rating")?.toDouble() ?: 0.0
                            totalRating += rating
                        }
                        val averageRating = totalRating / result.size()
                        binding.txtRating.text = String.format("%.1f", averageRating)

                        // Tampilkan semua review
                        for (doc in result) {
                            if (!isAdded || activity == null || _binding == null) break

                            // Dapatkan data student berdasarkan studentId untuk nama reviewer
                            val studentId = doc.getString("studentId") ?: ""
                            val comment = doc.getString("comment") ?: ""
                            val rate = doc.getLong("rating")?.toInt() ?: 0

                            try {
                                val item = LayoutInflater.from(requireContext())
                                    .inflate(R.layout.item_review, binding.containerReviews, false)

                                // Set rating terlebih dahulu
                                item.findViewById<TextView>(R.id.tvStar).text = "★".repeat(rate)
                                item.findViewById<TextView>(R.id.tvComment).text = comment

                                // Ambil nama student berdasarkan studentId
                                if (studentId.isNotEmpty()) {
                                    firestore.collection("students")
                                        .document(studentId)
                                        .get()
                                        .addOnSuccessListener { studentDoc ->
                                            if (isAdded && activity != null && _binding != null) {
                                                val studentName = studentDoc.getString("name") ?: "Anonim"
                                                item.findViewById<TextView>(R.id.tvReviewer).text = studentName
                                            }
                                        }
                                        .addOnFailureListener {
                                            if (isAdded && activity != null && _binding != null) {
                                                item.findViewById<TextView>(R.id.tvReviewer).text = "Anonim"
                                            }
                                        }
                                } else {
                                    item.findViewById<TextView>(R.id.tvReviewer).text = "Anonim"
                                }

                                binding.containerReviews.addView(item)
                            } catch (e: Exception) {
                                Log.e("DetailGuru", "Error creating review item: ${e.message}", e)
                            }
                        }
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Cancel all listeners
        reviewsListener?.remove()
        coursesListener?.remove()

        // Clean up adapter
        coursePagingAdapter?.let { adapter ->
            try {
                adapter.stopListening()
            } catch (e: Exception) {
                Log.e("DetailGuru", "Error stopping adapter: ${e.message}", e)
            }
        }
        coursePagingAdapter = null

        _binding = null
    }

    override fun onStop() {
        super.onStop()
        // Stop paging adapter listening when fragment is not visible
        coursePagingAdapter?.stopListening()
    }

    override fun onStart() {
        super.onStart()
        // Resume paging adapter listening when fragment becomes visible
        coursePagingAdapter?.startListening()
    }

    companion object {
        fun newInstance(
            guruId: String,
            nama: String,
            mapel: String,
            universitas: String? = null,
            gender: String? = null,
            phone: String? = null,
            email: String? = null,
            tentang: String? = null,
            siswa: Int = 0,
            pengalaman: Int = 0,
            rating: Double = 5.0,
            ulasanCount: Int = 0,
            gambar: Int = R.drawable.avatar1
        ): DetailGuru {
            val fragment = DetailGuru()
            val args = Bundle().apply {
                putString("guruId", guruId)
                putString("nama", nama)
                putString("mapel", mapel)
                putString("universitas", universitas)
                putString("gender", gender)
                putString("phone", phone)
                putString("email", email)
                putString("tentang", tentang)
                putInt("siswa", siswa)
                putInt("pengalaman", pengalaman)
                putDouble("rating", rating)
                putInt("ulasanCount", ulasanCount)
                putInt("gambar", gambar)
            }
            fragment.arguments = args
            return fragment
        }
    }
}