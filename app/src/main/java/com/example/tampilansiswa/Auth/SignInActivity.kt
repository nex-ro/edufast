package com.example.tampilansiswa.Auth

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.tampilansiswa.MainActivity
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    companion object {
        private const val TAG = "SignInActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            fetchUserDataAndSave()
                        } else {
                            Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        binding.txtForgotPassword.setOnClickListener {
            Toast.makeText(this, "Fitur belum tersedia", Toast.LENGTH_SHORT).show()
        }

        binding.txtSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun fetchUserDataAndSave() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val email = document.getString("email") ?: currentUser.email ?: ""
                    val nama = document.getString("nama") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val role = document.getString("role") ?: "siswa"
                    val isActive = document.getBoolean("isActive") ?: true
                    val createdAt = document.getLong("createdAt") ?: System.currentTimeMillis()
                    val profileImage = document.getString("profileImage") // Ambil gambar dari Firestore

                    saveUserToPreferences(email, nama, phone, role, isActive, createdAt, profileImage)

                    Log.d(TAG, "Login successful for user: $nama with role: $role")
                    Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to fetch user data: ${exception.message}")
                    Toast.makeText(this, "Gagal ambil data user: ${exception.message}", Toast.LENGTH_SHORT).show()
                    saveUserToPreferences(
                        email = currentUser.email ?: "",
                        nama = currentUser.displayName ?: "User",
                        phone = "",
                        role = "siswa",
                        isActive = true,
                        createdAt = System.currentTimeMillis(),
                        profileImage = null // Tidak ada gambar dari Firestore
                    )
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
        }
    }

    private fun saveUserToPreferences(
        email: String,
        nama: String,
        phone: String,
        role: String,
        isActive: Boolean,
        createdAt: Long,
        profileImage: String?
    ) {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Simpan data user biasa
        editor.putLong("createdAt", createdAt)
        editor.putString("email", email)
        editor.putBoolean("isActive", isActive)
        editor.putString("nama", nama)
        editor.putString("phone", phone)
        editor.putString("role", role)
        editor.putString("uid", auth.currentUser?.uid ?: "")

        // Handle profile image
        handleProfileImage(editor, profileImage)

        editor.apply()

        Log.d(TAG, "User data saved to preferences successfully")
    }

    private fun handleProfileImage(editor: android.content.SharedPreferences.Editor, profileImage: String?) {
        try {
            if (!profileImage.isNullOrEmpty()) {
                // Jika ada gambar dari Firestore, simpan ke preferences
                editor.putString("profileImage", profileImage)
                Log.d(TAG, "Profile image from Firestore saved to preferences")
            } else {
                // Jika tidak ada gambar dari Firestore, cek apakah sudah ada di preferences
                val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                val existingImage = sharedPref.getString("profileImage", null)

                if (existingImage.isNullOrEmpty()) {
                    // Jika tidak ada gambar sama sekali, buat default image
                    val defaultImageBase64 = createDefaultProfileImageBase64()
                    editor.putString("profileImage", defaultImageBase64)
                    Log.d(TAG, "Default profile image created and saved")
                } else {
                    Log.d(TAG, "Existing profile image retained")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling profile image: ${e.message}")
            // Jika terjadi error, tetap buat default image
            try {
                val defaultImageBase64 = createDefaultProfileImageBase64()
                editor.putString("profileImage", defaultImageBase64)
                Log.d(TAG, "Default profile image created after error")
            } catch (defaultError: Exception) {
                Log.e(TAG, "Error creating default profile image: ${defaultError.message}")
            }
        }
    }

    private fun createDefaultProfileImageBase64(): String {
        return try {
            // Ambil drawable default (avatar1)
            val drawable = ContextCompat.getDrawable(this, R.drawable.avatar1)
            val bitmap = (drawable as BitmapDrawable).bitmap

            // Resize bitmap jika terlalu besar (opsional, untuk menghemat space)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true)

            // Convert ke Base64
            val byteArrayOutputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating default profile image: ${e.message}")
            "" // Return empty string jika gagal
        }
    }
    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}