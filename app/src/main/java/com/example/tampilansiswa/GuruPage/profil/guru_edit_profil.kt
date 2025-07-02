package com.example.tampilansiswa.GuruPage.profil
import android.app.Activity
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
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.FragmentEditProfileBinding
import com.example.tampilansiswa.databinding.FragmentGuruEditProfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException

class guru_edit_profil : Fragment() {

    private var _binding: FragmentGuruEditProfilBinding? = null
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
        _binding = FragmentGuruEditProfilBinding.inflate(inflater, container, false)

        setupSpinners()
        setupClickListeners()
        loadUserData(auth.currentUser?.uid)

        return binding.root
    }

    private fun setupSpinners() {
        // Setup Gender Spinner
        val genderAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_list,
            android.R.layout.simple_spinner_item
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = genderAdapter

    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_container, guru_profil()) // ganti R.id.fragment_container dengan ID container fragment kamu
                .addToBackStack(null)
                .commit()

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
        binding.etBio.setText(sharedPref.getString("bio", ""))

        val imageBase64 = sharedPref.getString("profileImage", null)
        loadImageFromBase64(imageBase64)

        uid?.let {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        binding.etNama.setText(document.getString("nama"))
                        binding.etEmail.setText(document.getString("email"))
                        binding.etPhone.setText(document.getString("phone"))
                        binding.etBio.setText(document.getString("bio"))
                        val education = document.getString("education")
                        binding.etEducation.setText(education)

                        // Set Gender Spinner
                        val gender = document.getString("gender")
                        val genderAdapter = binding.spinnerGender.adapter as ArrayAdapter<String>
                        val genderIndex = genderAdapter.getPosition(gender)
                        if (genderIndex >= 0) binding.spinnerGender.setSelection(genderIndex)


                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal memuat data profil", Toast.LENGTH_SHORT).show()
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
                binding.ivAvatar.setImageResource(R.drawable.avatar1)
            }
        }
    }

    private fun saveProfile() {
        val nama = binding.etNama.text.toString().trim()
        val gender = binding.spinnerGender.selectedItem.toString()
        val education = binding.etEducation.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()
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
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Format email tidak valid"
            return
        }
        if (phone.isEmpty()) {
            binding.etPhone.error = "Nomor HP tidak boleh kosong"
            return
        }
        if (phone.length < 10) {
            binding.etPhone.error = "Nomor HP minimal 10 digit"
            return
        }
        if (bio.isEmpty()) {
            binding.etBio.error = "Bio tidak boleh kosong"
            return
        }
        if (bio.length < 20) {
            binding.etBio.error = "Bio minimal 20 karakter"
            return
        }
        if (education.isEmpty()) {
            binding.etEducation.error = "Pendidikan tidak boleh kosong"
            return
        }


        val uid = auth.currentUser?.uid
        if (uid != null) {
            selectedImageUri?.let { uri ->
                saveImageToSharedPreferences(uri)
            }

            // Simpan ke SharedPreferences
            val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("nama", nama)
                putString("email", email)
                putString("phone", phone)
                putString("gender", gender)
                putString("education", education)
                putString("bio", bio)
                apply()
            }

            // Simpan ke Firestore
            val userMap = hashMapOf(
                "nama" to nama,
                "gender" to gender,
                "education" to education,
                "bio" to bio,
                "email" to email,
                "phone" to phone,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            db.collection("users").document(uid)
                .update(userMap as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, guru_profil())
                        .addToBackStack(null)
                        .commit()

                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Gagal memperbarui profil: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveImageToSharedPreferences(imageUri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val resizedBitmap = resizeBitmap(bitmap, 200, 200)
            val byteArrayOutputStream = java.io.ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val base64String = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)

            val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("profileImage", base64String)
                apply()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Gagal menyimpan gambar", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}