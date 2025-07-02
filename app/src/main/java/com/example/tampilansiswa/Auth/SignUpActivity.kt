package com.example.tampilansiswa.Auth

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

    companion object {
        private const val TAG = "SignUpActivity"
    }

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

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()
            ) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedRole.isEmpty() || selectedRole == "Pilih Role") {
                Toast.makeText(this, "Silakan pilih role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(name, phone, email, password, selectedRole)
        }

        binding.txtSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            finish()
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
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
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
                        "profileImage" to createDefaultProfileImageBase64()
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
                                    saveUserToPreferences(email, name, phone, role, true, createdAt, user["profileImage"] as String)
                                    Toast.makeText(this, "Registrasi berhasil sebagai $role", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Gagal menyimpan data role: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal menyimpan ke Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
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

        editor.putLong("createdAt", createdAt)
        editor.putString("email", email)
        editor.putBoolean("isActive", isActive)
        editor.putString("nama", nama)
        editor.putString("phone", phone)
        editor.putString("role", role)
        editor.putString("uid", auth.currentUser?.uid ?: "")
        editor.putString("profileImage", profileImage ?: "")

        editor.apply()
        Log.d(TAG, "User data saved to preferences successfully")
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
            Log.e(TAG, "Error creating default profile image: ${e.message}")
            ""
        }
    }
}
