package com.example.tampilansiswa.pelajaran

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.tampilansiswa.Data.Course
import com.example.tampilansiswa.R
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import android.content.Context
import com.google.firebase.Timestamp


class CourseDetailFragment : Fragment() {
    private var course: Course? = null
    private var paymentProofUri: Uri? = null
    private var paymentProofBitmap: Bitmap? = null
    private var paymentProofLocalPath: String? = null
    private var teacherImagePath: String? = null
    private var bankName: String = ""
    private var accountNumber: String = ""

    // Views
    private lateinit var ivCourseImage: ImageView
    private lateinit var tvCourseName: TextView
    private lateinit var tvSubject: TextView
    private lateinit var tvLevel: TextView
    private lateinit var tvCourseType: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvStartTime: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvBankName: TextView
    private lateinit var tvAccountNumber: TextView
    private lateinit var ivPaymentProof: ImageView
    private lateinit var btnSelectPaymentProof: Button
    private lateinit var btnFinishPayment: Button
    private var teacherName: String = ""

    private val db = FirebaseFirestore.getInstance()

    // Activity Result Launchers
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val ARG_COURSE = "course"

        fun newInstance(course: Course): CourseDetailFragment {
            val fragment = CourseDetailFragment()
            val args = Bundle()
            args.putSerializable(ARG_COURSE, course)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            course = it.getSerializable(ARG_COURSE) as? Course
        }

