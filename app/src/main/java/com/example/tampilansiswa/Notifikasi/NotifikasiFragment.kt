package com.example.tampilansiswa.Notifikasi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tampilansiswa.databinding.FragmentNotifikasiBinding

class NotifikasiFragment : Fragment() {

    private lateinit var binding: FragmentNotifikasiBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotifikasiBinding.inflate(inflater, container, false)
        return binding.root
    }
}
