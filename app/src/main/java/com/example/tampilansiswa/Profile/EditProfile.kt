package com.example.tampilansiswa.Profile
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
import java.io.File          // ⬅️ Tambahkan ini
import java.io.FileOutputStream
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
        binding.btnBack.setOnClickListener{
            parentFragmentManager.popBackStack()
        }
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
                .replace(R.id.frame_container, AkunFragment()) // ganti R.id.fragment_container dengan ID container fragment kamu
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

        val path = sharedPref.getString("profileImage", null)
        loadImageFromLocalPath(path)

        uid?.let {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        binding.etNama.setText(document.getString("nama"))
                        binding.etEmail.setText(document.getString("email"))
                        binding.etPhone.setText(document.getString("phone"))

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

    private fun loadImageFromLocalPath(path: String?) {
        if (!path.isNullOrEmpty()) {
            val file = File(path)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                binding.ivAvatar.setImageBitmap(null) // clear cache
                binding.ivAvatar.setImageBitmap(bitmap)
            } else {
                binding.ivAvatar.setImageResource(R.drawable.avatar1)
            }
        } else {
            binding.ivAvatar.setImageResource(R.drawable.avatar1)
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



        val sharedPref = requireContext().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
        val uid = sharedPref.getString("uid", "") ?: ""
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
                apply()
            }

            val imagePath = sharedPref.getString("profileImage", "")

            val userMap = hashMapOf(
                "nama" to nama,
                "gender" to gender,
                "email" to email,
                "phone" to phone,
                "updatedAt" to com.google.firebase.Timestamp.now(),
                "imagePath" to imagePath
            )


            db.collection("users").document(uid)
                .update(userMap as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, AkunFragment())
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

            val resized = resizeBitmap(bitmap, 400, 400)

            // Simpan ke internal storage
            val directory = File(requireContext().filesDir, "profile")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            // Hapus file lama jika ada
            val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val oldPath = sharedPref.getString("profileImage", null)
            oldPath?.let {
                val oldFile = File(it)
                if (oldFile.exists()) {
                    oldFile.delete()
                }
            }

            // Buat nama file baru yang unik
            val fileName = "profile_avatar_${System.currentTimeMillis()}.jpg"
            val imageFile = File(directory, fileName)
            val outputStream = FileOutputStream(imageFile)
            resized.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            // Simpan path ke SharedPreferences
            with(sharedPref.edit()) {
                putString("profileImage", imageFile.absolutePath)
                apply()
            }

        } catch (e: Exception) {
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