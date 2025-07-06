package com.example.tampilansiswa.Ulasan

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.tampilansiswa.R
import com.google.firebase.firestore.FirebaseFirestore

class ulasan : Fragment() {

    private lateinit var layoutStars: LinearLayout
    private lateinit var etKomentar: EditText
    private lateinit var btnSimpan: Button
    private lateinit var txtNama: TextView
    private lateinit var txtMapel: TextView
    private lateinit var txtKampus: TextView
    private lateinit var imgGuru: ImageView

    private var rating = 0
    private lateinit var stars: Array<ImageView>
    private val db = FirebaseFirestore.getInstance()

    private var teacherId: String = ""
    private var courseId: String = ""
    private var enrollmentId: String = ""
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseId = it.getString("courseId") ?: ""
            teacherId = it.getString("teacherId") ?: ""
            enrollmentId = it.getString("enrollmentId") ?: ""
        }

        // Get current user ID
        val sharedPref = requireActivity().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
        currentUserId = sharedPref.getString("uid", "") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_ulasan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupStars()
        loadTeacherAndCourseData()
        checkIfAlreadyReviewed()

        view.findViewById<ImageView>(R.id.btn_backulasan).setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }
        btnSimpan.setOnClickListener {
            submitReview()
        }
    }

    private fun initializeViews(view: View) {
        layoutStars = view.findViewById(R.id.layoutStars)
        etKomentar = view.findViewById(R.id.etKomentar)
        btnSimpan = view.findViewById(R.id.btnSimpan)
        txtNama = view.findViewById(R.id.txtNama)
        txtMapel = view.findViewById(R.id.txtMapel)
        txtKampus = view.findViewById(R.id.txtKampus)
        imgGuru = view.findViewById(R.id.imgGuru)
    }

    private fun setupStars() {
        stars = Array(5) { ImageView(requireContext()) }
        layoutStars.removeAllViews()

        for (i in 0 until 5) {
            val star = ImageView(requireContext()).apply {
                setImageResource(R.drawable.ic_bintang_kosong) // Use empty star initially
                val size = (36 * resources.displayMetrics.density).toInt()
                val params = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(8, 0, 8, 0)
                }
                layoutParams = params
                setOnClickListener {
                    rating = i + 1
                    updateStars()
                }
            }
            stars[i] = star
            layoutStars.addView(star)
        }
    }

    private fun updateStars() {
        for (i in stars.indices) {
            stars[i].setImageResource(
                if (i < rating) R.drawable.ic_bintang else R.drawable.ic_bintang_kosong
            )
        }
    }

    private fun loadTeacherAndCourseData() {
        // Load teacher data
        db.collection("users")
            .document(teacherId)
            .get()
            .addOnSuccessListener { teacherDoc ->
                if (teacherDoc.exists()) {
                    val teacherName = teacherDoc.getString("nama") ?: "Unknown Teacher"
                    val education = teacherDoc.getString("education") ?: "Unknown Education"

                    txtNama.text = teacherName
                    txtKampus.text = education

                    // Load course data
                    loadCourseData()
                } else {
                    Log.e("Ulasan", "Teacher document not found")
                    txtNama.text = "Teacher Not Found"
                    txtKampus.text = "Unknown"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Ulasan", "Error loading teacher data", exception)
                txtNama.text = "Error Loading Teacher"
                txtKampus.text = "Unknown"
            }
    }

    private fun loadCourseData() {
        db.collection("courses")
            .document(courseId)
            .get()
            .addOnSuccessListener { courseDoc ->
                if (courseDoc.exists()) {
                    val courseName = courseDoc.getString("courseName") ?: "Unknown Course"
                    val subject = courseDoc.getString("subject") ?: "Unknown Subject"

                    txtMapel.text = "$subject - $courseName"
                } else {
                    Log.e("Ulasan", "Course document not found")
                    txtMapel.text = "Unknown Course"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Ulasan", "Error loading course data", exception)
                txtMapel.text = "Error Loading Course"
            }
    }

    private fun checkIfAlreadyReviewed() {
        db.collection("reviews")
            .whereEqualTo("studentId", currentUserId)
            .whereEqualTo("teacherId", teacherId)
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // User has already reviewed this course
                    disableReviewForm()
                    Toast.makeText(requireContext(), "Anda sudah memberikan ulasan untuk kursus ini", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Ulasan", "Error checking existing reviews", exception)
            }
    }

    private fun disableReviewForm() {
        // Disable all interactive elements
        for (star in stars) {
            star.isClickable = false
            star.alpha = 0.5f
        }
        etKomentar.isEnabled = false
        etKomentar.alpha = 0.5f
        btnSimpan.isEnabled = false
        btnSimpan.alpha = 0.5f
        btnSimpan.text = "Sudah Direview"
    }

    private fun submitReview() {
        val komentar = etKomentar.text.toString().trim()

        if (rating == 0) {
            Toast.makeText(requireContext(), "Pilih rating terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (komentar.isEmpty()) {
            Toast.makeText(requireContext(), "Tulis komentar terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUserId.isEmpty()) {
            Toast.makeText(requireContext(), "Error: User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button to prevent multiple submissions
        btnSimpan.isEnabled = false
        btnSimpan.text = "Menyimpan..."

        // Create review document
        val reviewData = hashMapOf(
            "studentId" to currentUserId,
            "teacherId" to teacherId,
            "courseId" to courseId,
            "enrollmentId" to enrollmentId,
            "rating" to rating,
            "comment" to komentar,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        // Save review
        db.collection("reviews")
            .add(reviewData)
            .addOnSuccessListener { documentReference ->
                Log.d("Ulasan", "Review saved with ID: ${documentReference.id}")

                // Update teacher's rating statistics
                updateTeacherRating()
            }
            .addOnFailureListener { exception ->
                Log.e("Ulasan", "Error saving review", exception)
                Toast.makeText(requireContext(), "Gagal menyimpan ulasan", Toast.LENGTH_SHORT).show()

                // Re-enable button
                btnSimpan.isEnabled = true
                btnSimpan.text = "Simpan"
            }
    }

    private fun updateTeacherRating() {
        // Get current teacher statistics
        db.collection("users")
            .document(teacherId)
            .get()
            .addOnSuccessListener { teacherDoc ->
                if (teacherDoc.exists()) {
                    val currentStudentCount = teacherDoc.getLong("studentCount")?.toInt() ?: 0
                    val currentTotalRating = teacherDoc.getLong("totalRating")?.toInt() ?: 0

                    // Calculate new values
                    val newStudentCount = currentStudentCount + 1
                    val newTotalRating = currentTotalRating + rating
                    val newAverageRating = newTotalRating.toDouble() / newStudentCount

                    // Update teacher document
                    val updates = hashMapOf<String, Any>(
                        "studentCount" to newStudentCount,
                        "totalRating" to newTotalRating,
                        "averageRating" to newAverageRating
                    )

                    db.collection("users")
                        .document(teacherId)
                        .update(updates)
                        .addOnSuccessListener {
                            Log.d("Ulasan", "Teacher rating updated successfully")
                            Toast.makeText(requireContext(), "Ulasan berhasil dikirim", Toast.LENGTH_SHORT).show()

                            // Navigate back
                            requireActivity().supportFragmentManager.popBackStack()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Ulasan", "Error updating teacher rating", exception)
                            Toast.makeText(requireContext(), "Ulasan tersimpan, tapi gagal update rating guru", Toast.LENGTH_SHORT).show()

                            // Still navigate back as review was saved
                            requireActivity().supportFragmentManager.popBackStack()
                        }
                } else {
                    Log.e("Ulasan", "Teacher document not found for rating update")
                    Toast.makeText(requireContext(), "Ulasan tersimpan, tapi gagal update rating guru", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Ulasan", "Error fetching teacher data for rating update", exception)
                Toast.makeText(requireContext(), "Ulasan tersimpan, tapi gagal update rating guru", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
    }

    companion object {
        fun newInstance(courseId: String, teacherId: String, enrollmentId: String) = ulasan().apply {
            arguments = Bundle().apply {
                putString("courseId", courseId)
                putString("teacherId", teacherId)
                putString("enrollmentId", enrollmentId)
            }
        }
    }
}