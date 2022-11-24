package com.hudhudit.artook.apputils.modules.chatmessage

import com.hudhudit.artook.apputils.modules.profile.UserProfile
import com.hudhudit.artook.apputils.modules.user.UserData
import java.util.*
import kotlin.collections.ArrayList

class ChatRoom {
    var chatId : String? = null
    var lastMessage: String? = null
    var senderId: String? = null
    var senderName: String? = null
    var senderImg: String? = null
    var receiverId: String? = null
    var receiverName: String? = null
    var receiverImg: String? = null
    var messages: ArrayList<ChatMessage>? = null
    var messageTime: Long = 0

    constructor()

    constructor(
        chatId: String?,
        lastMessage: String?,
        senderId: String?,
        senderName: String?,
        senderImg: String?,
        receiverId: String?,
        receiverName: String?,
        receiverImg: String?,
        messages: ArrayList<ChatMessage>?
    ) {
        this.chatId = chatId
        this.lastMessage = lastMessage
        this.senderId = senderId
        this.senderName = senderName
        this.senderImg = senderImg
        this.receiverId = receiverId
        this.receiverName = receiverName
        this.receiverImg = receiverImg
        this.messages = messages

        // Initialize to current time
        messageTime = System.currentTimeMillis()
    }
}