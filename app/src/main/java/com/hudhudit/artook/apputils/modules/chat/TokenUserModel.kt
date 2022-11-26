package com.hudhudit.artook.apputils.modules.chat

import com.google.gson.annotations.SerializedName

data class TokenUserModel (
    var tokenId: String? = "",
    var userId: String? = "",
    val token: String? = "",
    )