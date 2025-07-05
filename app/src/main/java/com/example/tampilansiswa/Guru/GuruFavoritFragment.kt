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
import com.example.tampilansiswa.guru.GuruFavoritAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.widget.LinearLayout

class GuruFavoritFragment : Fragment() {

    private lateinit var rvGuruTerbaik: RecyclerView
    private lateinit var guruAdapter: GuruFavoritAdapter
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
        val sortedList = sortGuruByStudentCount(filteredList)
        guruAdapter.updateData(sortedList)
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
                            domisili = document.getString("domisili") ?: "",
                            phone = document.getString("phone") ?: "",
                            uid = document.getString("uid") ?: document.id,
                            imagePath = document.getString("imagePath") ?: "",
                            bio = document.getString("bio") ?: "",
                            education = document.getString("education") ?: "Tidak ada informasi",
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
                            subjek = document.getString("subjek") ?: "Mata Pelajaran Tidak Diketahui",
                        )
                        guruList.add(guru)
                    } catch (e: Exception) {
                        Log.e("GuruFavorit", "Error parsing guru data: ${e.message}")
                    }
                }

                // Sort by student count and store
                allGuruList = sortGuruByStudentCount(guruList)

                // Initialize adapter dengan click listener
                guruAdapter = GuruFavoritAdapter(allGuruList) { guru ->
                    navigateToGuruDetail(guru)
                }
                rvGuruTerbaik.adapter = guruAdapter

                // Setup chips after data is loaded
                val subjects = allGuruList.map { it.subjek }.distinct().filter { it != "Unknown" && it != "Mata Pelajaran Tidak Diketahui" }
                setupChipsWithSubjects(subjects)

                Log.d("GuruFavorit", "Successfully loaded ${allGuruList.size} guru(s)")
            }
            .addOnFailureListener { exception ->
                Log.e("GuruFavorit", "Error getting documents: ", exception)
            }
    }

    private fun navigateToGuruDetail(guru: Guru) {
        val detailFragment = DetailGuru.newInstance(
            guruId = guru.uid,
            nama = guru.nama,
            mapel = guru.subjek,
            universitas = guru.education,
            gender = guru.gender,
            phone = guru.phone,
            email = guru.email,
            tentang = guru.bio,
            siswa = guru.siswa + guru.studentCount, // Total student count
            rating = guru.averageRating,
            ulasanCount = guru.totalRating,
            gambar = guru.avatar
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun sortGuruByStudentCount(guruList: List<Guru>): List<Guru> {
        return guruList.sortedWith(compareByDescending<Guru> { guru ->
            // First priority: Sort by total student count (siswa + studentCount)
            val totalStudents = guru.siswa + guru.studentCount
            totalStudents
        }.thenByDescending { guru ->
            // Second priority: Sort by rating (highest first)
            guru.rating
        }.thenBy { guru ->
            // Third priority: Sort by name alphabetically for consistency
            guru.nama
        })
    }
}