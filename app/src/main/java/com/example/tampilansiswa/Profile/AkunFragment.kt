package com.example.tampilansiswa.Profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tampilansiswa.Onboarding.OnboardingActivity
import com.example.tampilansiswa.databinding.FragmentAkunBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.tampilansiswa.R

class AkunFragment : Fragment() {

    private var _binding: FragmentAkunBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "AkunFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAkunBinding.inflate(inflater, container, false)

        setupClickListeners()
        loadUserFromPreferences()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Refresh data setiap kali fragment terlihat (misalnya setelah edit profile)
        loadUserFromPreferences()
    }

    private fun setupClickListeners() {
        // Tombol edit profile
        binding.btnEdit.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Tombol Logout
        binding.itemLogout.setOnClickListener {
            performLogout()
        }

        // Profile image click - bisa untuk quick edit atau view
        binding.imgProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogout() {
        try {
            // Sign out dari Firebase
            auth.signOut()

            // Clear SharedPreferences
            val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.clear()
            editor.apply()

            Log.d(TAG, "User logged out successfully")

            // Navigate ke OnboardingActivity
            val intent = Intent(requireContext(), OnboardingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // Finish current activity jika diperlukan
            activity?.finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}")
        }
    }

    private fun loadUserFromPreferences() {
        try {
            val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)

            // Load text data
            val nama = sharedPref.getString("nama", "Nama tidak tersedia")
            val email = sharedPref.getString("email", "Email tidak tersedia")
            val phone = sharedPref.getString("phone", "No. HP tidak tersedia")
            val role = sharedPref.getString("role", "User")

            // Set text data
            binding.txtNama.text = nama
            binding.txtEmail.text = email
            binding.txtPhone.text = phone

            // Load profile image
            loadProfileImage(sharedPref)

            Log.d(TAG, "User data loaded successfully - Name: $nama, Email: $email")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user data: ${e.message}")
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
}