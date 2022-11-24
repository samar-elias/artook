package com.hudhudit.artook.apputils.modules.videosarticles

import com.hudhudit.artook.apputils.modules.status.Status

data class VideosArticlesCount(val no_video: String, val no_articles: String)

data class VideosArticlesCountResult(val status: Status, val results: VideosArticlesCount)
