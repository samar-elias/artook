package com.hudhudit.artook.apputils.modules.profile

import com.hudhudit.artook.apputils.modules.status.Status

data class Following(val id: String,
                    val name: String,
                    val image: String,
                    val email: String,
                    val bio: String)

data class Followings(val count_page: String,
                     val data: ArrayList<Following>)

data class FollowingsResult(val status: Status,
                            val results: Followings)
