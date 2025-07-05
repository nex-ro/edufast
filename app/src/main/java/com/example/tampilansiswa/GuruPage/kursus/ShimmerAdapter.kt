package com.example.tampilansiswa.GuruPage.kursus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.R

class ShimmerAdapter : RecyclerView.Adapter<ShimmerAdapter.ShimmerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShimmerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shimmer_layout_item, parent, false)
        return ShimmerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShimmerViewHolder, position: Int) {
        // No binding needed for shimmer
    }

    override fun getItemCount(): Int = 5 // minimal 1 supaya terlihat

    class ShimmerViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
