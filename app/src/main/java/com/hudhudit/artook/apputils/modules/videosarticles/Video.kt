package com.hudhudit.artook.apputils.modules.videosarticles

import android.os.Parcelable
import android.view.View
import com.hudhudit.artook.apputils.modules.status.Status
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Video(val id: String,
                 val title: String,
                 val video: String,
                 val status: String): Parcelable

data class Videos(val count: String,
                    val data: ArrayList<Video>)

data class VideosResults(val status: Status, val results: Videos)

data class VideosGroup(val videos: ArrayList<Video>)