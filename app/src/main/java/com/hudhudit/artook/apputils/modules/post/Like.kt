package com.hudhudit.artook.apputils.modules.post

import com.hudhudit.artook.apputils.modules.status.Status

data class Counts(val number_posts_comments: String,
                val number_posts_like: String)

data class CountsResult(val status: Status,
                      val results: Counts)
