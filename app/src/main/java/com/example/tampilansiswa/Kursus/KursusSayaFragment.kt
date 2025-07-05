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
import android.net.Uri
import com.example.tampilansiswa.Dashboard.HomeFragment
import java.io.File

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

    private var coursesLoaded = 0
    private var totalCourses = 0
    private val loadingLock = Any()
    private var currentTabType = TabType.PENDING

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
        binding.btnBack.setOnClickListener{
            navigateToFragment(HomeFragment())
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
        binding.rvKursus.layoutManager = LinearLayoutManager(requireContext())
        adapter = KursusAdapter(pendingList, this)
        binding.rvKursus.adapter = adapter
    }

    private fun setupTabs() {
        updateTabUI(TabType.PENDING)

        binding.tabPending.setOnClickListener {
            currentTabType = TabType.PENDING
            showKursus(pendingList)
            updateTabUI(TabType.PENDING)
        }

        binding.tabUpcoming.setOnClickListener {
            currentTabType = TabType.UPCOMING
            showKursus(upcomingList)
            updateTabUI(TabType.UPCOMING)
        }

        binding.tabSelesai.setOnClickListener {
            currentTabType = TabType.SELESAI
            showKursus(selesaiList)
            updateTabUI(TabType.SELESAI)
        }

        binding.tabCancel.setOnClickListener {
            currentTabType = TabType.CANCEL
            showKursus(cancelList)
            updateTabUI(TabType.CANCEL)
        }
    }

    private fun loadEnrollments() {
        val sharedPref = requireActivity().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
        val currentUser = sharedPref.getString("uid", "") ?: ""
        if (currentUser.isEmpty()) {
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

    // FIXED: Updated to include paymentProofPath from enrollment
    private fun createKursusFromData(enrollment: enrollments, course: Course, teacherName: String): Kursus {
        // Log untuk debugging
        Log.d("KursusSaya", "Creating Kursus with paymentProofPath: ${enrollment.paymentProofPath}")
        Log.d("KursusSaya", "Course poster path: ${course.poster}")

        return Kursus(
            namaGuru = teacherName,
            waktu = course.startTime,
            tanggal = course.date,
            avatar = R.drawable.avatar1,
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
            teacherId = course.uid,
            paymentProofPath = enrollment.paymentProofPath ?: "",
            poster = course.poster ?: ""
        )
    }

    private fun categorizeKursus(kursus: Kursus, status: String) {
        when (status.lowercase()) {
            "pending" -> pendingList.add(kursus)
            "approved", "confirmed","upcoming" -> upcomingList.add(kursus)
            "completed", "selesai" -> selesaiList.add(kursus)
            "cancelled", "cancel" -> cancelList.add(kursus)
            else -> {
                pendingList.add(kursus)
            }
        }
    }


    private fun loadTeacherAndCreateKursus(enrollment: enrollments, course: Course) {
        if (course.uid.isEmpty()) {
            Log.w("KursusSaya", "Course ${course.courseName} has empty teacher uid")
            val kursus = createKursusFromData(enrollment, course, "Teacher Not Assigned")
            categorizeKursus(kursus, enrollment.status)
            handleCourseLoadComplete()
            return
        }

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
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        loadTeacherWithRetry(enrollment, course, teacherId, retryCount + 1)
                    }, 1000L * (retryCount + 1))
                } else {
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
                updateTabCounts()
                updateEmptyState()
            } catch (e: Exception) {
                Log.e("KursusSaya", "Error updating adapter: ", e)
            }
        }
    }

    private fun updateEmptyState() {
        if (!isAdded || _binding == null) return

        val currentList = when (currentTabType) {
            TabType.PENDING -> pendingList
            TabType.UPCOMING -> upcomingList
            TabType.SELESAI -> selesaiList
            TabType.CANCEL -> cancelList
        }

        val isEmpty = currentList.isEmpty()

        // Show/hide empty state
        binding.layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvKursus.visibility = if (isEmpty) View.GONE else View.VISIBLE

        // Update empty state text based on current tab
        if (isEmpty) {
            val emptyMessage = when (currentTabType) {
                TabType.PENDING -> "Tidak ada kursus yang menunggu konfirmasi"
                TabType.UPCOMING -> "Tidak ada kursus yang akan datang"
                TabType.SELESAI -> "Tidak ada kursus yang telah selesai"
                TabType.CANCEL -> "Tidak ada kursus yang dibatalkan"
            }
            binding.txtEmptyMessage.text = emptyMessage
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

        // Hide empty state when loading
        if (show) {
            binding.layoutEmptyState.visibility = View.GONE
        }

        // Add your loading indicator here
        // binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        // binding.rvKursus.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun refreshData() {
        showLoading(true)
        loadEnrollments()
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
            adapter = KursusAdapter(data, this)
            binding.rvKursus.adapter = adapter
            updateEmptyState()
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