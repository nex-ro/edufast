package com.example.tampilansiswa.Profile

import android.app.Activity
import com.google.firebase.firestore.SetOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class EditProfile : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var selectedImageUri: Uri? = null

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        setupSpinner()
        setupClickListeners()
        loadUserData(auth.currentUser?.uid)

        return binding.root
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_list,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.tvGantiFoto.setOnClickListener {
            openGallery()
        }

        binding.ivAvatar.setOnClickListener {
            openGallery()
        }

        binding.btnSimpan.setOnClickListener {
            saveProfile()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }

    private fun loadUserData(uid: String?) {
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        binding.etNama.setText(sharedPref.getString("nama", ""))
        binding.etEmail.setText(sharedPref.getString("email", ""))
        binding.etPhone.setText(sharedPref.getString("phone", ""))

        val imagePath = sharedPref.getString("profileImageUrl", null)
        if (!imagePath.isNullOrEmpty()) {
            Glide.with(this)
                .load(File(imagePath))
                .placeholder(R.drawable.avatar1)
                .into(binding.ivAvatar)
        }

        uid?.let {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        binding.etNama.setText(document.getString("nama"))
                        binding.etEmail.setText(document.getString("email"))
                        binding.etPhone.setText(document.getString("phone"))

                        val gender = document.getString("gender")
                        val adapter = binding.spinnerGender.adapter as ArrayAdapter<String>
                        val genderIndex = adapter.getPosition(gender)
                        if (genderIndex >= 0) binding.spinnerGender.setSelection(genderIndex)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal memuat data profil", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfile() {
        val nama = binding.etNama.text.toString().trim()
        val gender = binding.spinnerGender.selectedItem.toString()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (nama.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val uid = sharedPref.getString("uid", null) ?: return

        // Ambil path gambar yang sudah ada dari SharedPreferences
        var imagePath = sharedPref.getString("profileImageUrl", null)

        // Jika ada gambar baru yang dipilih, simpan gambar baru
        if (selectedImageUri != null) {
            imagePath = saveImageToInternalStorage(selectedImageUri!!)
        }

        val userMap = hashMapOf<String, Any>(
            "nama" to nama,
            "gender" to gender,
            "email" to email,
            "phone" to phone
        )

        // Selalu sertakan profileImageUrl jika ada
        if (!imagePath.isNullOrEmpty()) {
            userMap["profileImageUrl"] = imagePath
        }

        db.collection("users").document(uid)
            .set(userMap, SetOptions.merge())
            .addOnSuccessListener {
                // Update SharedPreferences
                with(sharedPref.edit()) {
                    putString("nama", nama)
                    putString("email", email)
                    putString("phone", phone)
                    putString("gender", gender)
                    if (!imagePath.isNullOrEmpty()) {
                        putString("profileImageUrl", imagePath)
                    }
                    apply()
                }

                Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val filename = "profile_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().filesDir, filename)

            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
