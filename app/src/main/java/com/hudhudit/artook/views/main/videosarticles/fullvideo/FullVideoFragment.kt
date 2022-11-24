package com.hudhudit.artook.views.main.videosarticles.fullvideo

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.core.widget.NestedScrollView
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.modules.videosarticles.Video
import com.hudhudit.artook.databinding.FragmentFullVideoBinding
import com.hudhudit.artook.views.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FullVideoFragment : Fragment() {

    lateinit var binding: FragmentFullVideoBinding
    lateinit var mainActivity: MainActivity
    lateinit var video: Video

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_full_video, container, false)
        binding = FragmentFullVideoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mainActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClick()
        setData()
    }

    private fun onClick(){
        binding.close.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
    }

    private fun setData(){
        video = requireArguments().getParcelable("video")!!
        binding.videoTitle.text = video.title
        try {
            val link = video.video
            val mediaController = MediaController(context)
            mediaController.setAnchorView(binding.fullVideo)
            val video: Uri = Uri.parse(link)
            binding.fullVideo.setMediaController(mediaController)
            binding.fullVideo.setVideoURI(video)
            binding.fullVideo.start()
        } catch (e: Exception) {

        }
    }

}