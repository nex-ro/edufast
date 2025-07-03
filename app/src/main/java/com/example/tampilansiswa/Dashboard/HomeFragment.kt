package com.example.tampilansiswa.Dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.tampilansiswa.Data.Guru
import com.example.tampilansiswa.Guru.GuruActivity
import com.example.tampilansiswa.Guru.GuruFragment
import com.example.tampilansiswa.Kursus.KursusSayaFragment
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.FragmentHomeBinding
import com.example.tampilansiswa.pelajaran.pelajaran

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var guruAdapter: GuruAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listGuru = listOf(
            Guru("Anika Rahman", "Bahasa Inggris", 4.9, R.drawable.avatar1),
            Guru("Muhammad", "Fisika", 4.9, R.drawable.avatar2),
            Guru("Laila", "Matematika", 5.0, R.drawable.avatar3),
            Guru("Arif", "Kimia", 4.8, R.drawable.avatar4)
        )
        guruAdapter = GuruAdapter(listGuru)
        binding.rvGuru.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = guruAdapter
        }
        binding.menuPelajaran.setOnClickListener {
            navigateToFragment(pelajaran())
        }
        binding.menuGuru.setOnClickListener {
            navigateToFragment(GuruFragment())
        }

        binding.menuKursus.setOnClickListener {
            navigateToFragment(KursusSayaFragment())
        }
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
