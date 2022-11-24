package com.hudhudit.artook.views.main.videosarticles.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.hudhudit.artook.apputils.modules.videosarticles.Video
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.modules.videosarticles.VideosGroup
import com.hudhudit.artook.views.main.videosarticles.VideosArticlesFragment

class VideosAdapter(
    private var videosArticlesFragment: VideosArticlesFragment,
    private var videos: ArrayList<VideosGroup>
) :
    RecyclerView.Adapter<VideosAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.video_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val videoGroup = videos[position]
        if (videoGroup.videos.size <= 5){
            holder.layout2.visibility = View.GONE
            when (videoGroup.videos.size){
                1->{
                    holder.imgCV1.visibility = View.VISIBLE
                    holder.imgCV2.visibility = View.GONE
                    holder.imgCV3.visibility = View.GONE
                    holder.imgCV4.visibility = View.GONE
                    holder.imgCV5.visibility = View.GONE
                    holder.title1.visibility = View.VISIBLE
                    holder.title2.visibility = View.GONE
                    holder.title3.visibility = View.GONE
                    holder.title4.visibility = View.GONE
                    holder.title5.visibility = View.GONE
                    holder.title1.text = videoGroup.videos[0].title
                    Glide.with(context!!).load(videoGroup.videos[0].video).into(holder.img)
                    holder.imgCV1.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[0]) }
                }
                2->{
                    holder.imgCV1.visibility = View.VISIBLE
                    holder.imgCV2.visibility = View.VISIBLE
                    holder.imgCV3.visibility = View.GONE
                    holder.imgCV4.visibility = View.GONE
                    holder.imgCV5.visibility = View.GONE
                    holder.title1.visibility = View.VISIBLE
                    holder.title2.visibility = View.VISIBLE
                    holder.title3.visibility = View.GONE
                    holder.title4.visibility = View.GONE
                    holder.title5.visibility = View.GONE
                    holder.title1.text = videoGroup.videos[0].title
                    holder.title2.text = videoGroup.videos[1].title
                    Glide.with(context!!).load(videoGroup.videos[0].video).into(holder.img)
                    holder.imgCV1.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[0]) }
                    Glide.with(context!!).load(videoGroup.videos[1].video).into(holder.img2)
                    holder.imgCV2.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[1]) }
                }
                3->{
                    holder.imgCV1.visibility = View.VISIBLE
                    holder.imgCV2.visibility = View.VISIBLE
                    holder.imgCV3.visibility = View.VISIBLE
                    holder.imgCV4.visibility = View.GONE
                    holder.imgCV5.visibility = View.GONE
                    holder.title1.visibility = View.VISIBLE
                    holder.title2.visibility = View.VISIBLE
                    holder.title3.visibility = View.VISIBLE
                    holder.title4.visibility = View.GONE
                    holder.title5.visibility = View.GONE
                    holder.title1.text = videoGroup.videos[0].title
                    holder.title2.text = videoGroup.videos[1].title
                    holder.title3.text = videoGroup.videos[2].title
                    Glide.with(context!!).load(videoGroup.videos[0].video).into(holder.img)
                    holder.imgCV1.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[0]) }
                    Glide.with(context!!).load(videoGroup.videos[1].video).into(holder.img2)
                    holder.imgCV2.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[1]) }
                    Glide.with(context!!).load(videoGroup.videos[2].video).into(holder.img3)
                    holder.imgCV3.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[2]) }
                }
                4->{
                    holder.imgCV1.visibility = View.VISIBLE
                    holder.imgCV2.visibility = View.VISIBLE
                    holder.imgCV3.visibility = View.VISIBLE
                    holder.imgCV4.visibility = View.VISIBLE
                    holder.imgCV5.visibility = View.GONE
                    holder.title1.visibility = View.VISIBLE
                    holder.title2.visibility = View.VISIBLE
                    holder.title3.visibility = View.VISIBLE
                    holder.title4.visibility = View.VISIBLE
                    holder.title5.visibility = View.GONE
                    holder.title1.text = videoGroup.videos[0].title
                    holder.title2.text = videoGroup.videos[1].title
                    holder.title3.text = videoGroup.videos[2].title
                    holder.title4.text = videoGroup.videos[3].title
                    Glide.with(context!!).load(videoGroup.videos[0].video).into(holder.img)
                    holder.imgCV1.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[0]) }
                    Glide.with(context!!).load(videoGroup.videos[1].video).into(holder.img2)
                    holder.imgCV2.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[1]) }
                    Glide.with(context!!).load(videoGroup.videos[2].video).into(holder.img3)
                    holder.imgCV3.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[2]) }
                    Glide.with(context!!).load(videoGroup.videos[3].video).into(holder.img4)
                    holder.imgCV4.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[3]) }
                }
                5->{
                    holder.imgCV1.visibility = View.VISIBLE
                    holder.imgCV2.visibility = View.VISIBLE
                    holder.imgCV3.visibility = View.VISIBLE
                    holder.imgCV4.visibility = View.VISIBLE
                    holder.imgCV5.visibility = View.VISIBLE
                    holder.title1.visibility = View.VISIBLE
                    holder.title2.visibility = View.VISIBLE
                    holder.title3.visibility = View.VISIBLE
                    holder.title4.visibility = View.VISIBLE
                    holder.title5.visibility = View.VISIBLE
                    holder.title1.text = videoGroup.videos[0].title
                    holder.title2.text = videoGroup.videos[1].title
                    holder.title3.text = videoGroup.videos[2].title
                    holder.title4.text = videoGroup.videos[3].title
                    holder.title5.text = videoGroup.videos[4].title
                    Glide.with(context!!).load(videoGroup.videos[0].video).into(holder.img)
                    holder.imgCV1.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[0]) }
                    Glide.with(context!!).load(videoGroup.videos[1].video).into(holder.img2)
                    holder.imgCV2.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[1]) }
                    Glide.with(context!!).load(videoGroup.videos[2].video).into(holder.img3)
                    holder.imgCV3.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[2]) }
                    Glide.with(context!!).load(videoGroup.videos[3].video).into(holder.img4)
                    holder.imgCV4.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[3]) }
                    Glide.with(context!!).load(videoGroup.videos[4].video).into(holder.img5)
                    holder.imgCV5.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[4]) }
                }
            }
        }else{
            holder.layout2.visibility = View.VISIBLE
            when (videoGroup.videos.size){
                1->{
                    holder.imgCV1.visibility = View.VISIBLE
                    holder.imgCV2.visibility = View.GONE
                    holder.imgCV3.visibility = View.GONE
                    holder.imgCV4.visibility = View.GONE
                    holder.imgCV5.visibility = View.GONE
                    holder.title1.visibility = View.VISIBLE
                    holder.title2.visibility = View.GONE
                    holder.title3.visibility = View.GONE
                    holder.title4.visibility = View.GONE
                    holder.title5.visibility = View.GONE
                    holder.title1.text = videoGroup.videos[0].title
                    Glide.with(context!!).load(videoGroup.videos[0].video).into(holder.img)
                    holder.imgCV1.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[0]) }
                }
                2->{
                    holder.imgCV1.visibility = View.VISIBLE
                    holder.imgCV2.visibility = View.VISIBLE
                    holder.imgCV3.visibility = View.GONE
                    holder.imgCV4.visibility = View.GONE
                    holder.imgCV5.visibility = View.GONE
                    holder.title1.visibility = View.VISIBLE
                    holder.title2.visibility = View.VISIBLE
                    holder.title3.visibility = View.GONE
                    holder.title4.visibility = View.GONE
                    holder.title5.visibility = View.GONE
                    holder.title1.text = videoGroup.videos[0].title
                    holder.title2.text = videoGroup.videos[1].title
                    Glide.with(context!!).load(videoGroup.videos[0].video).into(holder.img)
                    holder.imgCV1.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[0]) }
                    Glide.with(context!!).load(videoGroup.videos[1].video).into(holder.img2)
                    holder.imgCV2.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[1]) }
                }
                3->{
                    holder.imgCV1.visibility = View.VISIBLE
                    holder.imgCV2.visibility = View.VISIBLE
                    holder.imgCV3.visibility = View.VISIBLE
                    holder.imgCV4.visibility = View.GONE
                    holder.imgCV5.visibility = View.GONE
                    holder.title1.visibility = View.VISIBLE
                    holder.title2.visibility = View.VISIBLE
                    holder.title3.visibility = View.VISIBLE
                    holder.title4.visibility = View.GONE
                    holder.title5.visibility = View.GONE
                    holder.title1.text = videoGroup.videos[0].title
                    holder.title2.text = videoGroup.videos[1].title
                    holder.title3.text = videoGroup.videos[2].title
                    Glide.with(context!!).load(videoGroup.videos[0].video).into(holder.img)
                    holder.imgCV1.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[0]) }
                    Glide.with(context!!).load(videoGroup.videos[1].video).into(holder.img2)
                    holder.imgCV2.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[1]) }
                    Glide.with(context!!).load(videoGroup.videos[2].video).into(holder.img3)
                    holder.imgCV3.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[2]) }
                }
                4->{
                    holder.imgCV1.visibility = View.VISIBLE
                    holder.imgCV2.visibility = View.VISIBLE
                    holder.imgCV3.visibility = View.VISIBLE
                    holder.imgCV4.visibility = View.VISIBLE
                    holder.imgCV5.visibility = View.GONE
                    holder.title1.visibility = View.VISIBLE
                    holder.title2.visibility = View.VISIBLE
                    holder.title3.visibility = View.VISIBLE
                    holder.title4.visibility = View.VISIBLE
                    holder.title5.visibility = View.GONE
                    holder.title1.text = videoGroup.videos[0].title
                    holder.title2.text = videoGroup.videos[1].title
                    holder.title3.text = videoGroup.videos[2].title
                    holder.title4.text = videoGroup.videos[3].title
                    Glide.with(context!!).load(videoGroup.videos[0].video).into(holder.img)
                    holder.imgCV1.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[0]) }
                    Glide.with(context!!).load(videoGroup.videos[1].video).into(holder.img2)
                    holder.imgCV2.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[1]) }
                    Glide.with(context!!).load(videoGroup.videos[2].video).into(holder.img3)
                    holder.imgCV3.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[2]) }
                    Glide.with(context!!).load(videoGroup.videos[3].video).into(holder.img4)
                    holder.imgCV4.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[3]) }
                }
                5->{
                    holder.imgCV1.visibility = View.VISIBLE
                    holder.imgCV2.visibility = View.VISIBLE
                    holder.imgCV3.visibility = View.VISIBLE
                    holder.imgCV4.visibility = View.VISIBLE
                    holder.imgCV5.visibility = View.VISIBLE
                    holder.title1.visibility = View.VISIBLE
                    holder.title2.visibility = View.VISIBLE
                    holder.title3.visibility = View.VISIBLE
                    holder.title4.visibility = View.VISIBLE
                    holder.title5.visibility = View.VISIBLE
                    holder.title1.text = videoGroup.videos[0].title
                    holder.title2.text = videoGroup.videos[1].title
                    holder.title3.text = videoGroup.videos[2].title
                    holder.title4.text = videoGroup.videos[3].title
                    holder.title5.text = videoGroup.videos[4].title
                    Glide.with(context!!).load(videoGroup.videos[0].video).into(holder.img)
                    holder.imgCV1.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[0]) }
                    Glide.with(context!!).load(videoGroup.videos[1].video).into(holder.img2)
                    holder.imgCV2.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[1]) }
                    Glide.with(context!!).load(videoGroup.videos[2].video).into(holder.img3)
                    holder.imgCV3.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[2]) }
                    Glide.with(context!!).load(videoGroup.videos[3].video).into(holder.img4)
                    holder.imgCV4.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[3]) }
                    Glide.with(context!!).load(videoGroup.videos[4].video).into(holder.img5)
                    holder.imgCV5.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[4]) }
                }
                6->{
                    holder.imgCV1.visibility = View.VISIBLE
                    holder.imgCV2.visibility = View.VISIBLE
                    holder.imgCV3.visibility = View.VISIBLE
                    holder.imgCV4.visibility = View.VISIBLE
                    holder.imgCV5.visibility = View.VISIBLE
                    holder.title1.visibility = View.VISIBLE
                    holder.title2.visibility = View.VISIBLE
                    holder.title3.visibility = View.VISIBLE
                    holder.title4.visibility = View.VISIBLE
                    holder.title5.visibility = View.VISIBLE
                    holder.title1.text = videoGroup.videos[0].title
                    holder.title2.text = videoGroup.videos[1].title
                    holder.title3.text = videoGroup.videos[2].title
                    holder.title4.text = videoGroup.videos[3].title
                    holder.title5.text = videoGroup.videos[4].title
                    Glide.with(context!!).load(videoGroup.videos[0].video).into(holder.img)
                    holder.imgCV1.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[0]) }
                    Glide.with(context!!).load(videoGroup.videos[1].video).into(holder.img2)
                    holder.imgCV2.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[1]) }
                    Glide.with(context!!).load(videoGroup.videos[2].video).into(holder.img3)
                    holder.imgCV3.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[2]) }
                    Glide.with(context!!).load(videoGroup.videos[3].video).into(holder.img4)
                    holder.imgCV4.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[3]) }
                    Glide.with(context!!).load(videoGroup.videos[4].video).into(holder.img5)
                    holder.imgCV5.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[4]) }
                    holder.imgCV6.visibility = View.VISIBLE
                    holder.imgCV7.visibility = View.GONE
                    holder.imgCV8.visibility = View.GONE
                    holder.imgCV9.visibility = View.GONE
                    holder.imgCV10.visibility = View.GONE
                    holder.title6.visibility = View.VISIBLE
                    holder.title7.visibility = View.GONE
                    holder.title8.visibility = View.GONE
                    holder.title9.visibility = View.GONE
                    holder.title10.visibility = View.GONE
                    holder.title6.text = videoGroup.videos[5].title
                    Glide.with(context!!).load(videoGroup.videos[5].video).into(holder.img6)
                    holder.imgCV6.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[5]) }
                }
                7->{
                    holder.imgCV1.visibility = View.VISIBLE
                    holder.imgCV2.visibility = View.VISIBLE
                    holder.imgCV3.visibility = View.VISIBLE
                    holder.imgCV4.visibility = View.VISIBLE
                    holder.imgCV5.visibility = View.VISIBLE
                    holder.title1.visibility = View.VISIBLE
                    holder.title2.visibility = View.VISIBLE
                    holder.title3.visibility = View.VISIBLE
                    holder.title4.visibility = View.VISIBLE
                    holder.title5.visibility = View.VISIBLE
                    holder.title1.text = videoGroup.videos[0].title
                    holder.title2.text = videoGroup.videos[1].title
                    holder.title3.text = videoGroup.videos[2].title
                    holder.title4.text = videoGroup.videos[3].title
                    holder.title5.text = videoGroup.videos[4].title
                    Glide.with(context!!).load(videoGroup.videos[0].video).into(holder.img)
                    holder.imgCV1.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[0]) }
                    Glide.with(context!!).load(videoGroup.videos[1].video).into(holder.img2)
                    holder.imgCV2.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[1]) }
                    Glide.with(context!!).load(videoGroup.videos[2].video).into(holder.img3)
                    holder.imgCV3.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[2]) }
                    Glide.with(context!!).load(videoGroup.videos[3].video).into(holder.img4)
                    holder.imgCV4.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[3]) }
                    Glide.with(context!!).load(videoGroup.videos[4].video).into(holder.img5)
                    holder.imgCV5.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[4]) }
                    holder.imgCV6.visibility = View.VISIBLE
                    holder.imgCV7.visibility = View.VISIBLE
                    holder.imgCV8.visibility = View.GONE
                    holder.imgCV9.visibility = View.GONE
                    holder.imgCV10.visibility = View.GONE
                    holder.title6.visibility = View.VISIBLE
                    holder.title7.visibility = View.VISIBLE
                    holder.title8.visibility = View.GONE
                    holder.title9.visibility = View.GONE
                    holder.title10.visibility = View.GONE
                    holder.title6.text = videoGroup.videos[5].title
                    holder.title7.text = videoGroup.videos[6].title
                    Glide.with(context!!).load(videoGroup.videos[5].video).into(holder.img6)
                    holder.imgCV6.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[5]) }
                    Glide.with(context!!).load(videoGroup.videos[6].video).into(holder.img7)
                    holder.imgCV7.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[6]) }
                }
                8->{
                    holder.imgCV1.visibility = View.VISIBLE
                    holder.imgCV2.visibility = View.VISIBLE
                    holder.imgCV3.visibility = View.VISIBLE
                    holder.imgCV4.visibility = View.VISIBLE
                    holder.imgCV5.visibility = View.VISIBLE
                    holder.title1.visibility = View.VISIBLE
                    holder.title2.visibility = View.VISIBLE
                    holder.title3.visibility = View.VISIBLE
                    holder.title4.visibility = View.VISIBLE
                    holder.title5.visibility = View.VISIBLE
                    holder.title1.text = videoGroup.videos[0].title
                    holder.title2.text = videoGroup.videos[1].title
                    holder.title3.text = videoGroup.videos[2].title
                    holder.title4.text = videoGroup.videos[3].title
                    holder.title5.text = videoGroup.videos[4].title
                    Glide.with(context!!).load(videoGroup.videos[0].video).into(holder.img)
                    holder.imgCV1.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[0]) }
                    Glide.with(context!!).load(videoGroup.videos[1].video).into(holder.img2)
                    holder.imgCV2.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[1]) }
                    Glide.with(context!!).load(videoGroup.videos[2].video).into(holder.img3)
                    holder.imgCV3.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[2]) }
                    Glide.with(context!!).load(videoGroup.videos[3].video).into(holder.img4)
                    holder.imgCV4.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[3]) }
                    Glide.with(context!!).load(videoGroup.videos[4].video).into(holder.img5)
                    holder.imgCV5.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[4]) }
                    holder.imgCV6.visibility = View.VISIBLE
                    holder.imgCV7.visibility = View.VISIBLE
                    holder.imgCV8.visibility = View.VISIBLE
                    holder.imgCV9.visibility = View.GONE
                    holder.imgCV10.visibility = View.GONE
                    holder.title6.visibility = View.VISIBLE
                    holder.title7.visibility = View.VISIBLE
                    holder.title8.visibility = View.VISIBLE
                    holder.title9.visibility = View.GONE
                    holder.title10.visibility = View.GONE
                    holder.title6.text = videoGroup.videos[5].title
                    holder.title7.text = videoGroup.videos[6].title
                    holder.title8.text = videoGroup.videos[7].title
                    Glide.with(context!!).load(videoGroup.videos[5].video).into(holder.img6)
                    holder.imgCV6.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[5]) }
                    Glide.with(context!!).load(videoGroup.videos[6].video).into(holder.img7)
                    holder.imgCV7.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[6]) }
                    Glide.with(context!!).load(videoGroup.videos[7].video).into(holder.img8)
                    holder.imgCV8.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[7]) }
                }
                9->{
                    holder.imgCV1.visibility = View.VISIBLE
                    holder.imgCV2.visibility = View.VISIBLE
                    holder.imgCV3.visibility = View.VISIBLE
                    holder.imgCV4.visibility = View.VISIBLE
                    holder.imgCV5.visibility = View.VISIBLE
                    holder.title1.visibility = View.VISIBLE
                    holder.title2.visibility = View.VISIBLE
                    holder.title3.visibility = View.VISIBLE
                    holder.title4.visibility = View.VISIBLE
                    holder.title5.visibility = View.VISIBLE
                    holder.title1.text = videoGroup.videos[0].title
                    holder.title2.text = videoGroup.videos[1].title
                    holder.title3.text = videoGroup.videos[2].title
                    holder.title4.text = videoGroup.videos[3].title
                    holder.title5.text = videoGroup.videos[4].title
                    Glide.with(context!!).load(videoGroup.videos[0].video).into(holder.img)
                    holder.imgCV1.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[0]) }
                    Glide.with(context!!).load(videoGroup.videos[1].video).into(holder.img2)
                    holder.imgCV2.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[1]) }
                    Glide.with(context!!).load(videoGroup.videos[2].video).into(holder.img3)
                    holder.imgCV3.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[2]) }
                    Glide.with(context!!).load(videoGroup.videos[3].video).into(holder.img4)
                    holder.imgCV4.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[3]) }
                    Glide.with(context!!).load(videoGroup.videos[4].video).into(holder.img5)
                    holder.imgCV5.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[4]) }
                    holder.imgCV6.visibility = View.VISIBLE
                    holder.imgCV7.visibility = View.VISIBLE
                    holder.imgCV8.visibility = View.VISIBLE
                    holder.imgCV9.visibility = View.VISIBLE
                    holder.imgCV10.visibility = View.GONE
                    holder.title6.visibility = View.VISIBLE
                    holder.title7.visibility = View.VISIBLE
                    holder.title8.visibility = View.VISIBLE
                    holder.title9.visibility = View.VISIBLE
                    holder.title10.visibility = View.GONE
                    holder.title6.text = videoGroup.videos[5].title
                    holder.title7.text = videoGroup.videos[6].title
                    holder.title8.text = videoGroup.videos[7].title
                    holder.title9.text = videoGroup.videos[8].title
                    Glide.with(context!!).load(videoGroup.videos[5].video).into(holder.img6)
                    holder.imgCV6.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[5]) }
                    Glide.with(context!!).load(videoGroup.videos[6].video).into(holder.img7)
                    holder.imgCV7.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[6]) }
                    Glide.with(context!!).load(videoGroup.videos[7].video).into(holder.img8)
                    holder.imgCV8.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[7]) }
                    Glide.with(context!!).load(videoGroup.videos[8].video).into(holder.img9)
                    holder.imgCV9.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[8]) }
                }
                10->{
                    holder.imgCV1.visibility = View.VISIBLE
                    holder.imgCV2.visibility = View.VISIBLE
                    holder.imgCV3.visibility = View.VISIBLE
                    holder.imgCV4.visibility = View.VISIBLE
                    holder.imgCV5.visibility = View.VISIBLE
                    holder.title1.visibility = View.VISIBLE
                    holder.title2.visibility = View.VISIBLE
                    holder.title3.visibility = View.VISIBLE
                    holder.title4.visibility = View.VISIBLE
                    holder.title5.visibility = View.VISIBLE
                    holder.title1.text = videoGroup.videos[0].title
                    holder.title2.text = videoGroup.videos[1].title
                    holder.title3.text = videoGroup.videos[2].title
                    holder.title4.text = videoGroup.videos[3].title
                    holder.title5.text = videoGroup.videos[4].title
                    Glide.with(context!!).load(videoGroup.videos[0].video).into(holder.img)
                    holder.imgCV1.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[0]) }
                    Glide.with(context!!).load(videoGroup.videos[1].video).into(holder.img2)
                    holder.imgCV2.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[1]) }
                    Glide.with(context!!).load(videoGroup.videos[2].video).into(holder.img3)
                    holder.imgCV3.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[2]) }
                    Glide.with(context!!).load(videoGroup.videos[3].video).into(holder.img4)
                    holder.imgCV4.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[3]) }
                    Glide.with(context!!).load(videoGroup.videos[4].video).into(holder.img5)
                    holder.imgCV5.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[4]) }
                    holder.imgCV6.visibility = View.VISIBLE
                    holder.imgCV7.visibility = View.VISIBLE
                    holder.imgCV8.visibility = View.VISIBLE
                    holder.imgCV9.visibility = View.VISIBLE
                    holder.imgCV10.visibility = View.VISIBLE
                    holder.title6.visibility = View.VISIBLE
                    holder.title7.visibility = View.VISIBLE
                    holder.title8.visibility = View.VISIBLE
                    holder.title9.visibility = View.VISIBLE
                    holder.title10.visibility = View.VISIBLE
                    holder.title6.text = videoGroup.videos[5].title
                    holder.title7.text = videoGroup.videos[6].title
                    holder.title8.text = videoGroup.videos[7].title
                    holder.title9.text = videoGroup.videos[8].title
                    holder.title10.text = videoGroup.videos[9].title
                    Glide.with(context!!).load(videoGroup.videos[5].video).into(holder.img6)
                    holder.imgCV6.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[5]) }
                    Glide.with(context!!).load(videoGroup.videos[6].video).into(holder.img7)
                    holder.imgCV7.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[6]) }
                    Glide.with(context!!).load(videoGroup.videos[7].video).into(holder.img8)
                    holder.imgCV8.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[7]) }
                    Glide.with(context!!).load(videoGroup.videos[8].video).into(holder.img9)
                    holder.imgCV9.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[8]) }
                    Glide.with(context!!).load(videoGroup.videos[9].video).into(holder.img10)
                    holder.imgCV10.setOnClickListener { videosArticlesFragment.openVideo(videoGroup.videos[9]) }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var img: ImageView = itemView.findViewById(R.id.img)
        var img2: ImageView = itemView.findViewById(R.id.img2)
        var img3: ImageView = itemView.findViewById(R.id.img5)
        var img4: ImageView = itemView.findViewById(R.id.img3)
        var img5: ImageView = itemView.findViewById(R.id.img4)
        var title1: TextView = itemView.findViewById(R.id.video1_title)
        var title2: TextView = itemView.findViewById(R.id.video2_title)
        var title3: TextView = itemView.findViewById(R.id.video3_title)
        var title4: TextView = itemView.findViewById(R.id.video4_title)
        var title5: TextView = itemView.findViewById(R.id.video5_title)
        var title6: TextView = itemView.findViewById(R.id.video6_title)
        var title7: TextView = itemView.findViewById(R.id.video7_title)
        var title8: TextView = itemView.findViewById(R.id.video8_title)
        var title9: TextView = itemView.findViewById(R.id.video9_title)
        var title10: TextView = itemView.findViewById(R.id.video10_title)
        var imgCV1: MaterialCardView = itemView.findViewById(R.id.pic1)
        var imgCV2: MaterialCardView = itemView.findViewById(R.id.pic2)
        var imgCV3: MaterialCardView = itemView.findViewById(R.id.pic5)
        var imgCV4: MaterialCardView = itemView.findViewById(R.id.pic3)
        var imgCV5: MaterialCardView = itemView.findViewById(R.id.pic4)
        var img6: ImageView = itemView.findViewById(R.id.img6)
        var img7: ImageView = itemView.findViewById(R.id.img7)
        var img8: ImageView = itemView.findViewById(R.id.img8)
        var img9: ImageView = itemView.findViewById(R.id.img9)
        var img10: ImageView = itemView.findViewById(R.id.img10)
        var imgCV6: MaterialCardView = itemView.findViewById(R.id.pic6)
        var imgCV7: MaterialCardView = itemView.findViewById(R.id.pic7)
        var imgCV8: MaterialCardView = itemView.findViewById(R.id.pic8)
        var imgCV9: MaterialCardView = itemView.findViewById(R.id.pic9)
        var imgCV10: MaterialCardView = itemView.findViewById(R.id.pic10)
        var layout2: ConstraintLayout = itemView.findViewById(R.id.layout2)
    }

}