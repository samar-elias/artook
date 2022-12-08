package com.hudhudit.artook.views.main.newpost.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.modules.post.NewPostMedia
import com.hudhudit.artook.views.main.newpost.PostDetailsFragment
import de.hdodenhof.circleimageview.CircleImageView


class MediaAdapter(
    private var postDetailsFragment: PostDetailsFragment,
    private var mediaUris: ArrayList<NewPostMedia>
) :
    RecyclerView.Adapter<MediaAdapter.ViewHolder>() {
    var context: Context? = null
    var isSet = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.media_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val mediaUri = mediaUris[position]

        Glide.with(context!!).load(mediaUri.mediaUri).into(holder.mediaImg)

        if (mediaUri.mediaType == "0"){
            Glide.with(context!!).load(R.drawable.image_icon).into(holder.mediaType)
        }else if (mediaUri.mediaType == "1"){
            Glide.with(context!!).load(R.drawable.video_icon).into(holder.mediaType)
        }

        holder.delete.setOnClickListener {
            postDetailsFragment.deleteUri(mediaUri)
        }
    }

    override fun getItemCount(): Int {
        return mediaUris.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mediaImg: ImageView = itemView.findViewById(R.id.media)
        var delete: ImageView = itemView.findViewById(R.id.delete_media)
        var mediaType: ImageView = itemView.findViewById(R.id.media_type)
    }

}