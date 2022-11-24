package com.hudhudit.artook.apputils.modules.profile

import com.hudhudit.artook.apputils.modules.status.Status

data class Counts(val followers: String,
                  val following: String)

data class ProfileCounts(val status: Status,
                         val results: Counts)
