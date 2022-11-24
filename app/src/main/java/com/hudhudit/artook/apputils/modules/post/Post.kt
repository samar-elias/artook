package com.hudhudit.artook.apputils.modules.post

import android.os.Parcelable
import com.hudhudit.artook.apputils.modules.status.Status
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Post(val id: String,
                val client_id: String,
                val name: String,
                val image_client: String,
                val description: String,
                val date: String,
                val time: String,
                val posts_media: ArrayList<PostMedia>,
                val number_posts_comments: String,
                var number_posts_like: String,
                var is_like: String,
                var is_save: String): Parcelable

data class Posts(val count_page: String,
                 val data: ArrayList<Post>)

data class PostsResult(val status: Status,
                       val results: Posts?)

data class PostResult(val status: Status,
                      val results: Post)
