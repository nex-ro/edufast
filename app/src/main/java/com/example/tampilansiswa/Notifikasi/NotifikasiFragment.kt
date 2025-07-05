package com.example.tampilansiswa.Notifikasi

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.GuruPage.kursus.guru_kursus
import com.example.tampilansiswa.GuruPage.siswa.guru_siswa
import com.example.tampilansiswa.Kursus.KursusSayaFragment
import com.example.tampilansiswa.R
import com.example.tampilansiswa.databinding.FragmentNotifikasiBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class NotifikasiFragment : Fragment() {

    private lateinit var binding: FragmentNotifikasiBinding
    private lateinit var notificationAdapter: NotificationAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notifications = mutableListOf<NotificationModel>()

    companion object {
        private const val TAG = "NotifikasiFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotifikasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(notifications) { notification ->
            // Handle notification click
            onNotificationClick(notification)
        }

        binding.recyclerViewNotifications.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }
    }

    private fun loadNotifications() {
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("uid", "") ?: ""
        val role = sharedPref.getString("role", "") ?: ""

        if (userId.isEmpty()) {
            Log.e(TAG, "User ID is empty")
            showEmptyState()
            return
        }

        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewNotifications.visibility = View.GONE
        binding.textViewEmpty.visibility = View.GONE

        Log.d(TAG, "Loading notifications for user: $userId")

        // Query notifications where toUid equals current user's uid
        db.collection("notifications")
            .whereEqualTo("toUid", userId)
            .get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Query successful. Documents found: ${documents.size()}")

                notifications.clear()
                for (document in documents) {
                    try {
                        val notification = document.toObject(NotificationModel::class.java)
                        // Set document ID jika belum ada
                        if (notification.id.isEmpty()) {
                            notification.id = document.id
                        }
                        notifications.add(notification)
                        Log.d(TAG, "Added notification: ${notification.judulPesan}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing notification: ${e.message}")
                    }
                }

                binding.progressBar.visibility = View.GONE
                if (notifications.isEmpty()) {
                    Log.d(TAG, "No notifications found for user")
                    showEmptyState()
                } else {
                    Log.d(TAG, "Displaying ${notifications.size} notifications")
                    showNotifications()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error loading notifications: ${exception.message}")
                binding.progressBar.visibility = View.GONE
                showEmptyState()
                Toast.makeText(context, "Gagal memuat notifikasi: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEmptyState() {
        binding.textViewEmpty.visibility = View.VISIBLE
        binding.recyclerViewNotifications.visibility = View.GONE
    }

    private fun showNotifications() {
        binding.textViewEmpty.visibility = View.GONE
        binding.recyclerViewNotifications.visibility = View.VISIBLE
        notificationAdapter.notifyDataSetChanged()
    }

    private fun onNotificationClick(notification: NotificationModel) {
        Log.d(TAG, "Notification clicked - ID: ${notification.id}, isRead: ${notification.isRead}")
        if (!notification.isRead) {
            markAsRead(notification.id)
        }
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("uid", "") ?: ""
        val role = sharedPref.getString("role", "") ?: ""

        if (role == "guru") {
            navigateToFragment(guru_siswa())
        } else {
            navigateToFragment(KursusSayaFragment())
        }


        when (notification.type) {
            "enrollment" -> {
                Toast.makeText(context, "Membuka detail pesanan...", Toast.LENGTH_SHORT).show()
            }
            "enrollment_confirmation" -> {
                Toast.makeText(context, "Membuka detail konfirmasi...", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(context, notification.message, Toast.LENGTH_LONG).show()
            }
        }
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
            Log.e("HomeFragment", "Error navigating to fragment: ${e.message}", e)
        }
    }
    private fun markAsRead(notificationId: String) {
        if (notificationId.isEmpty()) {
            Log.e(TAG, "Cannot mark as read: notification ID is empty")
            return
        }

        Log.d(TAG, "Marking notification as read: $notificationId")

        db.collection("notifications")
            .document(notificationId)
            .update("isRead", true)
            .addOnSuccessListener {
                // Update local data
                notifications.find { it.id == notificationId }?.let { notification ->
                    notification.isRead = true
                    // Find position and update specific item
                    val position = notifications.indexOf(notification)
                    if (position != -1) {
                        // PERBAIKI: Pastikan adapter diupdate dengan benar
                        requireActivity().runOnUiThread {
                            notificationAdapter.notifyItemChanged(position)
                        }
                    }
                }
                Log.d(TAG, "Notification marked as read: $notificationId")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error marking notification as read: ${exception.message}")
                Toast.makeText(context, "Gagal menandai notifikasi sebagai dibaca", Toast.LENGTH_SHORT).show()
            }
    }
    // Fungsi untuk debugging - bisa dihapus di production
    private fun debugUserSession() {
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val allPrefs = sharedPref.all

        Log.d(TAG, "=== Debug User Session ===")
        for ((key, value) in allPrefs) {
            Log.d(TAG, "$key: $value")
        }

        Log.d(TAG, "Firebase Auth User: ${auth.currentUser?.uid}")
        Log.d(TAG, "========================")
    }

    override fun onResume() {
        super.onResume()
        // Debug session saat fragment muncul
        debugUserSession()
        // Refresh notifications when fragment becomes visible
        loadNotifications()
    }
}

// Data class untuk model notifikasi - Updated
data class NotificationModel(
    var id: String = "",
    val fromUid: String = "",
    val toUid: String = "",         // Penerima notifikasi
    val enrollId: String = "",
    val judulPesan: String = "",
    val message: String = "",
    val createdAt: com.google.firebase.Timestamp? = null,
    var isRead: Boolean = true,
    val type: String = ""
) {
    fun getFormattedTime(): String {
        return createdAt?.let { timestamp ->
            val date = timestamp.toDate()
            val now = Date()
            val diff = now.time - date.time

            when {
                diff < 60000 -> "Baru saja" // less than 1 minute
                diff < 3600000 -> "${diff / 60000} menit yang lalu" // less than 1 hour
                diff < 86400000 -> "${diff / 3600000} jam yang lalu" // less than 1 day
                diff < 604800000 -> "${diff / 86400000} hari yang lalu" // less than 1 week
                else -> {
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    sdf.format(date)
                }
            }
        } ?: "Waktu tidak diketahui"
    }
}