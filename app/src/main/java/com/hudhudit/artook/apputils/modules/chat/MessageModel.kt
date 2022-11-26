package com.hudhudit.artook.apputils.modules.chat

import android.os.Parcelable
import com.hudhudit.artook.apputils.modules.chat.ItemQuaryModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MessageModel(

    var sender: String? = "",
    var message: String? = "",

    var messageTime: String? = "",

    var chatId: String? = "",
    var messageId: String? = "",
    var type: String? = "",




    ) : Parcelable {
    constructor() : this( "","", "", "", "", "")




}