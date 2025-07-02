package com.example.tampilansiswa.Profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var selectedImageUri: Uri? = null
    private var currentImagePath: String? = null

    // Activity Result Launcher untuk memilih gambar dari galeri
    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.ivAvatar.setImageURI(uri)
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = auth.currentUser?.uid

        setupSpinner()
        setupClickListeners()
        loadUserData(uid)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_list,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = adapter
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Ganti foto
        binding.tvGantiFoto.setOnClickListener {
            showImagePickerDialog()
        }

        // Avatar click
        binding.ivAvatar.setOnClickListener {
            showImagePickerDialog()
        }

        // Simpan button
        binding.btnSimpan.setOnClickListener {
            saveProfile()
        }
    }

    private fun showImagePickerDialog() {
        openGallery()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }



    private fun loadUserData(uid: String?) {
        // Load dari SharedPreferences terlebih dahulu
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)

        // Load data dari SharedPreferences
        binding.etNama.setText(sharedPref.getString("nama", ""))
        binding.etEmail.setText(sharedPref.getString("email", ""))
        binding.etPhone.setText(sharedPref.getString("phone", ""))

        // Load image dari SharedPreferences
        val imageBase64 = sharedPref.getString("profileImage", null)
        loadImageFromBase64(imageBase64)

        uid?.let {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        binding.etNama.setText(document.getString("nama"))
                        binding.etEmail.setText(document.getString("email"))
                        binding.etPhone.setText(document.getString("phone"))

                        // Load gender
                        val gender = document.getString("gender")
                        val adapter = binding.spinnerGender.adapter as ArrayAdapter<String>
                        val genderIndex = adapter.getPosition(gender)
                        if (genderIndex >= 0) binding.spinnerGender.setSelection(genderIndex)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal memuat data profil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadImageFromBase64(imageBase64: String?) {
        imageBase64?.let { base64String ->
            try {
                val decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                binding.ivAvatar.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                // Jika gagal load, gunakan default avatar
                binding.ivAvatar.setImageResource(R.drawable.avatar1)
            }
        }
    }

    private fun saveProfile() {
        val nama = binding.etNama.text.toString().trim()
        val gender = binding.spinnerGender.selectedItem.toString()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        // Validasi input
        if (nama.isEmpty()) {
            binding.etNama.error = "Nama tidak boleh kosong"
            return
        }
        if (email.isEmpty()) {
            binding.etEmail.error = "Email tidak boleh kosong"
            return
        }
        if (phone.isEmpty()) {
            binding.etPhone.error = "Nomor HP tidak boleh kosong"
            return
        }

        val uid = auth.currentUser?.uid
        if (uid != null) {
            // Simpan gambar ke SharedPreferences jika ada gambar baru
            selectedImageUri?.let { uri ->
                saveImageToSharedPreferences(uri)
            }

            // Simpan data ke SharedPreferences
            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("nama", nama)
            editor.putString("email", email)
            editor.putString("phone", phone)
            editor.putString("gender", gender)
            editor.apply()

            val userMap = hashMapOf(
                "nama" to nama,
                "gender" to gender,
                "email" to email,
                "phone" to phone
            )

            // Update data di Firestore
            db.collection("users").document(uid)
                .update(userMap as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveImageToSharedPreferences(imageUri: Uri) {
        try {
            // Baca bitmap dari URI
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Resize bitmap untuk menghemat memory
            val resizedBitmap = resizeBitmap(bitmap, 200, 200)

            // Convert bitmap ke Base64
            val byteArrayOutputStream = java.io.ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)

            // Simpan ke SharedPreferences
            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("profileImage", base64String)
            editor.apply()

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}