package com.hudhudit.artook.apputils.modules.videosarticles

import android.os.Parcelable
import com.hudhudit.artook.apputils.modules.status.Status
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Article(val id: String,
                   val title: String,
                   val description: String,
                   val image: String,
                   val date: String,
                   val status: String): Parcelable

data class Articles(val count: String,
                    val data: ArrayList<Article>)

data class ArticlesResults(val status: Status, val results: Articles)
