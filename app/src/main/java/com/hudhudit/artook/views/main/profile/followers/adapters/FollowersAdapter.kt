package com.hudhudit.artook.views.main.profile.followers.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hudhudit.artook.apputils.modules.profile.Follower
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.profile.followers.FollowersFragment
import de.hdodenhof.circleimageview.CircleImageView

class FollowersAdapter(
    private var followersFragment: FollowersFragment,
    private var followers: ArrayList<Follower>
) :
    RecyclerView.Adapter<FollowersAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.profile_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val follower = followers[position]

        holder.userName.text = follower.name
        if (follower.image.isNotEmpty()){
            Glide.with(context!!).load(follower.image).into(holder.userImg)
        }

        holder.itemView.setOnClickListener { followersFragment.navigateToMyUserProfile(follower.id) }
    }

    override fun getItemCount(): Int {
        return followers.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userImg: CircleImageView = itemView.findViewById(R.id.user_img)
        var userName: TextView = itemView.findViewById(R.id.user_name)
    }

}