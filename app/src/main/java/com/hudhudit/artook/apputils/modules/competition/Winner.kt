package com.hudhudit.artook.apputils.modules.competition

import com.hudhudit.artook.apputils.modules.status.Status

data class Winner(val id: String,
                  val client_id: String,
                  val image: String,
                  val name: String,
                  val user_name: String,
                  val image_client: String,
                  val price: String)

data class Winners(val count: String,
                   val data: ArrayList<Winner>)

data class WinnersResult(val status: Status,
                   val results: Winners)
