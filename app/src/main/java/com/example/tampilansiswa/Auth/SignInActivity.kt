package com.example.tampilansiswa.Auth

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.tampilansiswa.MainActivity
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import android.text.InputType
import java.io.File          // ⬅️ Tambahkan ini
import java.io.FileOutputStream
import android.content.SharedPreferences
import android.view.View

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val passwordEditText = binding.etPassword
        val togglePassword = binding.ivTogglePassword
        var isPasswordVisible = false

        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            passwordEditText.inputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            passwordEditText.setSelection(passwordEditText.text.length)
            togglePassword.setImageResource(
                if (isPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
            )
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showError("Email dan Password tidak boleh kosong")
                Toast.makeText(this, "Mohon lengkapi email dan password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showLoading(true)

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        hideError()
                        fetchUserDataAndSave()
                    } else {
                        showLoading(false)
                        showError("Login gagal: ${task.exception?.message}")
                        Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.txtSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showError(message: String) {
        binding.tvErrorMessage.apply {
            text = message
            visibility = android.view.View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun hideError() {
        binding.tvErrorMessage.visibility = android.view.View.GONE
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
                    val profileImage = document.getString("imagePath")
                    showLoading(false)
                    saveUserToPreferences(email, nama, phone, role, isActive, createdAt, profileImage)
                    Toast.makeText(this, "Berhasil masuk sebagai $nama", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    showLoading(false)
                    showError("Gagal mengambil data user: ${it.message}")
                    Toast.makeText(this, "Gagal mengambil data user", Toast.LENGTH_SHORT).show()
                    saveUserToPreferences(
                        email = currentUser.email ?: "",
                        nama = currentUser.displayName ?: "User",
                        phone = "",
                        role = "siswa",
                        isActive = true,
                        createdAt = System.currentTimeMillis(),
                        profileImage = null
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
        editor.putString("uid", auth.currentUser?.uid ?: "")
        editor.putLong("createdAt", createdAt)
        editor.putString("email", email)
        editor.putBoolean("isActive", isActive)
        editor.putString("nama", nama)
        editor.putString("phone", phone)
        editor.putString("role", role)
        handleProfileImage(editor, profileImage)
        editor.apply()
    }

    private fun handleProfileImage(editor: SharedPreferences.Editor, profileImage: String?) {
        try {
            if (!profileImage.isNullOrEmpty()) {
                editor.putString("profileImage", profileImage)
            } else {
                val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                val existingImage = sharedPref.getString("profileImage", null)

                if (existingImage.isNullOrEmpty()) {
                    val defaultImagePath = saveDefaultAvatarToInternalStorage()
                    editor.putString("profileImage", defaultImagePath)
                }
            }
        } catch (_: Exception) {
            val defaultImagePath = saveDefaultAvatarToInternalStorage()
            editor.putString("profileImage", defaultImagePath)
        }
    }

    private fun saveDefaultAvatarToInternalStorage(): String {
        return try {
            val drawable = ContextCompat.getDrawable(this, R.drawable.avatar1)
            val bitmap = (drawable as BitmapDrawable).bitmap
            val resized = Bitmap.createScaledBitmap(bitmap, 400, 400, true)

            val file = File(filesDir, "profile/default_profile.jpg")
            file.parentFile?.mkdirs() // pastikan direktori ada
            val outStream = file.outputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
            outStream.flush()
            outStream.close()

            file.absolutePath
        } catch (e: Exception) {
            ""
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
