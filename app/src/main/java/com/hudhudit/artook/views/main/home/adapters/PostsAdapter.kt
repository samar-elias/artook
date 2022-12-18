package com.hudhudit.artook.views.main.home.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnScrollChangedListener
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.bumptech.glide.Glide
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.modules.post.Post
import com.hudhudit.artook.views.main.home.HomeFragment
import de.hdodenhof.circleimageview.CircleImageView


class PostsAdapter(
    private var homeFragment: HomeFragment,
    private var posts: ArrayList<Post>
) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {
    var context: Context? = null
    var isSet = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.post_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val post = posts[position]

        if (post.image_client.isNotEmpty()){
            Glide.with(context!!).load(post.image_client).into(holder.userImg)
        }
        holder.userName.text = post.name
        holder.postDate.text = post.time+" "+post.date
        holder.likesCount.text = post.number_posts_like
        holder.commentsCount.text = post.number_posts_comments
        holder.postCaption.text = post.description
        if (post.is_like == "1"){
            Glide.with(context!!).load(R.drawable.liked).into(holder.likeIcon)
        }else{
            Glide.with(context!!).load(R.drawable.like).into(holder.likeIcon)
        }

        if (post.is_save == "1"){
            Glide.with(context!!).load(R.drawable.selected_save_post).into(holder.save)
        }else{
            Glide.with(context!!).load(R.drawable.save_post).into(holder.save)
        }

        if (post.client_id == AppDefs.user.results!!.id){
            holder.options.visibility = View.VISIBLE
            holder.report.visibility = View.GONE
        }else{
            holder.options.visibility = View.GONE
            holder.report.visibility = View.VISIBLE
        }

        val adapter = PostMediaAdapter(homeFragment, post, position, post.posts_media)
        val mSnapHelper: SnapHelper = PagerSnapHelper()
        mSnapHelper.attachToRecyclerView(holder.mediaRV)
        holder.mediaRV.onFlingListener = null
        holder.mediaRV.adapter = adapter
        holder.mediaRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        holder.options.setOnClickListener {
            if (holder.optionsLayout.visibility == View.GONE){
                holder.optionsLayout.visibility = View.VISIBLE
            }else{
                holder.optionsLayout.visibility = View.GONE
            }
        }

        holder.like.setOnClickListener {
            if (post.is_like == "0"){
                homeFragment.likePost(post)
                post.is_like = "1"
                notifyDataSetChanged()
            }else{
                homeFragment.unlikePost(post)
                post.is_like = "0"
                notifyDataSetChanged()
            }
        }

        holder.save.setOnClickListener {
            if (post.is_save == "0"){
                homeFragment.savePost(post.id)
                post.is_save = "1"
                notifyDataSetChanged()
            }else{
                homeFragment.unSavePost(post.id)
                post.is_save = "0"
                notifyDataSetChanged()
            }
        }

        holder.report.setOnClickListener { homeFragment.reportPopUp(post.id) }

        holder.delete.setOnClickListener { homeFragment.deletePopUp(post.id) }

        holder.edit.setOnClickListener { homeFragment.updatePopUp(post) }

        holder.itemView.setOnClickListener { homeFragment.openPost(post, position) }

        holder.share.setOnClickListener { homeFragment.shareApp() }

        holder.userImg.setOnClickListener { homeFragment.navigateToMyUserProfile(post.client_id) }

        holder.userInfoLayout.setOnClickListener { homeFragment.navigateToMyUserProfile(post.client_id) }

        holder.mediaRV.viewTreeObserver
            .addOnScrollChangedListener {
                holder.counter.text = ((holder.mediaRV.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()+1).toString()+"/"+post.posts_media.size
            }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userImg: CircleImageView = itemView.findViewById(R.id.user_img)
        var userName: TextView = itemView.findViewById(R.id.user_name)
        var postDate: TextView = itemView.findViewById(R.id.post_date)
        var report: ImageView = itemView.findViewById(R.id.report)
        var delete: ImageView = itemView.findViewById(R.id.delete)
        var edit: ImageView = itemView.findViewById(R.id.edit)
        var options: ImageView = itemView.findViewById(R.id.options)
        var optionsLayout: LinearLayoutCompat = itemView.findViewById(R.id.options_layout)
        var share: ImageView = itemView.findViewById(R.id.share)
        var save: ImageView = itemView.findViewById(R.id.save)
        var mediaRV: RecyclerView = itemView.findViewById(R.id.media_RV)
        var like: LinearLayoutCompat = itemView.findViewById(R.id.like)
        var likeIcon: ImageView = itemView.findViewById(R.id.like_icon)
        var comment: LinearLayoutCompat = itemView.findViewById(R.id.comment)
        var userInfoLayout: LinearLayoutCompat = itemView.findViewById(R.id.user_info_layout)
        var likesCount: TextView = itemView.findViewById(R.id.likes_count)
        var commentsCount: TextView = itemView.findViewById(R.id.comment_count)
        var postCaption: TextView = itemView.findViewById(R.id.post_caption)
        var counter: TextView = itemView.findViewById(R.id.media_counter)
    }

}