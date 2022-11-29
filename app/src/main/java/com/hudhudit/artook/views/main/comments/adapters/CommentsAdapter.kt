package com.hudhudit.artook.views.main.comments.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.modules.post.Comment
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.comments.CommentsFragment
import de.hdodenhof.circleimageview.CircleImageView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class CommentsAdapter(
    private var commentsFragment: CommentsFragment,
    private var comments: ArrayList<Comment>
) :
    RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.comment_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val comment = comments[position]

        holder.date.text = comment.convertedTime

        if (comment.image_client.isNotEmpty()){
            Glide.with(context!!).load(comment.image_client).into(holder.userImg)
        }
        holder.userName.text = comment.name
        holder.comment.text = comment.title


        holder.itemView.setOnClickListener {
            if (comment.client_id == AppDefs.user.results!!.id){
                commentsFragment.navigateToMyProfile()
            }else{
                commentsFragment.navigateToMyUserProfile(comment.client_id)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (comment.client_id == AppDefs.user.results!!.id || commentsFragment.isMyPost){
                commentsFragment.deleteCommentPopUp(comment.id)
            }
            return@setOnLongClickListener true
        }
//
//        holder.date.text = comment.convertedTime
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userImg: CircleImageView = itemView.findViewById(R.id.user_img)
        var userName: TextView = itemView.findViewById(R.id.user_name)
        var comment: TextView = itemView.findViewById(R.id.comment)
        var date: TextView = itemView.findViewById(R.id.comment_date)

    }

}