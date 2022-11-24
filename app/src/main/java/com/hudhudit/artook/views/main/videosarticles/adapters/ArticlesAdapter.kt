package com.hudhudit.artook.views.main.videosarticles.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hudhudit.artook.apputils.modules.videosarticles.Article
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.videosarticles.VideosArticlesFragment

class ArticlesAdapter(
    private var videosArticlesFragment: VideosArticlesFragment,
    private var articles: ArrayList<Article>
) :
    RecyclerView.Adapter<ArticlesAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.article_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val article = articles[position]

        if (article.image.startsWith("http")){
            Glide.with(context!!).load(article.image).into(holder.articleImg)
        }

        holder.articleTitle.text = article.title
        holder.articleDate.text = article.date
        holder.article.text = article.description

        holder.itemView.setOnClickListener { videosArticlesFragment.readMore(article) }
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var articleImg: ImageView = itemView.findViewById(R.id.image)
        var articleTitle: TextView = itemView.findViewById(R.id.article_title)
        var articleDate: TextView = itemView.findViewById(R.id.article_date)
        var article: TextView = itemView.findViewById(R.id.article)
        var readMore: LinearLayoutCompat = itemView.findViewById(R.id.read_more)
    }

}