package com.hudhudit.artook.apputils.modules.chat

import com.google.gson.annotations.SerializedName

data class ReportModel (
    var id:String="",
    @SerializedName("title")
    val title: String = "",
    var isSelected: Boolean,
        )