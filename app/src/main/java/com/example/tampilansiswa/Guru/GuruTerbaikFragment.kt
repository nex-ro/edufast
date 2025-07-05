package com.example.tampilansiswa.Guru

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.R
import com.example.tampilansiswa.Data.Guru
import com.example.tampilansiswa.Detail.DetailGuru
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.widget.LinearLayout
import com.example.tampilansiswa.guru.GuruTerbaikAdapter

class GuruTerbaikFragment : Fragment() {

    private lateinit var rvGuruTerbaik: RecyclerView
    private lateinit var guruAdapter: GuruTerbaikAdapter
    private lateinit var db: FirebaseFirestore
    private var allGuruList: List<Guru> = emptyList()
    private var currentSubject = "All"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guru_terbaik, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Setup toolbar
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbarGuruTerbaik)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        // Setup RecyclerView
        rvGuruTerbaik = view.findViewById(R.id.rvGuruTerbaik)
        rvGuruTerbaik.layoutManager = LinearLayoutManager(requireContext())

        // Load data from Firestore
        loadGuruData()
    }

    private fun setupChipsWithSubjects(subjects: List<String>) {
        val chipContainer = view?.findViewById<LinearLayout>(R.id.chipContainer)
        chipContainer?.removeAllViews()

        // Add "All" chip first
        val allChip = Chip(requireContext()).apply {
            text = "All"
            isCheckable = true
            isChecked = true
            setChipBackgroundColorResource(R.color.blue)
            setChipStrokeColorResource(R.color.blue)
            setTextColor(resources.getColor(android.R.color.white, null))
            chipStrokeWidth = 2f
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 16
            }
            setOnClickListener {
                filterBySubject("All")
                updateChipSelection(this)
            }
        }
        chipContainer?.addView(allChip)

        // Add subject chips
        subjects.forEach { subject ->
            val chip = Chip(requireContext()).apply {
                text = subject
                isCheckable = true
                setChipBackgroundColorResource(android.R.color.white)
                setChipStrokeColorResource(R.color.blue)
                setTextColor(resources.getColor(R.color.blue, null))
                chipStrokeWidth = 2f
                layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 16
                }
                setOnClickListener {
                    filterBySubject(subject)
                    updateChipSelection(this)
                }
            }
            chipContainer?.addView(chip)
        }
    }

    private fun updateChipSelection(selectedChip: Chip) {
        val chipContainer = view?.findViewById<LinearLayout>(R.id.chipContainer)
        chipContainer?.let { container ->
            for (i in 0 until container.childCount) {
                val chip = container.getChildAt(i) as? Chip
                chip?.let { currentChip ->
                    if (currentChip == selectedChip) {
                        // Selected chip style
                        currentChip.isChecked = true
                        currentChip.setChipBackgroundColorResource(R.color.blue)
                        currentChip.setTextColor(resources.getColor(android.R.color.white, null))
                    } else {
                        // Unselected chip style
                        currentChip.isChecked = false
                        currentChip.setChipBackgroundColorResource(android.R.color.white)
                        currentChip.setTextColor(resources.getColor(R.color.blue, null))
                    }
                }
            }
        }
    }

    private fun filterBySubject(subject: String) {
        currentSubject = subject
        val filteredList = if (subject == "All") {
            allGuruList
        } else {
            allGuruList.filter { it.subjek == subject }
        }

        // Update adapter with filtered and sorted data
        val sortedList = sortGuruByRating(filteredList)
        guruAdapter.updateData(sortedList)
    }

    private fun navigateToDetailGuru(guru: Guru) {
        val detailFragment = DetailGuru.newInstance(
            guruId = guru.uid,
            nama = guru.nama,
            mapel = guru.subjek,
            universitas = guru.education,
            gender = guru.gender,
            phone = guru.phone,
            email = guru.email,
            tentang = guru.bio,
            siswa = guru.studentCount,
            rating = guru.averageRating,
            ulasanCount = guru.totalRating,
            gambar = getDefaultAvatarByGender(guru.gender)
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun getDefaultAvatarByGender(gender: String): Int {
        return when {
            gender.equals("female", ignoreCase = true) -> R.drawable.avatar3
            gender.equals("male", ignoreCase = true) -> R.drawable.avatar2
            else -> R.drawable.avatar1
        }
    }

    private fun loadGuruData() {
        db.collection("users")
            .whereEqualTo("role", "guru")
            .get()
            .addOnSuccessListener { documents ->
                val guruList = mutableListOf<Guru>()

                for (document in documents) {
                    try {
                        val guru = Guru(
                            nama = document.getString("nama") ?: "Unknown",
                            rating = document.getDouble("averageRating") ?: 0.0,
                            avatar = R.drawable.avatar1, // Default avatar
                            email = document.getString("email") ?: "",
                            phone = document.getString("phone") ?: "",
                            uid = document.getString("uid") ?: document.id,
                            imagePath = document.getString("imagePath") ?: "",
                            bio = document.getString("bio") ?: "",
                            education = document.getString("education") ?: "",
                            gender = document.getString("gender") ?: "",
                            averageRating = document.getDouble("averageRating") ?: 0.0,
                            totalRating = document.getLong("totalRating")?.toInt() ?: 0,
                            siswa = document.getLong("siswa")?.toInt() ?: 0,
                            studentCount = document.getLong("studentCount")?.toInt() ?: 0,
                            isActive = document.getBoolean("isActive") ?: true,
                            profileImageUrl = document.getString("profileImageUrl") ?: "",
                            role = document.getString("role") ?: "guru",
                            createdAt = document.getLong("createdAt") ?: 0,
                            updatedAt = document.get("updatedAt"),
                            subjek = document.getString("subjek") ?: "Unknown",
                        )
                        guruList.add(guru)
                    } catch (e: Exception) {
                        Log.e("GuruTerbaik", "Error parsing guru data: ${e.message}")
                    }
                }

                // Sort by rating and store
                allGuruList = sortGuruByRating(guruList)

                // Initialize adapter with click listener
                guruAdapter = GuruTerbaikAdapter(allGuruList) { guru ->
                    navigateToDetailGuru(guru)
                }
                rvGuruTerbaik.adapter = guruAdapter

                // Setup chips after data is loaded
                val subjects = allGuruList.map { it.subjek }.distinct().filter { it != "Unknown" }
                setupChipsWithSubjects(subjects)
            }
            .addOnFailureListener { exception ->
                Log.e("GuruTerbaik", "Error getting documents: ", exception)
            }
    }

    private fun sortGuruByRating(guruList: List<Guru>): List<Guru> {
        return guruList.sortedWith(compareByDescending<Guru> { guru ->
            // First, sort by whether they have a rating (rated teachers first)
            if (guru.rating > 0) 1 else 0
        }.thenByDescending { guru ->
            // Then sort by rating value (highest first)
            guru.rating
        }.thenBy { guru ->
            // Finally, sort by name alphabetically for consistency
            guru.nama
        })
    }
}