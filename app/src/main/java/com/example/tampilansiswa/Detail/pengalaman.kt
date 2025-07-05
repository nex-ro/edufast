package com.example.tampilansiswa.Detail

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.Data.Pengalaman
import com.example.tampilansiswa.R
import com.google.firebase.firestore.FirebaseFirestore

class pengalaman : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PengalamanAdapter
    private val pengalamanList = mutableListOf<Pengalaman>()
    private lateinit var firestore: FirebaseFirestore
    private var teacherId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pengalaman, container, false)

        // Initialize components
        recyclerView = view.findViewById(R.id.recyclerViewPengalaman)
        firestore = FirebaseFirestore.getInstance()

        // Get teacher ID from arguments
        teacherId = arguments?.getString("teacherId") ?: ""

        // Setup RecyclerView
        setupRecyclerView()

        val btnBack = view.findViewById<android.widget.ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            if (isAdded && activity != null) {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

        loadPengalamanData()

        return view
    }

    private fun setupRecyclerView() {
        adapter = PengalamanAdapter(pengalamanList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun loadPengalamanData() {
        if (teacherId.isEmpty()) {
            Log.e("PengalamanFragment", "Teacher ID is empty")
            showEmptyState("ID Guru tidak ditemukan")
            return
        }

        firestore.collection("pengalaman")
            .whereEqualTo("uid", teacherId)
            .get()
            .addOnSuccessListener { documents ->
                pengalamanList.clear()

                if (documents.isEmpty) {
                    showEmptyState("Belum ada pengalaman yang ditambahkan")
                } else {
                    for (document in documents) {
                        try {
                            val pengalaman = document.toObject(Pengalaman::class.java)
                            pengalaman.id = document.id
                            pengalamanList.add(pengalaman)
                        } catch (e: Exception) {
                            Log.e("PengalamanFragment", "Error parsing document: ${e.message}")
                        }
                    }

                    // Sort by tahunMulai descending (newest first)
                    pengalamanList.sortByDescending { it.tahunMulai }

                    adapter.notifyDataSetChanged()
                    hideEmptyState()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("PengalamanFragment", "Error getting documents: ", exception)
                showEmptyState("Gagal memuat data pengalaman: ${exception.message}")
            }
    }

    private fun showEmptyState(message: String) {
        // Hide RecyclerView
        recyclerView.visibility = View.GONE

        // Show empty state message
        val parent = recyclerView.parent as? ViewGroup
        parent?.let {
            // Remove existing empty message if any
            val existingMessage = it.findViewWithTag<TextView>("empty_pengalaman_message")
            existingMessage?.let { msg -> it.removeView(msg) }

            // Create new empty message
            val emptyText = TextView(requireContext()).apply {
                text = message
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
                setPadding(32, 32, 32, 32)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                tag = "empty_pengalaman_message"
            }

            // Add the message
            val index = it.indexOfChild(recyclerView)
            it.addView(emptyText, index + 1)
        }
    }

    private fun hideEmptyState() {
        // Show RecyclerView
        recyclerView.visibility = View.VISIBLE

        // Remove empty state message
        val parent = recyclerView.parent as? ViewGroup
        parent?.let {
            val existingMessage = it.findViewWithTag<TextView>("empty_pengalaman_message")
            existingMessage?.let { msg -> it.removeView(msg) }
        }
    }

    companion object {
        fun newInstance(teacherId: String): pengalaman {
            val fragment = pengalaman()
            val args = Bundle().apply {
                putString("teacherId", teacherId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}