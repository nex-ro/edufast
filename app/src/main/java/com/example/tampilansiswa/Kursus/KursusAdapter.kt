package com.example.tampilansiswa.Kursus

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.Data.Kursus
import com.example.tampilansiswa.Ulasan.UlasanActivity
import com.example.tampilansiswa.databinding.ItemKursusBinding
import com.example.tampilansiswa.R
import com.example.tampilansiswa.Ulasan.ulasan
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.launch
import java.io.File
import java.lang.ref.WeakReference

class KursusAdapter(
    private val list: MutableList<Kursus>,
    private val fragment: Fragment? = null
) : RecyclerView.Adapter<KursusAdapter.ViewHolder>() {

    private var overlayView: View? = null
    private var contextRef: WeakReference<Context>? = null
    private val TAG = "KursusAdapter"

    inner class ViewHolder(val binding: ItemKursusBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Kursus, position: Int) {
            try {
                // Validate data first
                if (!isValidKursus(data)) {
                    Log.w(TAG, "Invalid kursus data at position $position")
                    bindDefaultData(data)
                    return
                }

                // Store context reference safely
                val context = binding.root.context
                contextRef = WeakReference(context)

                bindCourseData(data, context)
                bindTeacherImage(data, context)
                setStatusColor(data.status)
                configureButtons(data.status)
                setupButtonListeners(data)

            } catch (e: Exception) {
                Log.e(TAG, "Error binding data at position $position", e)
                bindDefaultData(data)
            }
        }

        private fun isValidKursus(data: Kursus): Boolean {
            return try {
                data.courseName.isNotEmpty() &&
                        data.namaGuru.isNotEmpty() &&
                        data.status.isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }

        private fun bindDefaultData(data: Kursus) {
            try {
                binding.txtCourseName.text = data.courseName.takeIf { it.isNotEmpty() } ?: "Unknown Course"
                binding.txtNama.text = data.namaGuru.takeIf { it.isNotEmpty() } ?: "Unknown Teacher"
                binding.txtWaktu.text = "${data.waktu} - ${data.tanggal}"
                binding.txtLocation.text = data.fullLocation
                binding.txtPrice.text = data.formattedPrice
                binding.txtDuration.text = data.formattedDuration
                binding.txtCourseType.text = data.courseType
                binding.txtStatus.text = data.status.takeIf { it.isNotEmpty() } ?: "pending"
                binding.imgGuru.setImageResource(R.drawable.avatar1)

                // Hide all buttons for safety
                binding.viewLine.visibility = View.GONE
                binding.layoutTombol.visibility = View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Error binding default data", e)
            }
        }

        private fun bindCourseData(data: Kursus, context: Context) {
            try {
                binding.txtCourseName.text = data.courseName
                binding.txtNama.text = data.namaGuru
                binding.txtWaktu.text = "${data.waktu} - ${data.tanggal}"
                binding.txtLocation.text = data.fullLocation
                binding.txtPrice.text = data.formattedPrice
                binding.txtDuration.text = data.formattedDuration
                binding.txtCourseType.text = data.courseType
                binding.txtStatus.text = data.status
            } catch (e: Exception) {
                Log.e(TAG, "Error binding course data", e)
            }
        }

        private fun bindTeacherImage(data: Kursus, context: Context) {
            try {
                if (data.poster.isNotEmpty()) {
                    val file = File(data.poster)
                    if (file.exists() && file.canRead()) {
                        val requestOptions = RequestOptions()
                            .placeholder(R.drawable.avatar1)
                            .error(R.drawable.avatar1)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .timeout(10000)

                        Glide.with(context)
                            .load(file)
                            .apply(requestOptions)
                            .into(binding.imgGuru)
                    } else {
                        binding.imgGuru.setImageResource(R.drawable.avatar1)
                    }
                } else {
                    binding.imgGuru.setImageResource(R.drawable.avatar1)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading teacher image", e)
                binding.imgGuru.setImageResource(R.drawable.avatar1)
            }
        }

        private fun setStatusColor(status: String) {
            try {
                val context = contextRef?.get() ?: return

                when (status.lowercase().trim()) {
                    "pending" -> {
                        binding.txtStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark))
                        binding.txtStatus.setBackgroundResource(R.drawable.bg_status_orange)
                    }
                    "approved", "confirmed" -> {
                        binding.txtStatus.setTextColor(context.getColor(android.R.color.holo_blue_dark))
                        binding.txtStatus.setBackgroundResource(R.drawable.bg_status_blue)
                    }
                    "completed", "selesai" -> {
                        binding.txtStatus.setTextColor(context.getColor(android.R.color.holo_green_dark))
                        binding.txtStatus.setBackgroundResource(R.drawable.bg_status_green)
                    }
                    "cancelled", "cancel" -> {
                        binding.txtStatus.setTextColor(context.getColor(android.R.color.holo_red_dark))
                        binding.txtStatus.setBackgroundResource(R.drawable.bg_status_red)
                    }
                    else -> {
                        // Default styling
                        binding.txtStatus.setTextColor(context.getColor(android.R.color.darker_gray))
                        binding.txtStatus.setBackgroundResource(R.drawable.bg_status_orange)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting status color", e)
            }
        }

        private fun configureButtons(status: String) {
            try {
                when (status.lowercase().trim()) {
                    "pending", "approved", "confirmed", "cancelled", "cancel" -> {
                        binding.btnLihatBukti.visibility = View.VISIBLE
                        binding.btnJadwalUlang.visibility = View.GONE
                        binding.btnBeriUlasan.visibility = View.GONE
                        binding.viewLine.visibility = View.VISIBLE
                        binding.layoutTombol.visibility = View.VISIBLE
                    }
                    "completed", "selesai" -> {
                        binding.btnLihatBukti.visibility = View.GONE
                        binding.btnJadwalUlang.visibility = View.VISIBLE
                        binding.btnBeriUlasan.visibility = View.VISIBLE
                        binding.viewLine.visibility = View.VISIBLE
                        binding.layoutTombol.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.viewLine.visibility = View.GONE
                        binding.layoutTombol.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error configuring buttons", e)
                // Default: hide all buttons
                binding.viewLine.visibility = View.GONE
                binding.layoutTombol.visibility = View.GONE
            }
        }

        private fun setupButtonListeners(data: Kursus) {
            try {
                val context = contextRef?.get() ?: return

                // Clear existing listeners to prevent memory leaks
                binding.btnLihatBukti.setOnClickListener(null)
                binding.btnJadwalUlang.setOnClickListener(null)
                binding.btnBeriUlasan.setOnClickListener(null)

                // Set new listeners
                binding.btnLihatBukti.setOnClickListener {
                    handleLihatBuktiClick(data, context)
                }

                binding.btnJadwalUlang.setOnClickListener {
                    handleJadwalUlangClick(data, context)
                }

                binding.btnBeriUlasan.setOnClickListener {
                    handleBeriUlasanClick(data)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up button listeners", e)
            }
        }

        private fun handleLihatBuktiClick(data: Kursus, context: Context) {
            try {
                showToast(context, "Lihat Bukti: ${data.courseName}")
                showPaymentProofOverlay(data.paymentProofPath)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling lihat bukti click", e)
                showToast(context, "Error menampilkan bukti pembayaran")
            }
        }

        private fun handleJadwalUlangClick(data: Kursus, context: Context) {
            try {
                showToast(context, "Jadwal Ulang: ${data.courseName}")
                // Add your reschedule logic here
            } catch (e: Exception) {
                Log.e(TAG, "Error handling jadwal ulang click", e)
                showToast(context, "Error mengatur jadwal ulang")
            }
        }

        private fun handleBeriUlasanClick(data: Kursus) {
            try {
                navigateToUlasan(data)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling beri ulasan click", e)
                val context = contextRef?.get()
                if (context != null) {
                    showToast(context, "Error membuka halaman ulasan")
                }
            }
        }

        private fun navigateToUlasan(data: Kursus) {
            try {
                // Validate required data
                if (data.courseId.isEmpty() || data.teacherId.isEmpty() || data.enrollmentId.isEmpty()) {
                    Log.w(TAG, "Missing required IDs for ulasan navigation")
                    val context = contextRef?.get()
                    if (context != null) {
                        showToast(context, "Data tidak lengkap untuk membuka ulasan")
                    }
                    return
                }

                // Try fragment navigation first
                if (fragment != null && fragment.isAdded && fragment.activity != null) {
                    navigateToUlasanFragment(data)
                } else {
                    // Fallback to activity navigation
                    navigateToUlasanActivity(data)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to ulasan", e)
                navigateToUlasanActivity(data)
            }
        }

        private fun navigateToUlasanFragment(data: Kursus) {
            try {
                val ulasanFragment = ulasan().apply {
                    arguments = Bundle().apply {
                        putString("courseId", data.courseId)
                        putString("teacherId", data.teacherId)
                        putString("enrollmentId", data.enrollmentId)
                    }
                }

                fragment?.parentFragmentManager
                    ?.beginTransaction()
                    ?.replace(R.id.frame_container, ulasanFragment)
                    ?.addToBackStack(null)
                    ?.commitAllowingStateLoss()

            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to ulasan fragment", e)
                navigateToUlasanActivity(data)
            }
        }

        private fun navigateToUlasanActivity(data: Kursus) {
            try {
                val context = contextRef?.get() ?: return

                val intent = Intent(context, UlasanActivity::class.java).apply {
                    putExtra("courseId", data.courseId)
                    putExtra("teacherId", data.teacherId)
                    putExtra("enrollmentId", data.enrollmentId)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to ulasan activity", e)
                val context = contextRef?.get()
                if (context != null) {
                    showToast(context, "Gagal membuka halaman ulasan")
                }
            }
        }
    }

    private fun showPaymentProofOverlay(paymentProofPath: String) {
        try {
            val context = contextRef?.get() ?: return
            val fragmentActivity = getFragmentActivity() ?: return

            if (paymentProofPath.isEmpty()) {
                showToast(context, "Bukti pembayaran tidak tersedia")
                return
            }

            // Check if file exists
            val file = File(paymentProofPath)
            if (!file.exists() || !file.canRead()) {
                showToast(context, "File bukti pembayaran tidak dapat diakses")
                return
            }

            // Remove existing overlay if any
            hidePaymentProofOverlay()

            val overlayLayout = LayoutInflater.from(context).inflate(R.layout.overlay_payment_proof, null)
            val imageView = overlayLayout.findViewById<ImageView>(R.id.imgPaymentProof)
            val btnClose = overlayLayout.findViewById<Button>(R.id.btnCloseOverlay)
            val progressBar = overlayLayout.findViewById<ProgressBar>(R.id.progressBarImage)

            // Show progress bar
            progressBar.visibility = View.VISIBLE

            val requestOptions = RequestOptions()
                .placeholder(R.drawable.ic_visibility)
                .error(R.drawable.ic_empty_courses)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .timeout(15000)

            Glide.with(context)
                .load(file)
                .apply(requestOptions)
                .into(imageView)

            // Hide progress bar after loading
            imageView.post {
                progressBar.visibility = View.GONE
            }

            // Set click listeners
            btnClose.setOnClickListener {
                hidePaymentProofOverlay()
            }

            overlayLayout.setOnClickListener {
                hidePaymentProofOverlay()
            }

            imageView.setOnClickListener {
                // Prevent overlay from closing when image is clicked
            }

            // Add overlay to parent view
            val parentView = fragmentActivity.findViewById<ViewGroup>(android.R.id.content)
            parentView.addView(overlayLayout)
            overlayView = overlayLayout

            // Animate fade in
            overlayLayout.alpha = 0f
            overlayLayout.animate()
                .alpha(1f)
                .setDuration(300)
                .start()

        } catch (e: Exception) {
            Log.e(TAG, "Error showing payment proof overlay", e)
            val context = contextRef?.get()
            if (context != null) {
                showToast(context, "Gagal menampilkan bukti pembayaran")
            }
        }
    }

    private fun hidePaymentProofOverlay() {
        try {
            val fragmentActivity = getFragmentActivity() ?: return

            overlayView?.let { overlay ->
                try {
                    overlay.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            try {
                                val parentView = fragmentActivity.findViewById<ViewGroup>(android.R.id.content)
                                parentView.removeView(overlay)
                                overlayView = null
                            } catch (e: Exception) {
                                Log.e(TAG, "Error removing overlay in animation end", e)
                            }
                        }
                        .start()
                } catch (e: Exception) {
                    Log.e(TAG, "Error animating overlay hide", e)
                    // Force remove overlay
                    forceRemoveOverlay(fragmentActivity)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding payment proof overlay", e)
        }
    }

    private fun forceRemoveOverlay(fragmentActivity: FragmentActivity) {
        try {
            overlayView?.let { overlay ->
                val parentView = fragmentActivity.findViewById<ViewGroup>(android.R.id.content)
                parentView.removeView(overlay)
                overlayView = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error force removing overlay", e)
        }
    }

    private fun getFragmentActivity(): FragmentActivity? {
        return try {
            when {
                fragment != null && fragment.isAdded -> fragment.requireActivity()
                else -> {
                    val context = contextRef?.get()
                    if (context is FragmentActivity) context else null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting fragment activity", e)
            null
        }
    }

    private fun showToast(context: Context, message: String) {
        try {
            if (context is FragmentActivity && !context.isFinishing) {
                context.runOnUiThread {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast", e)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return try {
            val binding = ItemKursusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating view holder", e)
            throw e
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            if (position >= 0 && position < list.size) {
                val item = list[position]
                if (item != null) {
                    holder.bind(item, position)
                } else {
                    Log.w(TAG, "Null item at position $position")
                }
            } else {
                Log.w(TAG, "Invalid position: $position, list size: ${list.size}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding view holder at position $position", e)
        }
    }

    override fun getItemCount(): Int {
        return try {
            list.size
        } catch (e: Exception) {
            Log.e(TAG, "Error getting item count", e)
            0
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        try {
            super.onDetachedFromRecyclerView(recyclerView)
            cleanup()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDetachedFromRecyclerView", e)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        try {
            super.onViewRecycled(holder)
            // Clear Glide requests to prevent memory leaks
            val context = holder.binding.root.context
            Glide.with(context).clear(holder.binding.imgGuru)
        } catch (e: Exception) {
            Log.e(TAG, "Error recycling view holder", e)
        }
    }

    private fun cleanup() {
        try {
            hidePaymentProofOverlay()
            contextRef?.clear()
            contextRef = null
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    // Public method to safely update data
    fun updateData(newList: List<Kursus>) {
        try {
            list.clear()
            list.addAll(newList)
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating data", e)
        }
    }
}