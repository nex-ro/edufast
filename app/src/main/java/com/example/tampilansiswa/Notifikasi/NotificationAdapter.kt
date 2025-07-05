package com.example.tampilansiswa.Notifikasi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.R
import android.net.Uri
import java.io.File

class NotificationAdapter(
    private val notifications: List<NotificationModel>,
    private val onItemClick: (NotificationModel) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewNotification)
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewNotificationTitle)
        val textViewMessage: TextView = itemView.findViewById(R.id.textViewNotificationMessage)
        val textViewTime: TextView = itemView.findViewById(R.id.textViewNotificationTime)
        val viewUnreadIndicator: View = itemView.findViewById(R.id.viewUnreadIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    // Di NotificationAdapter.onBindViewHolder(), update icon logic
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        holder.textViewTitle.text = notification.judulPesan
        holder.textViewMessage.text = notification.message
        holder.textViewTime.text = notification.getFormattedTime()

        // Set icon based on notification type
        when (notification.type) {
            "enrollment" -> holder.imageView.setImageResource(R.drawable.ic_schools)
            "enrollment_confirmation" -> holder.imageView.setImageResource(R.drawable.ic_check_circle)
            "enrollment_accepted" -> holder.imageView.setImageResource(R.drawable.ic_check_circle)
            "enrollment_rejected" -> holder.imageView.setImageResource(R.drawable.ic_close)
            "enrollment_completed" -> holder.imageView.setImageResource(R.drawable.ic_check_circle)
            else -> holder.imageView.setImageResource(R.drawable.ic_notification)
        }

        // Show/hide unread indicator
        holder.viewUnreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE

        // Set background and text colors
        if (notification.isRead) {
            holder.itemView.setBackgroundColor(holder.itemView.context.getColor(android.R.color.white))
            holder.textViewMessage.setTextColor(holder.itemView.context.getColor(R.color.text_secondary))
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.context.getColor(R.color.unread_background))
            holder.textViewMessage.setTextColor(holder.itemView.context.getColor(android.R.color.black))
        }

        holder.textViewTitle.setTextColor(holder.itemView.context.getColor(android.R.color.black))

        holder.itemView.setOnClickListener {
            onItemClick(notification)
        }
    }
    override fun getItemCount(): Int = notifications.size
}