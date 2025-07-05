package com.example.tampilansiswa.pelajaran

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.Data.Course
import com.example.tampilansiswa.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.*
import android.widget.Button
import android.widget.ImageView
import com.example.tampilansiswa.Dashboard.HomeFragment

class pelajaran : Fragment() {

    private lateinit var adapter: CoursePagingAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var searchEditText: EditText
    private lateinit var tabContainer: LinearLayout
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var emptyStateText: TextView
    private lateinit var retryButton: Button

    private var currentSearchQuery = ""
    private var currentSelectedSubject = "All"
    private var availableSubjects = mutableListOf<String>()
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pelajaran, container, false)

        firestore = FirebaseFirestore.getInstance()
        recyclerView = view.findViewById(R.id.rv_courses)
        searchEditText = view.findViewById(R.id.et_search)
        tabContainer = view.findViewById(R.id.tab_container)
        shimmerLayout = view.findViewById(R.id.shimmer_layout)
        emptyStateLayout = view.findViewById(R.id.empty_state_layout)
        emptyStateText = view.findViewById(R.id.empty_state_text)
        retryButton = view.findViewById(R.id.btn_retry)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupSearch()
        setupRetryButton()
        loadSubjects()

        view.findViewById<ImageView>(R.id.backbtnpel).setOnClickListener{
            navigateToFragment(HomeFragment())
        }

        return view
    }
    private fun navigateToFragment(fragment: Fragment) {
        try {
            requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit()
        } catch (e: Exception) {
        }
    }
    private fun setupSearch() {
        searchEditText.addTextChangedListener { text ->
            searchJob?.cancel()
            searchJob = CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                currentSearchQuery = text.toString().trim()
                updateAdapter()
            }
        }
    }

    private fun setupRetryButton() {
        retryButton.setOnClickListener {
            loadSubjects()
        }
    }

    private fun loadSubjects() {
        showShimmer(true)

        firestore.collection("courses")
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { documents ->
                val subjects = mutableSetOf<String>()

                for (document in documents) {
                    val subject = document.getString("subject") ?: ""
                    if (subject.isNotEmpty()) {
                        subjects.add(subject.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }

                availableSubjects.clear()
                availableSubjects.add("All")
                availableSubjects.addAll(subjects.sorted())

                setupTabs()
                setupAdapter()
            }
            .addOnFailureListener {
                setupDefaultTabs()
                setupAdapter()
                showShimmer(false)
            }
    }

    private fun setupTabs() {
        tabContainer.removeAllViews()

        availableSubjects.forEach { subject ->
            val tabView = createTabView(subject)
            tabContainer.addView(tabView)
        }

        if (availableSubjects.isNotEmpty()) {
            selectTab(availableSubjects[0])
        }
    }

    private fun setupDefaultTabs() {
        availableSubjects.clear()
        availableSubjects.addAll(listOf("All", "English", "Mathematics", "Science"))
        setupTabs()
    }

    private fun createTabView(subject: String): TextView {
        val tabView = TextView(requireContext())
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 24, 0)
        tabView.layoutParams = layoutParams

        tabView.text = subject
        tabView.textSize = 14f
        tabView.setPadding(48, 24, 48, 24)

        updateTabAppearance(tabView, subject == currentSelectedSubject)

        tabView.setOnClickListener {
            selectTab(subject)
        }

        return tabView
    }

    private fun selectTab(subject: String) {
        currentSelectedSubject = subject

        for (i in 0 until tabContainer.childCount) {
            val tabView = tabContainer.getChildAt(i) as TextView
            val isSelected = tabView.text == subject
            updateTabAppearance(tabView, isSelected)
        }

        updateAdapter()
    }

    private fun updateTabAppearance(tabView: TextView, isSelected: Boolean) {
        if (isSelected) {
            tabView.setBackgroundResource(R.color.primary_color)
            tabView.setTextColor(resources.getColor(android.R.color.white, null))
            tabView.setTypeface(null, android.graphics.Typeface.BOLD)
        } else {
            tabView.setBackgroundResource(R.drawable.tab_background)
            tabView.setTextColor(resources.getColor(android.R.color.black, null))
            tabView.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }

    private fun setupAdapter() {
        val query = buildQuery()

        val config = PagingConfig(
            pageSize = 20,
            prefetchDistance = 5,
            enablePlaceholders = false
        )

        val options = FirestorePagingOptions.Builder<Course>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(query, config, Course::class.java)
            .build()

        adapter = CoursePagingAdapter(options) { course ->
            navigateToDetailFragment(course)
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                showShimmer(false)
                checkEmptyState()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                showShimmer(false)
                checkEmptyState()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                showShimmer(false)
                checkEmptyState()
            }
        })

        recyclerView.adapter = adapter
        adapter.startListening()
    }

    private fun updateAdapter() {
        if (!::adapter.isInitialized) {
            setupAdapter()
            return
        }

        showShimmer(true)

        val query = buildQuery()

        val config = PagingConfig(
            pageSize = 20,
            prefetchDistance = 5,
            enablePlaceholders = false
        )

        val options = FirestorePagingOptions.Builder<Course>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(query, config, Course::class.java)
            .build()

        adapter.updateOptions(options)
    }

    private fun buildQuery(): Query {
        var query = firestore.collection("courses")
            .whereEqualTo("active", true)

        if (currentSelectedSubject != "All") {
            val subjectForQuery = currentSelectedSubject.lowercase()
            query = query.whereEqualTo("subject", subjectForQuery)
        }

        if (currentSearchQuery.isNotEmpty()) {
            query = query.orderBy("courseName")
                .startAt(currentSearchQuery.lowercase())
                .endAt(currentSearchQuery.lowercase() + "\uf8ff")
        } else {
            query = query.limit(20)
        }

        return query
    }

    private fun checkEmptyState() {
        val isEmpty = adapter.itemCount == 0

        if (isEmpty) {
            val message = when {
                currentSearchQuery.isNotEmpty() -> "Data tidak ditemukan untuk pencarian \"$currentSearchQuery\""
                currentSelectedSubject != "All" -> "Tidak ada kursus untuk kategori $currentSelectedSubject"
                else -> "Data tidak tersedia"
            }
            updateEmptyStateMessage(message)
            showEmptyState(true)
        } else {
            showEmptyState(false)
        }
    }

    private fun updateEmptyStateMessage(message: String) {
        emptyStateText.text = message
    }

    private fun showEmptyState(show: Boolean) {
        emptyStateLayout.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showShimmer(show: Boolean) {
        if (show) {
            shimmerLayout.visibility = View.VISIBLE
            shimmerLayout.startShimmer()
            recyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.GONE
        } else {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
        }
    }

    private fun navigateToDetailFragment(course: Course) {
        val detailFragment = CourseDetailFragment.newInstance(course)

        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onStart() {
        super.onStart()
        if (::adapter.isInitialized) {
            adapter.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::adapter.isInitialized) {
            adapter.stopListening()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::shimmerLayout.isInitialized) {
            shimmerLayout.startShimmer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::shimmerLayout.isInitialized) {
            shimmerLayout.stopShimmer()
        }
        searchJob?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
    }
}
