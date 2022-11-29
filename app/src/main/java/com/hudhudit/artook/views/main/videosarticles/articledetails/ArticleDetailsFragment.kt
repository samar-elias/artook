package com.hudhudit.artook.views.main.videosarticles.articledetails

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.hudhudit.artook.apputils.modules.videosarticles.Article
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.databinding.FragmentArticleDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArticleDetailsFragment : Fragment() {

    lateinit var binding: FragmentArticleDetailsBinding
    lateinit var mainActivity: MainActivity
    lateinit var article: Article

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_article_details, container, false)
        binding = FragmentArticleDetailsBinding.inflate(layoutInflater)
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
        mainActivity.visibleBottomBar()
        if (AppDefs.lang == "ar"){
            binding.toolbarLayout.navigateBack.scaleX = (-1).toFloat()
        }else{
            binding.toolbarLayout.navigateBack.scaleX = (1).toFloat()
        }
    }

    private fun onClick(){
        binding.toolbarLayout.navigateBack.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
    }

    private fun setData(){
        article  = requireArguments().getParcelable("article")!!
        binding.toolbarLayout.title.text = article.title
        if (article.image.startsWith("http")){
            Glide.with(mainActivity).load(article.image).into(binding.image)
        }
        binding.articleTitle.text = article.title
        binding.articleDate.text = article.date
        binding.article.text = article.description
    }
}