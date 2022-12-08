package com.hudhudit.artook.apputils.modules.post

import com.hudhudit.artook.apputils.modules.status.Status

data class Comment(val id: String,
                   val client_id: String,
                   val name: String,
                   val image_client: String,
                   val title: String,
                   val time: String,
                   var convertedTime: String = "")

data class Comments(val count_page: String,
                    val data: ArrayList<Comment>)

data class CommentsResult(val status: Status,
                          val results: Comments)
