package com.hudhudit.artook.apputils.modules.chat

import android.os.Parcelable
import com.hudhudit.artook.apputils.modules.chat.MessageModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserChatModel(
    var chatId: String? = "",
    var userLoginId: String? = "",
    var userOwnerItemId: String? = "",
    var nameOwnerItemUser: String? = "",
    var nameUser: String? = "",
    var lastMessage: String? = "",
    var imageOwnerItem: String? = "",
    var date: String? = "",
    var imageUser: String? = "",

    var countMassage: String? = "0",
    var countMessageOwnerItem: String? = "0",
    var messageModel: MessageModel?,

    ) : Parcelable {
    constructor() : this("", "", "", "", "", "", "", "", "", "0", "0", MessageModel())

    constructor(
        chatId: String,
        userLoginId: String,
        userOwnerItemId: String,
        nameOwnerItemUser: String,
        nameUser: String,
        lastMessage: String,
        imageOwnerItem: String,
        date: String,
        imageUser: String,
        countMassage: String,
        countMessageOwnerItem: String,

        ) : this() {
        this.chatId = chatId
        this.userLoginId = userLoginId
        this.userOwnerItemId = userOwnerItemId
        this.nameOwnerItemUser = nameOwnerItemUser
        this.nameUser = nameUser
        this.lastMessage = lastMessage
        this.imageOwnerItem = imageOwnerItem
        this.date = date

        this.imageUser = imageUser
        this.countMassage=countMassage
        this.countMessageOwnerItem=countMessageOwnerItem


    }


}