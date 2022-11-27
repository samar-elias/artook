package com.hudhudit.artook.views.main.notifications.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hudhudit.artook.apputils.modules.notification.Notification
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.notifications.NotificationsFragment
import de.hdodenhof.circleimageview.CircleImageView

class NotificationsAdapter(
    private var notificationsFragment: NotificationsFragment,
    private var notifications: ArrayList<Notification>
) :
    RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.notification_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val notification = notifications[position]

        if (notification.image_client.isNotEmpty()){
            Glide.with(context!!).load(notification.image_client).into(holder.userImg)
        }
        holder.userName.text = Html.fromHtml(notification.name)
        holder.notification.text = notification.title
        holder.notificationDate.text = notification.time
        if (notification.is_seen == "0"){
            holder.unRead.visibility = View.VISIBLE
        }else{
            holder.unRead.visibility = View.INVISIBLE
        }

        holder.delete.setOnClickListener { notificationsFragment.deleteNotification(notification.id) }
        holder.itemView.setOnClickListener {
            if (notification.is_seen == "0"){
                notificationsFragment.readNotification(notification)
            }else{
                notificationsFragment.openNotification(notification)
            }
        }
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userImg: CircleImageView = itemView.findViewById(R.id.user_img)
        var userName: TextView = itemView.findViewById(R.id.user_name)
        var notificationDate: TextView = itemView.findViewById(R.id.notification_date)
        var notification: TextView = itemView.findViewById(R.id.notification)
        var delete: ImageView = itemView.findViewById(R.id.delete_notification)
        var unRead: CircleImageView = itemView.findViewById(R.id.unread)
    }

}