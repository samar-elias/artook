package com.hudhudit.artook.views.main.comments.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hudhudit.artook.apputils.modules.post.PostMedia
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.comments.CommentsFragment

class PostMediaAdapter(
    private var commentsFragment: CommentsFragment,
    private var postMedia: ArrayList<PostMedia>
) :
    RecyclerView.Adapter<PostMediaAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.media_slider_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val media = postMedia[position]
        if (media.media.endsWith(".mp4")){
            commentsFragment.videoPosition = position
            holder.videoLayout.visibility = View.VISIBLE
            holder.image.visibility = View.GONE
            try {
                val link = media.media
//                val mediaController = MediaController(context)
//                mediaController.setAnchorView(holder.video)
                val video: Uri = Uri.parse(link)
//                holder.video.setMediaController(mediaController)
                holder.video.setVideoURI(video)
//                holder.video.start()
            } catch (e: Exception) {

            }
        }else{
            holder.videoLayout.visibility = View.GONE
            holder.image.visibility = View.VISIBLE
            Glide.with(context!!).load(media.media).into(holder.image)
        }
        holder.mediaCounter.text = (position+1).toString()+"/"+postMedia.size

//        holder.video.setOnClickListener { holder.video.start() }

        holder.play.setOnClickListener {
            if (holder.video.isPlaying){
                Glide.with(context!!).load(R.drawable.play).into(holder.playIcon)
                holder.video.pause()
            }else{
                Glide.with(context!!).load(R.drawable.pause).into(holder.playIcon)
                holder.video.start()
            }
        }

        holder.itemView.setOnClickListener {
            holder.video.pause()
            commentsFragment.openFullMedia(media.media)
        }
    }

    override fun getItemCount(): Int {
        return postMedia.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.image)
        var video: VideoView = itemView.findViewById(R.id.video)
        var mediaCounter: TextView = itemView.findViewById(R.id.media_counter)
        var playIcon: ImageView = itemView.findViewById(R.id.play_icon)
        var play: ConstraintLayout = itemView.findViewById(R.id.play_video)
        var videoLayout: ConstraintLayout = itemView.findViewById(R.id.video_layout)

    }

}