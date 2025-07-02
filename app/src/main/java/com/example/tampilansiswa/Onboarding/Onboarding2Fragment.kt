package com.example.tampilansiswa.Onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tampilansiswa.Auth.SignInActivity
import com.example.tampilansiswa.Auth.SignUpActivity
import com.example.tampilansiswa.MainActivity
import com.example.tampilansiswa.databinding.FragmentOnboarding2Binding

class Onboarding2Fragment : Fragment() {

    private var _binding: FragmentOnboarding2Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboarding2Binding.inflate(inflater, container, false)

        val sharedPref = requireActivity().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)

        val uid = sharedPref.getString("uid", null)
        val email = sharedPref.getString("email", null)
        val isActive = sharedPref.getBoolean("isActive", false)

        // Jika user pernah login dan datanya masih ada
        if (!uid.isNullOrEmpty() && !email.isNullOrEmpty() && isActive) {
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish() // agar tidak bisa kembali ke onboarding
        }

        // Arahkan ke Sign In
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(requireContext(), SignInActivity::class.java))
        }

        // Arahkan ke Sign Up
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(requireContext(), SignUpActivity::class.java))
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
