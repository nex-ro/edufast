package com.example.tampilansiswa.Kursus

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tampilansiswa.Data.Kursus
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.FragmentKursusSayaBinding

class KursusSayaFragment : Fragment() {

    private var _binding: FragmentKursusSayaBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: KursusAdapter

    private val dataBelumSelesai = mutableListOf(
        Kursus("Anika Rahman", "11:00", "22 Januari 2025", R.drawable.avatar1, "Belum"),
        Kursus("Ghina Putri", "15:00", "22 Januari 2025", R.drawable.avatar1, "Belum")
    )

    private val dataSelesai = mutableListOf(
        Kursus("Anika Rahman", "13:00", "16 November 2024", R.drawable.avatar1, "Selesai"),
        Kursus("Muhammad", "15:00", "8 Desember 2024", R.drawable.avatar2, "Selesai")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKursusSayaBinding.inflate(inflater, container, false)

        binding.rvKursus.layoutManager = LinearLayoutManager(requireContext())
        showKursus(dataBelumSelesai)
        updateTabUI(isBelumSelesai = true)

        binding.tabBelumSelesai.setOnClickListener {
            showKursus(dataBelumSelesai)
            updateTabUI(isBelumSelesai = true)
        }

        binding.tabSelesai.setOnClickListener {
            showKursus(dataSelesai)
            updateTabUI(isBelumSelesai = false)
        }

        return binding.root
    }

    private fun updateTabUI(isBelumSelesai: Boolean) {
        val blue = ContextCompat.getColor(requireContext(), R.color.blue)
        val gray = ContextCompat.getColor(requireContext(), R.color.gray)
        val dark = ContextCompat.getColor(requireContext(), R.color.black)

        if (isBelumSelesai) {
            binding.tabBelumSelesai.setTextColor(blue)
            binding.tabSelesai.setTextColor(gray)
            binding.lineBelumSelesai.setBackgroundColor(blue)
            binding.lineSelesai.setBackgroundColor(gray)
        } else {
            binding.tabBelumSelesai.setTextColor(gray)
            binding.tabSelesai.setTextColor(blue)
            binding.lineBelumSelesai.setBackgroundColor(gray)
            binding.lineSelesai.setBackgroundColor(blue)
        }
    }

    private fun showKursus(data: MutableList<Kursus>) {
        adapter = KursusAdapter(data)
        binding.rvKursus.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
