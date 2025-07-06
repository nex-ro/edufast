package com.example.tampilansiswa.GuruPage.kursus

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import com.example.tampilansiswa.R
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.net.Uri
import java.io.File
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import com.example.tampilansiswa.GuruPage.Dashboard_guru
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
class guru_kursus : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CourseAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var fabAddCourse: FloatingActionButton
    private lateinit var tabLayout: TabLayout
    private var selectedImageUri: Uri? = null
    private var selectedImagePath: String? = null
    private var imagePreview: ImageView? = null
    val imagePath: String = ""

    private val db = FirebaseFirestore.getInstance()
    private val coursesCollection = db.collection("courses")

    private var currentTab = 0
    private var allCourses = listOf<Course>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_guru_kursus, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupTabs()
        loadCourses()

        fabAddCourse.setOnClickListener {
            showAddCourseDialog()
        }
        var btnback=view.findViewById<ImageView>(R.id.btn_back)
        btnback.setOnClickListener{
            navigateToFragment(Dashboard_guru())
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
        recyclerView = view.findViewById(R.id.recyclerViewCourses)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        fabAddCourse = view.findViewById(R.id.fabAddCourse)
        tabLayout = view.findViewById(R.id.tabLayout)
    }

    private fun setupRecyclerView() {
        adapter = CourseAdapter(
            onItemClick = { course -> showCourseDetails(course) },
            onEditClick = { course -> showEditCourseDialog(course) },
            onToggleStatusClick = { course -> toggleCourseStatus(course) },
            onDeleteClick = { course -> confirmDeleteCourse(course) }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@guru_kursus.adapter
        }
    }

    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                filterCourses()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadCourses() {
        showLoading(true)

        coursesCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                showLoading(false)

                if (error != null) {
                    showError("Gagal memuat data: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val ctx = context ?: return@addSnapshotListener
                    val sharedPref = ctx.getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
                    val uid = sharedPref.getString("uid", "") ?: ""

                    allCourses = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Course::class.java)?.copy(id = doc.id)
                    }.filter { it.uid == uid }

                    filterCourses()
                }
            }
    }

    private fun filterCourses() {
        val filteredCourses = when (currentTab) {
            0 -> allCourses.filter { it.active }      // Kursus Aktif
            1 -> allCourses.filter { !it.active }     // Kursus Non-Aktif
            else -> allCourses
        }

        adapter.updateCourses(filteredCourses)
        showEmptyState(filteredCourses.isEmpty())
        updateEmptyMessage()
    }

    private fun updateEmptyMessage() {
        val message = when (currentTab) {
            0 -> "Belum ada kursus aktif"
            1 -> "Belum ada kursus non-aktif"
            else -> "Belum ada kursus yang tersedia"
        }
        tvEmpty.text = message
    }

    private fun showAddCourseDialog() {
        showCourseDialog(null)
    }

    private fun showEditCourseDialog(course: Course) {
        showCourseDialog(course)
    }

    private fun showCourseDialog(courseToEdit: Course?) {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_add_course, null)

        val etCourseName = dialogView.findViewById<TextInputEditText>(R.id.etCourseName)
        val etSubject = dialogView.findViewById<TextInputEditText>(R.id.etSubject)
        val etDate = dialogView.findViewById<TextInputEditText>(R.id.etDate)
        val etStartTime = dialogView.findViewById<TextInputEditText>(R.id.etStartTime)
        val etDuration = dialogView.findViewById<TextInputEditText>(R.id.etDuration)
        val etCourseType = dialogView.findViewById<AutoCompleteTextView>(R.id.etCourseType)
        val etRegion = dialogView.findViewById<TextInputEditText>(R.id.etRegion)
        val etLocation = dialogView.findViewById<TextInputEditText>(R.id.etLocation)
        val etLevel = dialogView.findViewById<AutoCompleteTextView>(R.id.etLevel)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)
        val etPrice = dialogView.findViewById<TextInputEditText>(R.id.etPrice)

        // Setup dropdown adapters
        val courseTypes = arrayOf("Privat", "Ke Tempat")
        val courseTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, courseTypes)
        etCourseType.setAdapter(courseTypeAdapter)

        val levels = arrayOf("SD", "SMP", "SMA")
        val levelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, levels)
        etLevel.setAdapter(levelAdapter)

        imagePreview = dialogView.findViewById(R.id.imgPosterPreview)
        selectedImageUri = null
        selectedImagePath = null

        imagePreview?.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        courseToEdit?.let { course ->
            etCourseName.setText(course.courseName)
            etSubject.setText(course.subject)
            etDate.setText(course.date)
            etStartTime.setText(course.startTime)
            etDuration.setText(course.duration.toString())
            etCourseType.setText(course.courseType, false)
            etRegion.setText(course.region)
            etLocation.setText(course.location)
            etLevel.setText(course.level, false)
            etDescription.setText(course.description)
            etPrice.setText(course.price.toString())

            // Tambahkan blok ini
            course.poster?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    imagePreview?.setImageURI(Uri.fromFile(file))
                    selectedImagePath = path
                }
            }
        }

        // Setup click listeners
        etDate.setOnClickListener {
            showDatePicker { selectedDate ->
                etDate.setText(selectedDate)
            }
        }

        etStartTime.setOnClickListener {
            showTimePicker { selectedTime ->
                etStartTime.setText(selectedTime)
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val titleView = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        titleView?.text = if (courseToEdit == null) "Tambah Kursus Baru" else "Edit Kursus"

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveCourse(
                courseToEdit,
                etCourseName.text.toString().trim(),
                etSubject.text.toString().trim(),
                etDate.text.toString().trim(),
                etStartTime.text.toString().trim(),
                etDuration.text.toString().trim(),
                etCourseType.text.toString().trim(),
                etRegion.text.toString().trim(),
                etLocation.text.toString().trim(),
                etLevel.text.toString().trim(),
                etDescription.text.toString().trim(),
                etPrice.text.toString().trim(),
                dialog
            )
        }

        dialog.show()
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            imagePreview?.setImageURI(uri)

            // Simpan gambar ke internal storage
            selectedImagePath = saveImageToInternalStorage(uri)

            if (selectedImagePath == null) {
                showError("Gagal menyimpan gambar")
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val fileName = "poster_${UUID.randomUUID()}.jpg"

            // Buat direktori course_posters jika belum ada
            val posterDir = File(requireContext().filesDir, "course_posters")
            if (!posterDir.exists()) {
                posterDir.mkdirs()
            }

            val file = File(posterDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                onTimeSelected(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun saveCourse(
        courseToEdit: Course?,
        courseName: String,
        subject: String,
        date: String,
        startTime: String,
        duration: String,
        courseType: String,
        region: String,
        location: String,
        level: String,
        description: String,
        price: String,
        dialog: AlertDialog
    ) {
        if (courseName.isEmpty() || subject.isEmpty() || date.isEmpty() ||
            startTime.isEmpty() || duration.isEmpty() || courseType.isEmpty() ||
            region.isEmpty() || location.isEmpty() || level.isEmpty() ||
            description.isEmpty() || price.isEmpty()) {
            showError("Semua field harus diisi")
            return
        }

        val durationInt = duration.toIntOrNull()
        val priceInt = price.toLongOrNull()
        val sharedPref = requireContext().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
        val uid = sharedPref.getString("uid", "") ?: ""

        if (durationInt == null || durationInt <= 0) {
            showError("Durasi harus berupa angka yang valid")
            return
        }
        if (uid.isEmpty()) {
            showError("Gagal menyimpan kursus: UID pengguna tidak ditemukan.")

            return
        }

        if (priceInt == null || priceInt <= 0) {
            showError("Harga harus berupa angka yang valid")
            return
        }

        // Tentukan path poster yang akan disimpan
        val posterPath = when {
            // Jika ada gambar baru yang dipilih
            selectedImagePath != null -> selectedImagePath
            // Jika sedang edit dan tidak ada gambar baru, gunakan gambar lama
            courseToEdit != null -> courseToEdit.poster
            // Jika tidak ada gambar sama sekali
            else -> ""
        }

        val course = Course(
            id = courseToEdit?.id ?: "",
            courseName = courseName,
            subject = subject,
            date = date,
            startTime = startTime,
            duration = durationInt,
            courseType = courseType,
            region = region,
            location = location,
            level = level,
            description = description,
            price = priceInt,
            active = courseToEdit?.active ?: true,
            uid = uid,
            createdAt = courseToEdit?.createdAt ?: System.currentTimeMillis(),
            poster = posterPath.orEmpty()
        )

        showLoading(true)

        if (courseToEdit != null) {
            // Update course yang sudah ada
            coursesCollection.document(courseToEdit.id)
                .set(course)
                .addOnSuccessListener {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Kursus berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    showError("Gagal memperbarui kursus: ${e.message}")
                }
        } else {
            // Tambah course baru
            coursesCollection.add(course.copy(id = ""))
                .addOnSuccessListener { documentReference ->
                    val courseWithId = course.copy(id = documentReference.id)
                    coursesCollection.document(documentReference.id).set(courseWithId)
                        .addOnSuccessListener {
                            showLoading(false)
                            Toast.makeText(requireContext(), "Kursus berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .addOnFailureListener { e ->
                            showLoading(false)
                            showError("Gagal menyimpan ID kursus: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    showError("Gagal menambahkan kursus: ${e.message}")
                }
        }
    }

    private fun loadImageFromInternalStorage(imagePath: String, imageView: ImageView) {
        if (imagePath.isNotEmpty()) {
            val file = File(imagePath)
            if (file.exists()) {
                imageView.setImageURI(Uri.fromFile(file))
            } else {
                imageView.setImageResource(R.drawable.ic_img) // Ganti dengan drawable default Anda
            }
        }
    }

    private fun deleteOldImage(imagePath: String?) {
        if (!imagePath.isNullOrEmpty()) {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    private fun toggleCourseStatus(course: Course) {
        showLoading(true)
        val updatedCourse = course.copy(active = !course.active)
        coursesCollection.document(course.id)
            .set(updatedCourse)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(requireContext(), "Status kursus diperbarui", Toast.LENGTH_SHORT).show()
                loadCourses()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                showError("Gagal memperbarui status: ${e.message}")
            }
    }

    private fun confirmDeleteCourse(course: Course) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Kursus")
            .setMessage("Apakah Anda yakin ingin menghapus kursus ini?")
            .setPositiveButton("Ya") { _, _ ->
                deleteCourse(course)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteCourse(course: Course) {
        showLoading(true)
        coursesCollection.document(course.id)
            .delete()
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(requireContext(), "Kursus berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                showError("Gagal menghapus kursus: ${e.message}")
            }
    }

    private fun showCourseDetails(course: Course) {
        AlertDialog.Builder(requireContext())
            .setTitle(course.courseName)
            .setMessage("""
            Mata Pelajaran: ${course.subject}
            Tanggal: ${course.date}
            Jam Mulai: ${course.startTime}
            Durasi: ${course.duration} menit
            Jenis Kursus: ${course.courseType}
            Wilayah: ${course.region}
            Lokasi: ${course.location}
            Tingkatan: ${course.level}
            Deskripsi: ${course.description}
            Harga: Rp${course.price}
            Status: ${if (course.active) "Aktif" else "Non-Aktif"}
        """.trimIndent())
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(isEmpty: Boolean) {
        tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
