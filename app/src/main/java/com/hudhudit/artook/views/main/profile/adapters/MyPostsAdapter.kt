package com.hudhudit.artook.views.main.profile.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hudhudit.artook.apputils.modules.post.Post
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.profile.ProfileFragment

class MyPostsAdapter(
    private var profileFragment: ProfileFragment,
    private var posts: ArrayList<Post>
) :
    RecyclerView.Adapter<MyPostsAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.gallery_image_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val post = posts[position]

        if (post.posts_media.size > 0){
            Glide.with(context!!).load(post.posts_media[0].media).into(holder.image)
        }

        holder.itemView.setOnClickListener { profileFragment.openPost(post) }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.img)
    }

}