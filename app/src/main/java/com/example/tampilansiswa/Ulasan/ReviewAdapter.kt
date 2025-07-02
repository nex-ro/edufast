package com.example.tampilansiswa.Ulasan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.R
import com.example.tampilansiswa.Data.Review

class ReviewAdapter(private val items: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvReviewer: TextView = itemView.findViewById(R.id.tvReviewer)
        private val tvStar: TextView = itemView.findViewById(R.id.tvStar)
        private val tvComment: TextView = itemView.findViewById(R.id.tvComment)

        fun bind(review: Review) {
            tvReviewer.text = review.reviewer
            tvStar.text = "★".repeat(review.rating)
            tvComment.text = review.comment
        }
    }
}