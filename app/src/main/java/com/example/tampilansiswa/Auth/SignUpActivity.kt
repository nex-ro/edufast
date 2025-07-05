package com.example.tampilansiswa.Auth

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.tampilansiswa.MainActivity
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedRole = ""
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRoleSpinner()

        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showError("Semua field harus diisi")
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedRole.isEmpty() || selectedRole == "Pilih Role") {
                showError("Silakan pilih role")
                Toast.makeText(this, "Silakan pilih role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                showError("Password tidak cocok")
                Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                showError("Password minimal 6 karakter")
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(name, phone, email, password, selectedRole)
        }

        binding.ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            binding.etPassword.inputType =
                if (isPasswordVisible)
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                else
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        binding.ivToggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            binding.etConfirmPassword.inputType =
                if (isConfirmPasswordVisible)
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                else
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
        }

        binding.txtSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        listOf(
            binding.etName,
            binding.etPhone,
            binding.etEmail,
            binding.etPassword,
            binding.etConfirmPassword
        ).forEach {
            it.setOnFocusChangeListener { _, _ ->
                binding.tvErrorMessage.visibility = View.GONE
            }
        }
    }

    private fun setupRoleSpinner() {
        val roles = arrayOf("Pilih Role", "Siswa", "Guru")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = adapter

        binding.spinnerRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedRole = roles[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedRole = ""
            }
        }
    }

    private fun registerUser(name: String, phone: String, email: String, password: String, role: String) {
        showLoading()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                hideLoading()
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    val createdAt = System.currentTimeMillis()
                    val user = hashMapOf(
                        "uid" to uid,
                        "nama" to name,
                        "phone" to phone,
                        "email" to email,
                        "role" to role.lowercase(),
                        "createdAt" to createdAt,
                        "isActive" to true,
                    )

                    val collection = when (role.lowercase()) {
                        "siswa" -> "students"
                        "guru" -> "teachers"
                        else -> "users"
                    }

                    db.collection("users").document(uid).set(user)
                        .addOnSuccessListener {
                            db.collection(collection).document(uid).set(user)
                                .addOnSuccessListener {
                                    saveUserToPreferences(email, name, phone, role, true, createdAt)
                                    Toast.makeText(this, "Registrasi berhasil sebagai $role", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener {
                                    showError("Gagal menyimpan data role")
                                    Toast.makeText(this, "Gagal menyimpan data role", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            showError("Gagal menyimpan ke Firestore")
                            Toast.makeText(this, "Gagal menyimpan ke Firestore", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    val errorMessage = when {
                        task.exception?.message?.contains("email address is already in use") == true ->
                            "Email sudah terdaftar"
                        task.exception?.message?.contains("weak password") == true ->
                            "Password terlalu lemah"
                        task.exception?.message?.contains("badly formatted") == true ->
                            "Format email tidak valid"
                        else -> "Gagal: ${task.exception?.message}"
                    }
                    showError(errorMessage)
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
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
    ) {
        val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.putLong("createdAt", createdAt)
        editor.putString("email", email)
        editor.putBoolean("isActive", isActive)
        editor.putString("nama", nama)
        editor.putString("phone", phone)
        editor.putString("role", role.lowercase())
        editor.putString("uid", auth.currentUser?.uid ?: "")
        editor.apply()
    }

    private fun createDefaultProfileImageBase64(): String {
        return try {
            val drawable = ContextCompat.getDrawable(this, R.drawable.avatar1)
            val bitmap = (drawable as BitmapDrawable).bitmap
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
            val byteArrayOutputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            ""
        }
    }

    private fun showError(message: String) {
        binding.tvErrorMessage.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    private fun showLoading() {
        binding.progressOverlay.visibility = View.VISIBLE
        binding.btnSignUp.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressOverlay.visibility = View.GONE
        binding.btnSignUp.isEnabled = true
    }
}
