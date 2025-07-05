package com.example.tampilansiswa.GuruPage.siswa

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tampilansiswa.Data.enrollments
import com.example.tampilansiswa.Data.users
import com.example.tampilansiswa.GuruPage.siswa.CourseAdapter
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.FragmentGuruSiswaBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Button
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import android.graphics.drawable.Drawable
import com.example.tampilansiswa.GuruPage.Dashboard_guru

class guru_siswa : Fragment() {
    private var currentTeacherId: String = ""
    private var _binding: FragmentGuruSiswaBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CourseAdapter
    private lateinit var db: FirebaseFirestore
    private var overlayView: View? = null


    private val allCourses = mutableListOf<enrollments>()
    private val userMap = mutableMapOf<String, users>()
    private var currentFilter = "pending"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuruSiswaBinding.inflate(inflater, container, false)

        db = FirebaseFirestore.getInstance()
        try {
            val sharedPref = requireActivity().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
            currentTeacherId = sharedPref.getString("uid", "") ?: ""

            if (currentTeacherId.isEmpty()) {
                Toast.makeText(context, "Session tidak ditemukan, silakan login kembali", Toast.LENGTH_LONG).show()
                return binding.root
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error mengambil session: ${e.message}", Toast.LENGTH_SHORT).show()
            return binding.root
        }
        initializeTeacherStudentCount()

        setupRecyclerView()
        setupTabListeners()
        loadData()
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        binding.btnBack.setOnClickListener{
            navigateToFragment(Dashboard_guru())
        }
        return binding.root
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
    private fun setupRecyclerView() {
        adapter = CourseAdapter(emptyList(), emptyMap()) { course, action ->
            handleButtonClick(course, action)
        }
        binding.rvKursus.layoutManager = LinearLayoutManager(requireContext())
        binding.rvKursus.adapter = adapter
    }

    private fun setupTabListeners() {
        binding.tabPending.setOnClickListener {
            currentFilter = "pending"
            filterAndShowCourses()
            updateTabUI("pending")
        }

        binding.tabUpcoming.setOnClickListener {
            currentFilter = "upcoming"
            filterAndShowCourses()
            updateTabUI("upcoming")
        }

        binding.tabSelesai.setOnClickListener {
            currentFilter = "selesai"
            filterAndShowCourses()
            updateTabUI("selesai")
        }

        binding.tabCancel.setOnClickListener {
            currentFilter = "cancel"
            filterAndShowCourses()
            updateTabUI("cancel")
        }

        // Set default tab
        updateTabUI("pending")
    }

    private fun loadData() {
        binding.shimmerLayout.startShimmer() // <-- ini penting
        binding.shimmerLayout.visibility = View.VISIBLE
        binding.rvKursus.visibility = View.GONE

        loadCourses()
    }
    private fun initializeTeacherStudentCount() {
        if (currentTeacherId.isEmpty()) return

        db.collection("users").document(currentTeacherId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val siswaField = document.get("siswa")
                    if (siswaField == null) {
                        // Field siswa belum ada, inisialisasi dengan 0
                        db.collection("users").document(currentTeacherId)
                            .update("siswa", 0)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Field siswa berhasil diinisialisasi", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error inisialisasi field siswa: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error checking teacher document: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadCourses() {
        // Pastikan teacherId tersedia
        if (currentTeacherId.isEmpty()) {
            Toast.makeText(context, "Teacher ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            db.collection("enrollments")
                .whereEqualTo("teacherId", currentTeacherId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Toast.makeText(context, "Error loading courses: ${e.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        Toast.makeText(context, "Ditemukan ${snapshot.documents.size} dokumen", Toast.LENGTH_SHORT).show()

                        if (!snapshot.isEmpty) {
                            allCourses.clear()
                            for (doc in snapshot.documents) {
                                try {
                                    // Manual parsing untuk menangani timestamp
                                    val data = doc.data
                                    if (data != null) {
                                        val createdAtTimestamp = data["createdAt"] as? com.google.firebase.Timestamp
                                        val updatedAtTimestamp = data["updatedAt"] as? com.google.firebase.Timestamp

                                        val course = enrollments(
                                            id = doc.id,
                                            accountNumber = data["accountNumber"] as? String ?: "",
                                            amount = (data["amount"] as? Number)?.toLong() ?: 0L,
                                            bankName = data["bankName"] as? String ?: "",
                                            courseId = data["courseId"] as? String ?: "",
                                            courseName = data["courseName"] as? String ?: "",
                                            createdAt = createdAtTimestamp?.toDate()?.time ?: 0L,
                                            paymentProofPath = data["paymentProofPath"] as? String ?: "",
                                            status = data["status"] as? String ?: "",
                                            studentId = data["studentId"] as? String ?: "",
                                            teacherId = data["teacherId"] as? String ?: "",
                                            updatedAt = updatedAtTimestamp?.toDate()?.time ?: 0L
                                        )

                                        allCourses.add(course)
                                    }
                                } catch (ex: Exception) {
                                    Toast.makeText(context, "Error parsing doc: ${ex.message}", Toast.LENGTH_SHORT).show()
                                    continue
                                }
                            }


                            Toast.makeText(context, "Total courses loaded: ${allCourses.size}", Toast.LENGTH_SHORT).show()

                            // Load users for these courses
                            loadUsers()
                        } else {
                            Toast.makeText(context, "Tidak ada data enrollment untuk teacher ini", Toast.LENGTH_SHORT).show()
                            allCourses.clear()
                            loadUsers()
                        }
                    } else {
                        Toast.makeText(context, "Snapshot null", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Toast.makeText(context, "Error setting up listener: ${e.message}", Toast.LENGTH_SHORT).show()
        }

    }

    private fun loadUsers() {
        if (allCourses.isEmpty()) {
            stopShimmerAndShowData()
            filterAndShowCourses()
            return
        }

        val studentIds = allCourses.map { it.studentId }.distinct()

        db.collection("users")
            .whereIn("uid", studentIds)
            .get()
            .addOnSuccessListener { snapshot ->
                userMap.clear()
                for (doc in snapshot.documents) {
                    try {
                        // Manual parsing untuk memastikan semua field termuat
                        val data = doc.data
                        if (data != null) {
                            val user = users(
                                uid = data["uid"] as? String ?: "",
                                nama = data["nama"] as? String ?: "",
                                email = data["email"] as? String ?: "",
                                phone = data["phone"] as? String ?: "",
                                role = data["role"] as? String ?: "",
                                profilePath = data["profileImageUrl"] as? String ?: "",
                                imagePath = data["imagePath"] as? String ?: "",
                                gender = data["gender"] as? String ?: "",
                                isActive = data["isActive"] as? Boolean ?: true,
                                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                                updatedAt = (data["updatedAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
                            )
                            userMap[user.uid] = user
                        }
                    } catch (e: Exception) {
                        // Fallback ke parsing otomatis
                        val user = doc.toObject(users::class.java)
                        user?.let { userMap[it.uid] = it }
                    }
                }
                stopShimmerAndShowData()
                filterAndShowCourses()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading users: ${e.message}", Toast.LENGTH_SHORT).show()
                stopShimmerAndShowData()
                filterAndShowCourses()
            }
    }
    private fun stopShimmerAndShowData() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        binding.rvKursus.visibility = View.VISIBLE
    }


    private fun filterAndShowCourses() {
        val filteredCourses = when (currentFilter) {
            "pending" -> allCourses.filter { it.status.lowercase() == "pending" }
            "upcoming" -> allCourses.filter { it.status.lowercase() == "upcoming" }
            "selesai" -> allCourses.filter { it.status.lowercase() == "selesai" || it.status.lowercase() == "completed" }
            "cancel" -> allCourses.filter { it.status.lowercase() == "cancel" || it.status.lowercase() == "cancelled" }
            else -> allCourses
        }

        adapter.updateData(filteredCourses, userMap)
    }


    private fun updateTabUI(selectedTab: String) {
        val blue = ContextCompat.getColor(requireContext(), R.color.blue)
        val gray = ContextCompat.getColor(requireContext(), R.color.gray)

        // Reset all tabs
        binding.tabPending.setTextColor(gray)
        binding.tabUpcoming.setTextColor(gray)
        binding.tabSelesai.setTextColor(gray)
        binding.tabCancel.setTextColor(gray)

        binding.linePending.setBackgroundColor(gray)
        binding.lineUpcoming.setBackgroundColor(gray)
        binding.lineSelesai.setBackgroundColor(gray)
        binding.lineCancel.setBackgroundColor(gray)

        // Set selected tab
        when (selectedTab) {
            "pending" -> {
                binding.tabPending.setTextColor(blue)
                binding.linePending.setBackgroundColor(blue)
            }
            "upcoming" -> {
                binding.tabUpcoming.setTextColor(blue)
                binding.lineUpcoming.setBackgroundColor(blue)
            }
            "selesai" -> {
                binding.tabSelesai.setTextColor(blue)
                binding.lineSelesai.setBackgroundColor(blue)
            }
            "cancel" -> {
                binding.tabCancel.setTextColor(blue)
                binding.lineCancel.setBackgroundColor(blue)
            }
        }
    }

    private fun handleButtonClick(course: enrollments, action: String) {
        when (action) {
            "view_proof" -> {
                showPaymentProofOverlay(course.paymentProofPath)
            }
            "accept" -> {
                // Update status ke "upcoming"
                updateCourseStatus(course, "upcoming")
            }
            "reject" -> {
                // Update status ke "cancel"
                updateCourseStatus(course, "cancel")
            }
            "reschedule" -> {
                Toast.makeText(context, "Mengubah jadwal", Toast.LENGTH_SHORT).show()
            }
            "complete" -> {
                // Update status ke "selesai"
                updateCourseStatus(course, "selesai")
            }
        }
    }
    private fun showPaymentProofOverlay(paymentProofPath: String) {
        if (paymentProofPath.isEmpty()) {
            Toast.makeText(context, "Bukti pembayaran tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val overlayLayout = LayoutInflater.from(requireContext()).inflate(R.layout.overlay_payment_proof, null)

        val imageView = overlayLayout.findViewById<ImageView>(R.id.imgPaymentProof)
        val btnClose = overlayLayout.findViewById<Button>(R.id.btnCloseOverlay)
        val progressBar = overlayLayout.findViewById<ProgressBar>(R.id.progressBarImage)

        // Show progress bar
        progressBar.visibility = View.VISIBLE

        Glide.with(requireContext())
            .load(paymentProofPath)
            .placeholder(R.drawable.ic_visibility)
            .error(R.drawable.ic_empty_courses)
            .into(imageView)

        imageView.postDelayed({
            progressBar.visibility = View.GONE
        }, 2000)

        btnClose.setOnClickListener {
            hidePaymentProofOverlay()
        }

        // Set click listener untuk overlay background (tutup ketika diklik di luar gambar)
        overlayLayout.setOnClickListener {
            hidePaymentProofOverlay()
        }

        // Jangan tutup overlay ketika gambar diklik
        imageView.setOnClickListener {
            // Do nothing, prevent overlay from closing
        }

        // Tambahkan overlay ke parent view
        val parentView = requireActivity().findViewById<ViewGroup>(android.R.id.content)
        parentView.addView(overlayLayout)

        overlayView = overlayLayout

        // Animasi fade in
        overlayLayout.alpha = 0f
        overlayLayout.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun hidePaymentProofOverlay() {
        overlayView?.let { overlay ->
            // Animasi fade out
            overlay.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {
                    // Hapus overlay dari parent view
                    val parentView = requireActivity().findViewById<ViewGroup>(android.R.id.content)
                    parentView.removeView(overlay)
                    overlayView = null
                }
                .start()
        }
    }
    private fun updateCourseStatus(course: enrollments, newStatus: String) {
        if (course.id.isEmpty()) {
            Toast.makeText(context, "Course ID tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val courseRef = db.collection("enrollments").document(course.id)

            courseRef.update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).addOnSuccessListener {
                Toast.makeText(context, "Status berhasil diupdate", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Error updating status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}