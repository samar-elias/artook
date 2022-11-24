package com.hudhudit.artook.views.main.newpost

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hudhudit.artook.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewPostDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_post_details, container, false)
    }

}