package com.hudhudit.artook.views.video

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.modules.videosarticles.Video
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FullVideoActivity : AppCompatActivity() {

    lateinit var video: Video
    lateinit var fullVideo: VideoView
    lateinit var close: ImageView
    lateinit var videoTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_video)
        initViews()
        onClick()
        setData()
    }

    private fun initViews(){
        fullVideo = findViewById(R.id.full_video)
        close = findViewById(R.id.close)
        videoTitle = findViewById(R.id.video_title)
    }

    private fun onClick(){
        close.setOnClickListener { finish() }
    }

    private fun setData(){
        video = intent.getParcelableExtra("video")!!
        videoTitle.text = video.title
        try {
            val link = video.video
            val mediaController = MediaController(this)
            mediaController.setAnchorView(fullVideo)
            val video: Uri = Uri.parse(link)
            fullVideo.setMediaController(mediaController)
            fullVideo.setVideoURI(video)
            fullVideo.start()
        } catch (e: Exception) {

        }
    }
}