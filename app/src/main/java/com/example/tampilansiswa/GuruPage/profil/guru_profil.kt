package com.example.tampilansiswa.GuruPage.profil

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.tampilansiswa.Onboarding.OnboardingActivity
import com.example.tampilansiswa.Profile.EditProfile
import com.example.tampilansiswa.Profile.EditProfileActivity
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.FragmentGuruProfilBinding
import com.google.firebase.auth.FirebaseAuth

class guru_profil : Fragment() {

    private var _binding: FragmentGuruProfilBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "guru_profil"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGuruProfilBinding.inflate(inflater, container, false)

        setupClickListeners()
        loadUserFromPreferences()
        setupDarkModeSwitch()

        return binding.root
    }

    private fun setupClickListeners() {
        // Edit Profile di header
        binding.btnEdit.setOnClickListener {
            navigateToFragment(guru_edit_profil())
        }

        binding.itemEditProfile.setOnClickListener {
            navigateToFragment(EditProfile())
        }

        binding.imgProfile.setOnClickListener {
            navigateToFragment(EditProfile())
        }

        // Mengatur Password
        binding.itemChangePassword.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur mengatur password akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        // Mengatur Pembayaran
        binding.itemPaymentSettings.setOnClickListener {
            navigateToFragment(PaymentMethodFragment())
        }

        // Dark Mode Switch
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            saveDarkModePreference(isChecked)
            applyDarkMode(isChecked)
        }

        // Logout
        binding.itemLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun setupDarkModeSwitch() {
        val sharedPref = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("dark_mode", false)
        binding.switchDarkMode.isChecked = isDarkMode
    }

    private fun saveDarkModePreference(isDarkMode: Boolean) {
        val sharedPref = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean("dark_mode", isDarkMode).apply()
    }

    private fun applyDarkMode(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun performLogout() {
        try {
            // Sign out dari Firebase
            auth.signOut()

            // Clear shared preferences
            val userSession = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            userSession.edit().clear().apply()

            // Clear app settings jika diperlukan (opsional)
            // val appSettings = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
            // appSettings.edit().clear().apply()

            Log.d(TAG, "Guru logged out successfully")

            // Redirect ke OnboardingActivity
            val intent = Intent(requireContext(), OnboardingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // Finish current activity jika diperlukan
            activity?.finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}")
            Toast.makeText(requireContext(), "Terjadi kesalahan saat logout", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserFromPreferences()
    }

    private fun loadUserFromPreferences() {
        try {
            val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)

            // Load text data
            val nama = sharedPref.getString("nama", "Nama tidak tersedia")
            val email = sharedPref.getString("email", "Email tidak tersedia")
            val phone = sharedPref.getString("phone", "No. HP tidak tersedia")
            val role = sharedPref.getString("role", "Guru")

            // Set text data
            binding.txtNama.text = nama
            binding.txtEmail.text = email
            binding.txtPhone.text = phone

            // Load profile image
            loadProfileImage(sharedPref)

            Log.d(TAG, "Guru data loaded successfully - Name: $nama, Email: $email")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading guru data: ${e.message}")
            setDefaultValues()
        }
    }

    private fun loadProfileImage(sharedPref: android.content.SharedPreferences) {
        val base64Image = sharedPref.getString("profileImage", null)

        if (!base64Image.isNullOrEmpty()) {
            try {
                // Decode Base64 string ke byte array
                val byteArray = Base64.decode(base64Image, Base64.DEFAULT)

                // Convert byte array ke bitmap
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

                if (bitmap != null) {
                    // Set bitmap ke ImageView
                    binding.imgProfile.setImageBitmap(bitmap)
                    Log.d(TAG, "Profile image loaded successfully")
                } else {
                    Log.w(TAG, "Failed to decode bitmap from Base64")
                    setDefaultProfileImage()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error decoding profile image: ${e.message}")
                setDefaultProfileImage()
            }
        } else {
            Log.d(TAG, "No profile image found, using default")
            setDefaultProfileImage()
        }
    }

    private fun setDefaultProfileImage() {
        binding.imgProfile.setImageResource(R.drawable.avatar1)
    }

    private fun setDefaultValues() {
        binding.txtNama.text = "Nama tidak tersedia"
        binding.txtEmail.text = "Email tidak tersedia"
        binding.txtPhone.text = "No. HP tidak tersedia"
        setDefaultProfileImage()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun navigateToFragment(fragment: Fragment) {
        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}