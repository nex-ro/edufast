package com.example.tampilansiswa.Guru

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.Data.Guru
import com.example.tampilansiswa.Dashboard.GuruAdapter
import com.example.tampilansiswa.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class GuruFragment : Fragment() {

    private lateinit var rvGuru: RecyclerView
    private lateinit var searchGuru: EditText
    private lateinit var layoutFavorit: LinearLayout
    private lateinit var layoutTerbaik: LinearLayout
    private lateinit var db: FirebaseFirestore
    private lateinit var guruAdapter: GuruAdapter
    private val guruList = mutableListOf<Guru>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guru, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize views
        initViews(view)

        // Setup RecyclerView
        setupRecyclerView()

        // Setup click listeners
        setupClickListeners()

        // Load guru data from Firestore
        loadGuruData()
    }

    private fun initViews(view: View) {
        rvGuru = view.findViewById(R.id.rvGuru)
        searchGuru = view.findViewById(R.id.searchGuru)
        layoutFavorit = view.findViewById(R.id.layoutFavorit)
        layoutTerbaik = view.findViewById(R.id.layoutTerbaik)
    }

    private fun setupRecyclerView() {
        guruAdapter = GuruAdapter(guruList)
        rvGuru.layoutManager = GridLayoutManager(requireContext(), 2)
        rvGuru.adapter = guruAdapter
    }

    private fun setupClickListeners() {
        layoutFavorit.setOnClickListener {
            Toast.makeText(requireContext(), "Klik Favorit", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), GuruFavoritActivity::class.java))
        }

        layoutTerbaik.setOnClickListener {
            startActivity(Intent(requireContext(), GuruTerbaikActivity::class.java))
        }
    }

    private fun loadGuruData() {
        db.collection("users")
            .whereEqualTo("role", "guru")
            .whereEqualTo("isActive", true)
            .orderBy("rating", Query.Direction.DESCENDING)
            .limit(6)
            .get()
            .addOnSuccessListener { documents ->
                guruList.clear()

                for (document in documents) {
                    try {
                        val nama = document.getString("nama") ?: "Nama tidak tersedia"
                        val email = document.getString("email") ?: ""
                        val phone = document.getString("phone") ?: ""
                        val gender = document.getString("gender") ?: ""
                        val imagePath = document.getString("imagePath") ?: ""
                        val uid = document.getString("uid") ?: ""

                        // Get rating, if not available set as "unrated"
                        val rating = document.getDouble("rating") ?: 0.0
                        val ratingText = if (rating > 0) rating.toString() else "Unrated"

                        // Set default subject based on gender or use a default value
                        val subject = getDefaultSubject(gender)

                        // Use default avatar based on gender
                        val avatarResource = getDefaultAvatar(gender)

                        val guru = Guru(
                            nama = nama,
                            subject = subject,
                            rating = if (rating > 0) rating else 0.0,
                            avatar = avatarResource,
                            email = email,
                            phone = phone,
                            uid = uid,
                            imagePath = imagePath,
                            ratingText = ratingText
                        )

                        guruList.add(guru)

                    } catch (e: Exception) {
                        Log.e("GuruFragment", "Error parsing guru data: ${e.message}")
                    }
                }

                // If no guru found with rating, get 6 teachers without rating filter
                if (guruList.isEmpty()) {
                    loadGuruWithoutRating()
                } else {
                    guruAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("GuruFragment", "Error getting guru data: ${exception.message}")
                // Fallback: load guru without rating filter
                loadGuruWithoutRating()
            }
    }

    private fun loadGuruWithoutRating() {
        db.collection("users")
            .whereEqualTo("role", "guru")
            .whereEqualTo("isActive", true)
            .limit(6)
            .get()
            .addOnSuccessListener { documents ->
                guruList.clear()

                for (document in documents) {
                    try {
                        val nama = document.getString("nama") ?: "Nama tidak tersedia"
                        val email = document.getString("email") ?: ""
                        val phone = document.getString("phone") ?: ""
                        val gender = document.getString("gender") ?: ""
                        val imagePath = document.getString("imagePath") ?: ""
                        val uid = document.getString("uid") ?: ""

                        // Set as unrated
                        val rating = 0.0
                        val ratingText = "Unrated"

                        // Set default subject based on gender or use a default value
                        val subject = getDefaultSubject(gender)

                        // Use default avatar based on gender
                        val avatarResource = getDefaultAvatar(gender)

                        val guru = Guru(
                            nama = nama,
                            subject = subject,
                            rating = rating,
                            avatar = avatarResource,
                            email = email,
                            phone = phone,
                            uid = uid,
                            imagePath = imagePath,
                            ratingText = ratingText
                        )

                        guruList.add(guru)

                    } catch (e: Exception) {
                        Log.e("GuruFragment", "Error parsing guru data: ${e.message}")
                    }
                }

                guruAdapter.notifyDataSetChanged()

                if (guruList.isEmpty()) {
                    Toast.makeText(requireContext(), "Tidak ada guru yang tersedia", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("GuruFragment", "Error getting guru data: ${exception.message}")
                Toast.makeText(requireContext(), "Gagal memuat data guru", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getDefaultSubject(gender: String?): String {
        return when (gender?.lowercase()) {
            "laki-laki" -> "Matematika"
            "perempuan" -> "Bahasa Indonesia"
            else -> "Umum"
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
            .commit()
    }
}