package com.hudhudit.artook.apputils.modules.user

import com.hudhudit.artook.apputils.modules.status.Status

data class SearchUser(val client_id: String,
                      val name: String,
                      val user_name: String,
                      val image_client: String)

data class SearchUsers(val count: String,
                       val data: ArrayList<SearchUser>)

data class SearchUsersResult(val status: Status,
                             val results: SearchUsers)
