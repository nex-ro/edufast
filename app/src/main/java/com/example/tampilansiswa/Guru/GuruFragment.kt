package com.example.tampilansiswa.Guru

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.Data.Guru
import com.example.tampilansiswa.Guru.GuruAdapter
import com.example.tampilansiswa.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class GuruFragment : Fragment() {

    private var rvGuru: RecyclerView? = null
    private var searchGuru: EditText? = null
    private var layoutFavorit: LinearLayout? = null
    private var layoutTerbaik: LinearLayout? = null
    private var progressBar: ProgressBar? = null
    private var emptyState: LinearLayout? = null
    private var btnLihatSelanjutnya: Button? = null
    private var db: FirebaseFirestore? = null
    private var guruAdapter: GuruAdapter? = null
    private val guruList = mutableListOf<Guru>()
    private val allGuruList = mutableListOf<Guru>()
    private val filteredGuruList = mutableListOf<Guru>()
    private var currentPage = 0
    private val itemsPerPage = 6
    private var isLoading = false
    private var isSearching = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guru, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Initialize Firestore
            db = FirebaseFirestore.getInstance()

            // Initialize views
            initViews(view)

            // Setup RecyclerView
            setupRecyclerView()

            // Setup click listeners
            setupClickListeners()

            // Setup search functionality
            setupSearchFunctionality()

            // Load guru data from Firestore
            loadAllGuruData()
        } catch (e: Exception) {
            Log.e("GuruFragment", "Error in onViewCreated: ${e.message}", e)
            handleError("Terjadi kesalahan saat memuat halaman")
        }
    }

    private fun initViews(view: View) {
        try {
            rvGuru = view.findViewById(R.id.rvGuru)
            searchGuru = view.findViewById(R.id.etSearch)
            layoutFavorit = view.findViewById(R.id.layoutFavorit)
            layoutTerbaik = view.findViewById(R.id.layoutTerbaik)
            progressBar = view.findViewById(R.id.progressBar)
            emptyState = view.findViewById(R.id.emptyState)
            btnLihatSelanjutnya = view.findViewById(R.id.btnLihatSelanjutnya)
        } catch (e: Exception) {
            Log.e("GuruFragment", "Error initializing views: ${e.message}", e)
            throw e
        }
    }

    private fun setupRecyclerView() {
        try {
            if (context != null && rvGuru != null) {
                guruAdapter = GuruAdapter(guruList)
                rvGuru?.layoutManager = GridLayoutManager(requireContext(), 2)
                rvGuru?.adapter = guruAdapter
            }
        } catch (e: Exception) {
            Log.e("GuruFragment", "Error setting up RecyclerView: ${e.message}", e)
            handleError("Gagal menyiapkan tampilan")
        }
    }

    private fun setupClickListeners() {
        try {
            layoutFavorit?.setOnClickListener {
                if (context != null) {
                    try {
                        navigateToFragment(GuruFavoritFragment())
                    } catch (e: Exception) {
                        Log.e("GuruFragment", "Error starting GuruFavoritActivity: ${e.message}", e)
                        Toast.makeText(requireContext(), "Gagal membuka halaman favorit", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            layoutTerbaik?.setOnClickListener {
                if (context != null) {
                    try {
                        navigateToFragment(GuruTerbaikFragment())
                    } catch (e: Exception) {
                        Log.e("GuruFragment", "Error starting GuruTerbaikActivity: ${e.message}", e)
                        Toast.makeText(requireContext(), "Gagal membuka halaman terbaik", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            btnLihatSelanjutnya?.setOnClickListener {
                loadMoreGuru()
            }
        } catch (e: Exception) {
            Log.e("GuruFragment", "Error setting up click listeners: ${e.message}", e)
        }
    }

    private fun setupSearchFunctionality() {
        searchGuru?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                try {
                    val query = s.toString().trim()
                    if (query.isEmpty()) {
                        isSearching = false
                        loadInitialGuru()
                    } else {
                        isSearching = true
                        searchGuru(query)
                    }
                } catch (e: Exception) {
                    Log.e("GuruFragment", "Error in search: ${e.message}", e)
                }
            }
        })
    }

    private fun searchGuru(query: String) {
        try {
            filteredGuruList.clear()

            for (guru in allGuruList) {
                if (guru.nama.contains(query, ignoreCase = true) ||
                    guru.education.contains(query, ignoreCase = true)) {
                    filteredGuruList.add(guru)
                }
            }

            guruList.clear()
            guruList.addAll(filteredGuruList)
            guruAdapter?.notifyDataSetChanged()

            if (guruList.isEmpty()) {
                showEmptyState(true)
            } else {
                showEmptyState(false)
            }

            btnLihatSelanjutnya?.visibility = View.GONE
        } catch (e: Exception) {
            Log.e("GuruFragment", "Error in searchGuru: ${e.message}", e)
        }
    }

    private fun showLoading(show: Boolean) {
        try {
            isLoading = show
            if (show) {
                progressBar?.visibility = View.VISIBLE
                if (guruList.isEmpty()) {
                    rvGuru?.visibility = View.GONE
                    emptyState?.visibility = View.GONE
                }
            } else {
                progressBar?.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e("GuruFragment", "Error in showLoading: ${e.message}", e)
        }
    }

    private fun showEmptyState(show: Boolean) {
        try {
            if (show) {
                emptyState?.visibility = View.VISIBLE
                rvGuru?.visibility = View.GONE
                btnLihatSelanjutnya?.visibility = View.GONE
            } else {
                emptyState?.visibility = View.GONE
                rvGuru?.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e("GuruFragment", "Error in showEmptyState: ${e.message}", e)
        }
    }

    private fun updateButtonVisibility() {
        try {
            if (!isSearching && allGuruList.isNotEmpty()) {
                val totalPages = (allGuruList.size + itemsPerPage - 1) / itemsPerPage
                btnLihatSelanjutnya?.visibility = if (currentPage < totalPages - 1) View.VISIBLE else View.GONE
            } else {
                btnLihatSelanjutnya?.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e("GuruFragment", "Error in updateButtonVisibility: ${e.message}", e)
        }
    }

    private fun loadAllGuruData() {
        if (db == null) {
            handleError("Database tidak tersedia")
            return
        }

        showLoading(true)

        db!!.collection("users")
            .whereEqualTo("role", "guru")
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { documents ->
                try {
                    allGuruList.clear()

                    for (document in documents) {
                        try {
                            val guru = createGuruFromDocument(document)
                            allGuruList.add(guru)
                        } catch (e: Exception) {
                            Log.e("GuruFragment", "Error parsing guru data: ${e.message}", e)
                        }
                    }

                    // Sort by student count (descending)
                    allGuruList.sortByDescending { guru ->
                        when {
                            guru.siswa > 0 -> guru.siswa
                            guru.studentCount > 0 -> guru.studentCount
                            else -> 0
                        }
                    }

                    showLoading(false)

                    if (allGuruList.isEmpty()) {
                        showEmptyState(true)
                        if (context != null) {
                            Toast.makeText(requireContext(), "Tidak ada guru yang tersedia", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        currentPage = 0
                        loadInitialGuru()
                    }
                } catch (e: Exception) {
                    Log.e("GuruFragment", "Error processing guru data: ${e.message}", e)
                    showLoading(false)
                    handleError("Gagal memproses data guru")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("GuruFragment", "Error getting guru data: ${exception.message}", exception)
                showLoading(false)
                handleError("Gagal memuat data guru")
            }
    }

    private fun loadInitialGuru() {
        try {
            guruList.clear()
            val endIndex = minOf(itemsPerPage, allGuruList.size)

            for (i in 0 until endIndex) {
                guruList.add(allGuruList[i])
            }

            guruAdapter?.notifyDataSetChanged()
            showEmptyState(false)
            updateButtonVisibility()
        } catch (e: Exception) {
            Log.e("GuruFragment", "Error in loadInitialGuru: ${e.message}", e)
            handleError("Gagal memuat data awal")
        }
    }

    private fun loadMoreGuru() {
        if (isLoading || isSearching) return

        try {
            val startIndex = (currentPage + 1) * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, allGuruList.size)

            if (startIndex < allGuruList.size) {
                showLoading(true)

                // Simulate loading delay
                rvGuru?.postDelayed({
                    try {
                        for (i in startIndex until endIndex) {
                            guruList.add(allGuruList[i])
                        }

                        guruAdapter?.notifyItemRangeInserted(startIndex, endIndex - startIndex)
                        currentPage++

                        showLoading(false)
                        updateButtonVisibility()
                    } catch (e: Exception) {
                        Log.e("GuruFragment", "Error in loadMoreGuru delayed task: ${e.message}", e)
                        showLoading(false)
                        handleError("Gagal memuat data tambahan")
                    }
                }, 500)
            }
        } catch (e: Exception) {
            Log.e("GuruFragment", "Error in loadMoreGuru: ${e.message}", e)
            handleError("Gagal memuat data tambahan")
        }
    }

    private fun createGuruFromDocument(document: com.google.firebase.firestore.DocumentSnapshot): Guru {
        try {
            val nama = document.getString("nama") ?: "Nama tidak tersedia"
            val email = document.getString("email") ?: ""
            val phone = document.getString("phone") ?: ""
            val gender = document.getString("gender") ?: ""
            val imagePath = document.getString("imagePath") ?: ""
            val uid = document.getString("uid") ?: ""
            val bio = document.getString("bio") ?: ""
            val education = document.getString("education") ?: ""
            val profileImageUrl = document.getString("profileImageUrl") ?: ""
            val role = document.getString("role") ?: "guru"

            // Get ratings safely
            val averageRating = try {
                document.getDouble("averageRating") ?: 0.0
            } catch (e: Exception) {
                0.0
            }

            val rating = try {
                document.getDouble("rating") ?: 0.0
            } catch (e: Exception) {
                0.0
            }

            val totalRating = try {
                document.getLong("totalRating")?.toInt() ?: 0
            } catch (e: Exception) {
                0
            }

            // Get student count safely
            val siswa = try {
                document.getLong("siswa")?.toInt() ?: 0
            } catch (e: Exception) {
                0
            }

            val studentCount = try {
                document.getLong("studentCount")?.toInt() ?: 0
            } catch (e: Exception) {
                0
            }

            val isActive = try {
                document.getBoolean("isActive") ?: true
            } catch (e: Exception) {
                true
            }

            val createdAt = try {
                document.getLong("createdAt") ?: 0
            } catch (e: Exception) {
                0
            }

            val updatedAt = document.get("updatedAt")

            // Use averageRating if available, otherwise use rating
            val displayRating = if (averageRating > 0) averageRating else rating
            val ratingText = if (displayRating > 0) String.format("%.1f", displayRating) else "Belum dinilai"

            // Use default avatar based on gender
            val avatarResource = getDefaultAvatar(gender)

            return Guru(
                nama = nama,
                rating = displayRating,
                avatar = avatarResource,
                email = email,
                phone = phone,
                uid = uid,
                imagePath = imagePath,
                ratingText = ratingText,
                bio = bio,
                education = education,
                gender = gender,
                averageRating = averageRating,
                totalRating = totalRating,
                siswa = siswa,
                studentCount = studentCount,
                isActive = isActive,
                profileImageUrl = profileImageUrl,
                role = role,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        } catch (e: Exception) {
            Log.e("GuruFragment", "Error creating guru from document: ${e.message}", e)
            throw e
        }
    }

    private fun getDefaultAvatar(gender: String?): Int {
        return when (gender?.lowercase()) {
            "laki-laki" -> R.drawable.avatar1
            "perempuan" -> R.drawable.avatar2
            else -> R.drawable.avatar3
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun handleError(message: String) {
        try {
            showLoading(false)
            showEmptyState(true)
            if (context != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("GuruFragment", "Error in handleError: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up references to prevent memory leaks
        rvGuru = null
        searchGuru = null
        layoutFavorit = null
        layoutTerbaik = null
        progressBar = null
        emptyState = null
        btnLihatSelanjutnya = null
        guruAdapter = null
        db = null
    }
}