package com.example.tampilansiswa.Kursus

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tampilansiswa.Data.Course
import com.example.tampilansiswa.Data.enrollments
import com.example.tampilansiswa.Data.Kursus
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.FragmentKursusSayaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class KursusSayaFragment : Fragment() {

    private var _binding: FragmentKursusSayaBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: KursusAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val pendingList = mutableListOf<Kursus>()
    private val upcomingList = mutableListOf<Kursus>()
    private val selesaiList = mutableListOf<Kursus>()
    private val cancelList = mutableListOf<Kursus>()

    // Add synchronization for thread safety
    private var coursesLoaded = 0
    private var totalCourses = 0
    private val loadingLock = Any()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKursusSayaBinding.inflate(inflater, container, false)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        setupTabs()
        loadEnrollments()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.rvKursus.layoutManager = LinearLayoutManager(requireContext())
        adapter = KursusAdapter(pendingList)
        binding.rvKursus.adapter = adapter
    }

    private fun setupTabs() {
        updateTabUI(TabType.PENDING)

        binding.tabPending.setOnClickListener {
            showKursus(pendingList)
            updateTabUI(TabType.PENDING)
        }

        binding.tabUpcoming.setOnClickListener {
            showKursus(upcomingList)
            updateTabUI(TabType.UPCOMING)
        }

        binding.tabSelesai.setOnClickListener {
            showKursus(selesaiList)
            updateTabUI(TabType.SELESAI)
        }

        binding.tabCancel.setOnClickListener {
            showKursus(cancelList)
            updateTabUI(TabType.CANCEL)
        }
    }

    private fun loadEnrollments() {
        val sharedPref = requireActivity().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
        val currentUser = sharedPref.getString("uid", "") ?: ""
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading state
        showLoading(true)

        db.collection("enrollments")
            .whereEqualTo("studentId", currentUser)
            .get()
            .addOnSuccessListener { enrollmentDocs ->
                if (enrollmentDocs.isEmpty) {
                    showLoading(false)
                    updateAdapters()
                    return@addOnSuccessListener
                }

                val enrollments = enrollmentDocs.mapNotNull { doc ->
                    try {
                        val enrollment = doc.toObject(enrollments::class.java)
                        // Set the document ID if it's not already set
                        if (enrollment.id.isEmpty()) {
                            enrollment.copy(id = doc.id)
                        } else {
                            enrollment
                        }
                    } catch (e: Exception) {
                        Log.e("KursusSaya", "Error parsing enrollment: ", e)
                        null
                    }
                }

                loadCoursesForEnrollments(enrollments)
            }
            .addOnFailureListener { exception ->
                Log.e("KursusSaya", "Error getting enrollments: ", exception)
                showLoading(false)
                Toast.makeText(requireContext(), "Error loading enrollments", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadCoursesForEnrollments(enrollments: List<enrollments>) {
        // Clear existing data
        pendingList.clear()
        upcomingList.clear()
        selesaiList.clear()
        cancelList.clear()

        synchronized(loadingLock) {
            coursesLoaded = 0
            totalCourses = enrollments.size
        }

        if (totalCourses == 0) {
            showLoading(false)
            updateAdapters()
            return
        }

        enrollments.forEach { enrollment ->
            // Enhanced validation with logging
            if (enrollment.courseId.isEmpty()) {
                Log.w("KursusSaya", "Enrollment has empty courseId: ${enrollment.id}")
                handleCourseLoadComplete()
                return@forEach
            }

            Log.d("KursusSaya", "Loading course: ${enrollment.courseId}")

            db.collection("courses")
                .document(enrollment.courseId)
                .get()
                .addOnSuccessListener { courseDoc ->
                    try {
                        if (courseDoc.exists()) {
                            val course = courseDoc.toObject(Course::class.java)
                            if (course != null) {
                                // Set the document ID if it's not already set
                                val courseWithId = if (course.id.isEmpty()) {
                                    course.copy(id = courseDoc.id)
                                } else {
                                    course
                                }

                                Log.d("KursusSaya", "Course loaded: ${courseWithId.courseName}, Teacher ID: ${courseWithId.uid}")
                                loadTeacherAndCreateKursus(enrollment, courseWithId)
                            } else {
                                Log.w("KursusSaya", "Course document parse failed for ID: ${enrollment.courseId}")
                                handleCourseLoadComplete()
                            }
                        } else {
                            Log.w("KursusSaya", "Course document doesn't exist for ID: ${enrollment.courseId}")
                            handleCourseLoadComplete()
                        }
                    } catch (e: Exception) {
                        Log.e("KursusSaya", "Error processing course document: ", e)
                        handleCourseLoadComplete()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("KursusSaya", "Error getting course: ", exception)
                    handleCourseLoadComplete()
                }
        }
    }


    private fun createKursusFromData(enrollment: enrollments, course: Course, teacherName: String): Kursus {
        return Kursus(
            namaGuru = teacherName,
            waktu = course.startTime,
            tanggal = course.date,
            avatar = R.drawable.avatar1, // Default avatar
            status = enrollment.status,
            courseName = course.courseName,
            courseType = course.courseType,
            description = course.description,
            level = course.level,
            subject = course.subject,
            fullLocation = course.fullLocation,
            formattedPrice = course.formattedPrice,
            formattedDuration = course.formattedDuration,
            enrollmentId = enrollment.id,
            courseId = course.id,
            teacherId = course.uid
        )
    }

    private fun categorizeKursus(kursus: Kursus, status: String) {
        when (status.lowercase()) {
            "pending" -> pendingList.add(kursus)
            "approved", "confirmed" -> upcomingList.add(kursus)
            "completed", "selesai" -> selesaiList.add(kursus)
            "cancelled", "cancel" -> cancelList.add(kursus)
            else -> {
                Log.w("KursusSaya", "Unknown status: $status, defaulting to pending")
                pendingList.add(kursus) // Default to pending
            }
        }
    }
    private fun loadTeacherAndCreateKursus(enrollment: enrollments, course: Course) {
        // Enhanced validation and logging
        if (course.uid.isEmpty()) {
            Log.w("KursusSaya", "Course ${course.courseName} has empty teacher uid")
            val kursus = createKursusFromData(enrollment, course, "Teacher Not Assigned")
            categorizeKursus(kursus, enrollment.status)
            handleCourseLoadComplete()
            return
        }

        // Add retry mechanism for teacher loading
        loadTeacherWithRetry(enrollment, course, course.uid, 0)
    }
    private fun loadTeacherWithRetry(enrollment: enrollments, course: Course, teacherId: String, retryCount: Int) {
        val maxRetries = 2

        db.collection("users")
            .document(teacherId)
            .get()
            .addOnSuccessListener { teacherDoc ->
                val teacherName = when {
                    !teacherDoc.exists() -> {
                        Log.w("KursusSaya", "Teacher document doesn't exist for ID: $teacherId")
                        "Teacher Not Found"
                    }
                    else -> {
                        // Try multiple field names for teacher name
                        teacherDoc.getString("nama")
                            ?: run {
                                Log.w("KursusSaya", "Teacher document exists but has no name fields: $teacherId")
                                "Teacher Name Missing"
                            }
                    }
                }

                val kursus = createKursusFromData(enrollment, course, teacherName)
                categorizeKursus(kursus, enrollment.status)
                handleCourseLoadComplete()
            }
            .addOnFailureListener { exception ->
                Log.e("KursusSaya", "Error getting teacher (attempt ${retryCount + 1}): ", exception)

                if (retryCount < maxRetries) {
                    // Retry after a short delay
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        loadTeacherWithRetry(enrollment, course, teacherId, retryCount + 1)
                    }, 1000L * (retryCount + 1)) // Exponential backoff
                } else {
                    // All retries failed
                    val kursus = createKursusFromData(enrollment, course, "Teacher Load Failed")
                    categorizeKursus(kursus, enrollment.status)
                    handleCourseLoadComplete()
                }
            }
    }



    private fun handleCourseLoadComplete() {
        synchronized(loadingLock) {
            coursesLoaded++
            if (coursesLoaded >= totalCourses) {
                showLoading(false)
                updateAdapters()
            }
        }
    }

    private fun updateAdapters() {
        if (!isAdded || _binding == null) return

        activity?.runOnUiThread {
            try {
                adapter.notifyDataSetChanged()

                // Update tab counts (optional)
                updateTabCounts()
            } catch (e: Exception) {
                Log.e("KursusSaya", "Error updating adapter: ", e)
            }
        }
    }

    private fun updateTabCounts() {
        // Optional: Update tab text with counts
        // binding.tabPending.text = "Pending (${pendingList.size})"
        // binding.tabUpcoming.text = "Upcoming (${upcomingList.size})"
        // binding.tabSelesai.text = "Completed (${selesaiList.size})"
        // binding.tabCancel.text = "Cancelled (${cancelList.size})"
    }

    private fun showLoading(show: Boolean) {
        if (!isAdded || _binding == null) return

        // Add your loading indicator here
        // binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        // binding.rvKursus.visibility = if (show) View.GONE else View.VISIBLE
    }
    private fun refreshData() {
        showLoading(true)
        loadEnrollments()
    }

    // Optional: Add method to check data integrity
    private fun checkDataIntegrity() {
        val sharedPref = requireActivity().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
        val currentUser = sharedPref.getString("uid", "") ?: ""

        if (currentUser.isEmpty()) {
            Log.e("KursusSaya", "User session is empty")
            return
        }

        // Check if enrollments exist
        db.collection("enrollments")
            .whereEqualTo("studentId", currentUser)
            .get()
            .addOnSuccessListener { enrollmentDocs ->
                Log.d("KursusSaya", "Found ${enrollmentDocs.size()} enrollments for user: $currentUser")

                enrollmentDocs.forEach { doc ->
                    val enrollment = doc.toObject(enrollments::class.java)
                    Log.d("KursusSaya", "Enrollment: ${enrollment.id}, Course: ${enrollment.courseId}, Status: ${enrollment.status}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("KursusSaya", "Error checking enrollments: ", exception)
            }
    }

    private fun updateTabUI(tabType: TabType) {
        if (!isAdded || _binding == null) return

        try {
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

            // Set active tab
            when (tabType) {
                TabType.PENDING -> {
                    binding.tabPending.setTextColor(blue)
                    binding.linePending.setBackgroundColor(blue)
                }
                TabType.UPCOMING -> {
                    binding.tabUpcoming.setTextColor(blue)
                    binding.lineUpcoming.setBackgroundColor(blue)
                }
                TabType.SELESAI -> {
                    binding.tabSelesai.setTextColor(blue)
                    binding.lineSelesai.setBackgroundColor(blue)
                }
                TabType.CANCEL -> {
                    binding.tabCancel.setTextColor(blue)
                    binding.lineCancel.setBackgroundColor(blue)
                }
            }
        } catch (e: Exception) {
            Log.e("KursusSaya", "Error updating tab UI: ", e)
        }
    }

    private fun showKursus(data: MutableList<Kursus>) {
        if (!isAdded || _binding == null) return

        try {
            adapter = KursusAdapter(data)
            binding.rvKursus.adapter = adapter
        } catch (e: Exception) {
            Log.e("KursusSaya", "Error showing kursus: ", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class TabType {
        PENDING, UPCOMING, SELESAI, CANCEL
    }
}