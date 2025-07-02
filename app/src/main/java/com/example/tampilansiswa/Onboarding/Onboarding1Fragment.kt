package com.example.tampilansiswa.Onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.FragmentOnboarding1Binding

class Onboarding1Fragment : Fragment() {

    private var _binding: FragmentOnboarding1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboarding1Binding.inflate(inflater, container, false)

        // Tap di mana saja di layar akan berpindah ke Onboarding2Fragment
        binding.root.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_onboarding, Onboarding2Fragment())
                .commit()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
