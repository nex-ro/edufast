package com.example.tampilansiswa.GuruPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tampilansiswa.GuruPage.kursus.guru_kursus
import com.example.tampilansiswa.databinding.FragmentDashboardGuruBinding
import com.example.tampilansiswa.R

class Dashboard_guru : Fragment() {

    private var _binding: FragmentDashboardGuruBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardGuruBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardjadwal.setOnClickListener {
            Toast.makeText(requireContext(), "Button ditekan", Toast.LENGTH_SHORT).show()
            navigateToFragment(guru_kursus())
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
