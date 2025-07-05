package com.example.tampilansiswa.Dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.tampilansiswa.Data.Guru
import com.example.tampilansiswa.Guru.GuruFragment
import com.example.tampilansiswa.Kursus.KursusSayaFragment
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.FragmentHomeBinding
import com.example.tampilansiswa.pelajaran.pelajaran
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var guruAdapter: GuruAdapter
    private val db = FirebaseFirestore.getInstance()
    private val guruList = mutableListOf<Guru>()
    private var isDataLoading = false

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

        setupRecyclerView()
        setupMenuClickListeners()
        loadGuruData()
    }

    private fun setupRecyclerView() {
        // Check if fragment is still attached
        if (!isAdded || _binding == null) return

        guruAdapter = GuruAdapter(guruList)
        binding.rvGuru.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = guruAdapter
        }
    }

    private fun setupMenuClickListeners() {
        // Check if fragment is still attached
        val sharedPref = requireActivity().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
        val namaaa = sharedPref.getString("nama", "") ?: ""

        binding.nama.text="Hi ,"+namaaa
        if (!isAdded || _binding == null) return

        binding.menuPelajaran.setOnClickListener {
            if (isAdded && _binding != null) {
                navigateToFragment(pelajaran())
            }
        }

        binding.menuGuru.setOnClickListener {
            if (isAdded && _binding != null) {
                navigateToFragment(GuruFragment())
            }
        }

        binding.menuKursus.setOnClickListener {
            if (isAdded && _binding != null) {
                navigateToFragment(KursusSayaFragment())
            }
        }

        // See More button click listener
        binding.btnSeeMore.setOnClickListener {
            if (isAdded && _binding != null) {
                navigateToFragment(GuruFragment())
            }
        }
    }

    private fun loadGuruData() {
        // Check if fragment is still attached
        if (!isAdded || _binding == null) return

        // Prevent multiple simultaneous loads
        if (isDataLoading) return

        isDataLoading = true

        // Show loading - safely
        safeUpdateUI {
            binding.progressBar.visibility = View.VISIBLE
            binding.rvGuru.visibility = View.GONE
        }

        db.collection("users")
            .whereEqualTo("role", "guru")
            .get()
            .addOnSuccessListener { documents ->
                // Check if fragment is still attached before processing
                if (!isAdded || _binding == null) {
                    isDataLoading = false
                    return@addOnSuccessListener
                }

                // Use lifecycleScope to ensure safe UI updates
                lifecycleScope.launch {
                    try {
                        processGuruData(documents)
                    } catch (e: Exception) {
                        Log.e("HomeFragment", "Error processing data: ${e.message}", e)
                        handleDataLoadError("Error processing data")
                    } finally {
                        isDataLoading = false
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Check if fragment is still attached
                if (!isAdded || _binding == null) {
                    isDataLoading = false
                    return@addOnFailureListener
                }

                lifecycleScope.launch {
                    handleDataLoadError(exception.message ?: "Unknown error")
                    isDataLoading = false
                }
            }
    }

    private fun processGuruData(documents: com.google.firebase.firestore.QuerySnapshot) {
        guruList.clear()
        val tempGuruList = mutableListOf<Guru>()

        for (document in documents) {
            try {
                val nama = document.getString("nama") ?: "Unknown"
                val rating = when (val ratingValue = document.get("averageRating")) {
                    is Long -> ratingValue.toDouble()
                    is Double -> ratingValue
                    else -> 0.0
                }

                val guru = Guru(
                    nama = nama,
                    rating = rating,
                    avatar = R.drawable.avatar1,
                    email = document.getString("email") ?: "",
                    phone = document.getString("phone") ?: "",
                    uid = document.getString("uid") ?: document.id,
                    imagePath = document.getString("imagePath") ?: "",
                    bio = document.getString("bio") ?: "",
                    education = document.getString("education") ?: "",
                    gender = document.getString("gender") ?: "",
                    averageRating = rating,
                    totalRating = (document.getLong("totalRating") ?: 0).toInt(),
                    studentCount = (document.getLong("studentCount") ?: 0).toInt(),
                    isActive = document.getBoolean("isActive") ?: true,
                    profileImageUrl = document.getString("profileImageUrl") ?: "",
                    role = document.getString("role") ?: "guru",
                    createdAt = document.getLong("createdAt") ?: 0
                )

                tempGuruList.add(guru)
                Log.d("HomeFragment", "Added guru: ${guru.nama} with rating: ${guru.rating}")

            } catch (e: Exception) {
                Log.e("HomeFragment", "Error processing document ${document.id}: ${e.message}", e)
            }
        }

        // Sort by rating (highest first) and take top 4
        val sortedGuruList = tempGuruList
            .sortedByDescending { it.averageRating }
            .take(4)

        guruList.addAll(sortedGuruList)

        Log.d("HomeFragment", "Final guru list size: ${guruList.size}")
        Log.d("HomeFragment", "Top guru ratings: ${guruList.map { "${it.nama}: ${it.averageRating}" }}")

        // Update UI safely
        safeUpdateUI {
            binding.progressBar.visibility = View.GONE
            binding.rvGuru.visibility = View.VISIBLE

            if (guruList.isNotEmpty()) {
                binding.btnSeeMore.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
                guruAdapter.notifyDataSetChanged()
            } else {
                binding.btnSeeMore.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
                binding.rvGuru.visibility = View.GONE
            }
        }
    }

    private fun handleDataLoadError(errorMessage: String) {
        Log.e("HomeFragment", "Firestore error: $errorMessage")

        safeUpdateUI {
            binding.progressBar.visibility = View.GONE
            binding.rvGuru.visibility = View.VISIBLE
        }

        // Show toast safely
        if (isAdded && context != null) {
            Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
        }

        // Show fallback data
        loadFallbackData()
    }

    private fun loadFallbackData() {
        // Check if fragment is still attached
        if (!isAdded || _binding == null) return

        Log.d("HomeFragment", "Loading fallback data...")
        val fallbackGuru = listOf(
            Guru(nama = "Anika Rahman", rating = 4.9, avatar = R.drawable.avatar1),
            Guru(nama = "Muhammad", rating = 4.9, avatar = R.drawable.avatar2),
            Guru(nama = "Laila", rating = 5.0, avatar = R.drawable.avatar3),
            Guru(nama = "Arif", rating = 4.8, avatar = R.drawable.avatar4)
        )

        guruList.clear()
        guruList.addAll(fallbackGuru)

        safeUpdateUI {
            guruAdapter.notifyDataSetChanged()
            binding.btnSeeMore.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
            binding.rvGuru.visibility = View.VISIBLE
        }
    }

    /**
     * Safely update UI only if fragment is still attached and binding is not null
     */
    private fun safeUpdateUI(updateAction: () -> Unit) {
        if (isAdded && _binding != null && isResumed) {
            try {
                updateAction()
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error updating UI: ${e.message}", e)
            }
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        // Check if fragment is still attached and activity is not null
        if (!isAdded || activity == null || _binding == null) return

        try {
            requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit()
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error navigating to fragment: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel any ongoing operations
        isDataLoading = false
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        // Additional cleanup if needed
        isDataLoading = false
    }
}