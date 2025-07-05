package com.example.tampilansiswa.GuruPage.profil

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tampilansiswa.GuruPage.Dashboard_guru
import com.example.tampilansiswa.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Password : Fragment() {

    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var ivToggleCurrentPassword: ImageView
    private lateinit var ivToggleNewPassword: ImageView
    private lateinit var ivToggleConfirmPassword: ImageView

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firebaseAuth: FirebaseAuth

    private var isCurrentPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_password, container, false)

        initViews(view)
        initFirebase()
        setupClickListeners()
        view.findViewById<ImageView>(R.id.btn_back).setOnClickListener{
            navigateToFragment(guru_profil())
        }

        return view
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
        etCurrentPassword = view.findViewById(R.id.et_current_password)
        etNewPassword = view.findViewById(R.id.et_new_password)
        etConfirmPassword = view.findViewById(R.id.et_confirm_password)
        btnChangePassword = view.findViewById(R.id.btn_change_password)
        ivToggleCurrentPassword = view.findViewById(R.id.iv_toggle_current_password)
        ivToggleNewPassword = view.findViewById(R.id.iv_toggle_new_password)
        ivToggleConfirmPassword = view.findViewById(R.id.iv_toggle_confirm_password)

        sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
    }

    private fun initFirebase() {
        firebaseAuth = Firebase.auth
    }

    private fun setupClickListeners() {
        btnChangePassword.setOnClickListener {
            changePassword()
        }

        ivToggleCurrentPassword.setOnClickListener {
            togglePasswordVisibility(etCurrentPassword, ivToggleCurrentPassword, isCurrentPasswordVisible) {
                isCurrentPasswordVisible = it
            }
        }

        ivToggleNewPassword.setOnClickListener {
            togglePasswordVisibility(etNewPassword, ivToggleNewPassword, isNewPasswordVisible) {
                isNewPasswordVisible = it
            }
        }

        ivToggleConfirmPassword.setOnClickListener {
            togglePasswordVisibility(etConfirmPassword, ivToggleConfirmPassword, isConfirmPasswordVisible) {
                isConfirmPasswordVisible = it
            }
        }
    }

    private fun togglePasswordVisibility(
        editText: EditText,
        imageView: ImageView,
        isVisible: Boolean,
        callback: (Boolean) -> Unit
    ) {
        if (isVisible) {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.ic_visibility_off)
            callback(false)
        } else {
            editText.transformationMethod = null
            imageView.setImageResource(R.drawable.ic_visibility)
            callback(true)
        }
        editText.setSelection(editText.text.length)
    }

    private fun changePassword() {
        val currentPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (!validateInput(currentPassword, newPassword, confirmPassword)) {
            return
        }

        val uid = sharedPreferences.getString("uid", null)
        val email = sharedPreferences.getString("email", null)

        if (uid == null || email == null) {
            Toast.makeText(requireContext(), "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        btnChangePassword.isEnabled = false
        btnChangePassword.text = "Mengubah Password..."

        // Re-authenticate user with current password
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        firebaseAuth.currentUser?.reauthenticate(credential)
            ?.addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Update password
                    firebaseAuth.currentUser?.updatePassword(newPassword)
                        ?.addOnCompleteListener { updateTask ->
                            btnChangePassword.isEnabled = true
                            btnChangePassword.text = "Ubah Password"

                            if (updateTask.isSuccessful) {
                                Toast.makeText(requireContext(), "Password berhasil diubah", Toast.LENGTH_SHORT).show()
                                clearFields()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Gagal mengubah password: ${updateTask.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    btnChangePassword.isEnabled = true
                    btnChangePassword.text = "Ubah Password"
                    Toast.makeText(
                        requireContext(),
                        "Password saat ini salah",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun validateInput(currentPassword: String, newPassword: String, confirmPassword: String): Boolean {
        if (currentPassword.isEmpty()) {
            etCurrentPassword.error = "Password saat ini harus diisi"
            etCurrentPassword.requestFocus()
            return false
        }

        if (newPassword.isEmpty()) {
            etNewPassword.error = "Password baru harus diisi"
            etNewPassword.requestFocus()
            return false
        }

        if (newPassword.length < 6) {
            etNewPassword.error = "Password minimal 6 karakter"
            etNewPassword.requestFocus()
            return false
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Konfirmasi password harus diisi"
            etConfirmPassword.requestFocus()
            return false
        }

        if (newPassword != confirmPassword) {
            etConfirmPassword.error = "Password tidak cocok"
            etConfirmPassword.requestFocus()
            return false
        }

        if (currentPassword == newPassword) {
            etNewPassword.error = "Password baru harus berbeda dengan password lama"
            etNewPassword.requestFocus()
            return false
        }

        return true
    }

    private fun clearFields() {
        etCurrentPassword.text.clear()
        etNewPassword.text.clear()
        etConfirmPassword.text.clear()
    }
}