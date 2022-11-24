package com.hudhudit.artook.apputils.modules.profile

import com.hudhudit.artook.apputils.modules.status.Status

data class Follower(val id: String,
                    val name: String,
                    val image: String,
                    val email: String,
                    val bio: String)

data class Followers(val count_page: String,
                     val data: ArrayList<Follower>)

data class FollowersResult(val status: Status, val results: Followers)
