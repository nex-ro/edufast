package com.example.tampilansiswa.pelajaran

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingConfig
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.Data.Course
import com.example.tampilansiswa.R
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class pelajaran : Fragment() {

    private lateinit var adapter: CoursePagingAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pelajaran, container, false)

        firestore = FirebaseFirestore.getInstance()
        recyclerView = view.findViewById(R.id.rv_courses)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupAdapter()

        return view
    }

    private fun setupAdapter() {
        val query = firestore.collection("courses")
            .whereEqualTo("active", true)

        val config = PagingConfig(
            pageSize = 10,
            prefetchDistance = 5,
            enablePlaceholders = false
        )

        val options = FirestorePagingOptions.Builder<Course>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(query, config, Course::class.java)
            .build()

        adapter = CoursePagingAdapter(options)
        recyclerView.adapter = adapter
    }
}
