package com.hudhudit.artook.views.main.profile.followers.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hudhudit.artook.apputils.modules.profile.Following
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.profile.following.FollowingFragment
import de.hdodenhof.circleimageview.CircleImageView

class FollowingsAdapter(
    private var followingsFragment: FollowingFragment,
    private var followings: ArrayList<Following>
) :
    RecyclerView.Adapter<FollowingsAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.profile_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val following = followings[position]

        holder.userName.text = following.name
        if (following.image.isNotEmpty()){
            Glide.with(context!!).load(following.image).into(holder.userImg)
        }

        holder.itemView.setOnClickListener { followingsFragment.navigateToMyUserProfile(following.id) }
    }

    override fun getItemCount(): Int {
        return followings.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userImg: CircleImageView = itemView.findViewById(R.id.user_img)
        var userName: TextView = itemView.findViewById(R.id.user_name)
    }

}