        // Initialize activity result launchers
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    paymentProofUri = uri
                    paymentProofBitmap = null
                    displayPaymentProof()
                    updateFinishButtonState()
                }
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                bitmap?.let {
                    paymentProofBitmap = it
                    paymentProofUri = null
                    displayPaymentProof()
                    updateFinishButtonState()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_course_detail, container, false)
        initializeViews(view)
        setupViews()
        loadPaymentMethodData()
        return view
    }

    private fun initializeViews(view: View) {
        ivCourseImage = view.findViewById(R.id.iv_course_detail_image)
        tvCourseName = view.findViewById(R.id.tv_course_detail_name)
        tvSubject = view.findViewById(R.id.tv_course_detail_subject)
        tvLevel = view.findViewById(R.id.tv_course_detail_level)
        tvCourseType = view.findViewById(R.id.tv_course_detail_type)
        tvDuration = view.findViewById(R.id.tv_course_detail_duration)
        tvLocation = view.findViewById(R.id.tv_course_detail_location)
        tvDate = view.findViewById(R.id.tv_course_detail_date)
        tvStartTime = view.findViewById(R.id.tv_course_detail_start_time)
        tvPrice = view.findViewById(R.id.tv_course_detail_price)
        tvDescription = view.findViewById(R.id.tv_course_detail_description)
        tvBankName = view.findViewById(R.id.tv_bank_name)
        tvAccountNumber = view.findViewById(R.id.tv_account_number)
        ivPaymentProof = view.findViewById(R.id.iv_payment_proof)
        btnSelectPaymentProof = view.findViewById(R.id.btn_select_payment_proof)
        btnFinishPayment = view.findViewById(R.id.btn_finish_payment)
    }

    private fun setupViews() {
        course?.let { courseData ->
            // Set course data to views
            tvCourseName.text = courseData.courseName
            tvSubject.text = "Mata pelajaran: ${courseData.subject}"
            tvLevel.text = courseData.level
            tvCourseType.text = "Tipe kursus: ${courseData.courseType}"
            tvDuration.text = "Durasi: ${courseData.formattedDuration}"
            tvLocation.text = "Lokasi: ${courseData.fullLocation}"
            tvDate.text = "Tanggal: ${courseData.date}"
            tvStartTime.text = "Waktu mulai: ${courseData.startTime}"
            tvPrice.text = courseData.formattedPrice
            tvDescription.text = courseData.description.ifEmpty {
                "Tidak ada deskripsi tersedia untuk kursus ini."
            }

            // Load course image with fallback priority
            loadCourseImage(courseData)

            // Set button click listeners
            btnSelectPaymentProof.setOnClickListener {
                showImagePickerDialog()
            }

            btnFinishPayment.setOnClickListener {
                handlePaymentSubmission(courseData)
            }

            // Initially disable finish button
            updateFinishButtonState()
        }
    }

    private fun loadCourseImage(courseData: Course) {
        when {
            courseData.poster.isNotEmpty() -> {
                // Priority 1: Use course poster
                Glide.with(this)
                    .load(courseData.poster)
                    .placeholder(R.drawable.avatar1)
                    .error(R.drawable.avatar1)
                    .into(ivCourseImage)
            }
            teacherImagePath?.isNotEmpty() == true -> {
                // Priority 2: Use teacher image
                Glide.with(this)
                    .load(teacherImagePath)
                    .placeholder(R.drawable.avatar1)
                    .error(R.drawable.avatar1)
                    .into(ivCourseImage)
            }
            else -> {
                // Priority 3: Use default avatar
                ivCourseImage.setImageResource(R.drawable.avatar1)
            }
        }
    }

    private fun loadPaymentMethodData() {
        course?.let { courseData ->
            // Get teacher's payment method from Firestore
            db.collection("payment_methods")
                .whereEqualTo("userId", courseData.uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val paymentMethod = documents.documents[0]
                        bankName = paymentMethod.getString("bank") ?: ""
                        accountNumber = paymentMethod.getString("nomorRekening") ?: ""

                        // Update UI
                        tvBankName.text = bankName
                        tvAccountNumber.text = accountNumber

                        loadTeacherData(courseData.uid)
                    } else {
                        tvBankName.text = "Bank tidak tersedia"
                        tvAccountNumber.text = "Nomor rekening tidak tersedia"
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Gagal memuat data pembayaran: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadTeacherData(teacherId: String) {
        db.collection("users")
            .document(teacherId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    teacherImagePath = document.getString("imagePath")
                    teacherName = document.getString("fullName") ?: document.getString("name") ?: "Guru"
                    // Reload course image with teacher image as fallback
                    course?.let { loadCourseImage(it) }
                }
            }
            .addOnFailureListener { exception ->
                // Handle error silently for teacher data
                teacherName = "Guru" // Default fallback
            }
    }

    private fun createNotification(course: Course, enrollmentId: String) {
        val notificationId = UUID.randomUUID().toString()
        val timestamp = Timestamp.now()

        // Data notifikasi untuk guru
        val notificationData = hashMapOf(
            "id" to notificationId,
            "uid" to getCurrentUserId(),
            "enrollId" to enrollmentId,
            "judulPesan" to "Kelas berhasil dibooking",
            "message" to "Pesanan terhadap kelas ${course.courseName} berhasil dibuat sedang menunggu penerimaan dari guru $teacherName",
            "createdAt" to timestamp,
            "isRead" to false,
            "type" to "enrollment"
        )

        // Simpan notifikasi ke Firestore
        db.collection("notifications")
            .document(notificationId)
            .set(notificationData)
            .addOnSuccessListener {
                // Notifikasi berhasil disimpan
                android.util.Log.d("Notification", "Notifikasi berhasil dibuat untuk guru: ${course.uid}")
            }
            .addOnFailureListener { exception ->
                // Log error jika gagal menyimpan notifikasi
                android.util.Log.e("Notification", "Gagal membuat notifikasi: ${exception.message}")
            }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Pilih dari Galeri", "Ambil Foto")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Pilih Bukti Pembayaran")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openGallery()
                1 -> openCamera()
            }
        }
        builder.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun displayPaymentProof() {
        when {
            paymentProofUri != null -> {
                Glide.with(this)
                    .load(paymentProofUri)
                    .into(ivPaymentProof)
                ivPaymentProof.visibility = View.VISIBLE
            }
            paymentProofBitmap != null -> {
                ivPaymentProof.setImageBitmap(paymentProofBitmap)
                ivPaymentProof.visibility = View.VISIBLE
            }
            else -> {
                ivPaymentProof.visibility = View.GONE
            }
        }
    }

    private fun updateFinishButtonState() {
        btnFinishPayment.isEnabled = paymentProofUri != null || paymentProofBitmap != null
        btnFinishPayment.alpha = if (btnFinishPayment.isEnabled) 1.0f else 0.5f
    }

    private fun handlePaymentSubmission(course: Course) {
        if (paymentProofUri == null && paymentProofBitmap == null) {
            Toast.makeText(context, "Silakan pilih bukti pembayaran terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi user ID
        val currentUserId = getCurrentUserId()
        if (currentUserId.isEmpty()) {
            Toast.makeText(context, "Sesi pengguna tidak valid. Silakan login kembali.", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        btnFinishPayment.isEnabled = false
        btnFinishPayment.text = "Menyimpan..."

        // Save payment proof to local storage
        savePaymentProofLocally { localPath ->
            if (localPath != null) {
                // Save enrollment and order data to Firestore
                saveEnrollmentAndOrderData(course, localPath)
            } else {
                Toast.makeText(context, "Gagal menyimpan bukti pembayaran", Toast.LENGTH_SHORT).show()
                btnFinishPayment.isEnabled = true
                btnFinishPayment.text = "Selesaikan"
            }
        }
    }

    private fun savePaymentProofLocally(callback: (String?) -> Unit) {
        try {
            // Create directory for payment proofs if it doesn't exist
            val paymentProofDir = File(requireContext().filesDir, "payment_proofs")
            if (!paymentProofDir.exists()) {
                paymentProofDir.mkdirs()
            }

            // Generate unique filename
            val filename = "payment_proof_${System.currentTimeMillis()}.jpg"
            val file = File(paymentProofDir, filename)

            when {
                paymentProofUri != null -> {
                    // Save from URI
                    val inputStream = requireContext().contentResolver.openInputStream(paymentProofUri!!)
                    val outputStream = FileOutputStream(file)
                    inputStream?.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    paymentProofLocalPath = file.absolutePath
                    callback(file.absolutePath)
                }
                paymentProofBitmap != null -> {
                    // Save from Bitmap
                    val outputStream = FileOutputStream(file)
                    outputStream.use { output ->
                        paymentProofBitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, output)
                    }
                    paymentProofLocalPath = file.absolutePath
                    callback(file.absolutePath)
                }
                else -> {
                    callback(null)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            callback(null)
        }
    }

    private fun createStudentNotification(course: Course, enrollmentId: String) {
        val currentUserId = getCurrentUserId()
        val notificationId = UUID.randomUUID().toString()
        val timestamp = Timestamp.now()

        // Data notifikasi untuk siswa
        val studentNotificationData = hashMapOf(
            "id" to notificationId,
            "uid" to currentUserId, // ID siswa yang akan menerima notifikasi
            "enrollId" to enrollmentId,
            "judulPesan" to "Pesanan berhasil dibuat",
            "message" to "Pesanan Anda untuk kelas ${course.courseName} berhasil dibuat. Menunggu konfirmasi dari guru $teacherName",
            "createdAt" to timestamp,
            "isRead" to false,
            "type" to "enrollment_confirmation"
        )

        // Simpan notifikasi siswa ke Firestore
        db.collection("notifications")
            .document(notificationId)
            .set(studentNotificationData)
            .addOnSuccessListener {
                android.util.Log.d("Notification", "Notifikasi siswa berhasil dibuat")
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("Notification", "Gagal membuat notifikasi siswa: ${exception.message}")
            }
    }
    private fun saveEnrollmentAndOrderData(course: Course, paymentProofPath: String) {
        val currentUserId = getCurrentUserId()
        val orderId = UUID.randomUUID().toString()
        val timestamp = com.google.firebase.Timestamp.now()

        // Enrollment data
        val enrollmentData = hashMapOf(
            "id" to orderId,
            "courseId" to course.id,
            "courseName" to course.courseName,
            "studentId" to currentUserId,
            "teacherId" to course.uid,
            "paymentProofPath" to paymentProofPath,
            "bankName" to bankName,
            "accountNumber" to accountNumber,
            "amount" to course.price,
            "status" to "pending",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        // Order data
        val orderData = hashMapOf(
            "id" to orderId,
            "courseId" to course.id,
            "courseName" to course.courseName,
            "subject" to course.subject,
            "level" to course.level,
            "courseType" to course.courseType,
            "duration" to course.duration,
            "location" to course.fullLocation,
            "date" to course.date,
            "startTime" to course.startTime,
            "price" to course.price,
            "description" to course.description,
            "studentId" to currentUserId,
            "teacherId" to course.uid,
            "paymentProofPath" to paymentProofPath,
            "bankName" to bankName,
            "accountNumber" to accountNumber,
            "paymentStatus" to "pending",
            "orderStatus" to "active",
            "createdAt" to timestamp,
            "updatedAt" to timestamp
        )

        // Save enrollment data
        db.collection("enrollments")
            .document(orderId)
            .set(enrollmentData)
            .addOnSuccessListener {
                // Save order data
                db.collection("orders")
                    .document(orderId)
                    .set(orderData)
                    .addOnSuccessListener {
                        // Buat notifikasi setelah order berhasil disimpan
                        createNotification(course, orderId)

                        Toast.makeText(context, "Pembayaran berhasil dikirim! Menunggu konfirmasi guru.", Toast.LENGTH_LONG).show()
                        // Navigate back or to success page
                        parentFragmentManager.popBackStack()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(context, "Gagal menyimpan data pesanan: ${exception.message}", Toast.LENGTH_SHORT).show()
                        // Delete enrollment if order creation fails
                        db.collection("enrollments").document(orderId).delete()
                        resetSubmissionState()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Gagal menyimpan data pembayaran: ${exception.message}", Toast.LENGTH_SHORT).show()
                resetSubmissionState()
            }
    }


    private fun resetSubmissionState() {
        btnFinishPayment.isEnabled = true
        btnFinishPayment.text = "Selesaikan"
    }

    private fun getCurrentUserId(): String {
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPref.getString("uid", "") ?: ""
    }

}