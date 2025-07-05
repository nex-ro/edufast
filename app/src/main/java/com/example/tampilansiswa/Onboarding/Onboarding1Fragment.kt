package com.example.tampilansiswa.Onboarding

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.FragmentOnboarding1Binding

class Onboarding1Fragment : Fragment() {

    private var _binding: FragmentOnboarding1Binding? = null
    private val binding get() = _binding!!

    private var hasNavigated = false // Hindari navigasi ganda
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboarding1Binding.inflate(inflater, container, false)
        handler.postDelayed({
            navigateNext()
        }, 3000)

        binding.root.setOnClickListener {
            navigateNext()
        }

        return binding.root
    }

    private fun navigateNext() {
        if (!hasNavigated) {
            hasNavigated = true
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_onboarding, Onboarding2Fragment())
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null) // Hentikan handler saat view hancur
        _binding = null
    }
}
