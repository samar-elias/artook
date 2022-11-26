package com.hudhudit.artook.apputils.modules.chat

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ItemQuaryModel(
    val id:String="",
    val name: String = "",
    val image: String = "",
    val description: String = "",
    val price: String = "",

    ) : Parcelable {
    constructor() : this("", "","","","")

}