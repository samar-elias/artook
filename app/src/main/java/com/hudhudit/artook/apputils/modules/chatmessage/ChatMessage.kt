package com.hudhudit.artook.apputils.modules.chatmessage

import com.hudhudit.artook.apputils.modules.profile.UserProfile
import com.hudhudit.artook.apputils.modules.user.UserData
import java.util.*

class ChatMessage {
    var chatId: String? = null
    var messageText: String? = null
    var messageId: String? = null
    var messageTime: Long = 0

    constructor(chatId: String?, messageText: String?, messageId: String?) {
        this.messageText = messageText
        this.chatId = chatId
        this.messageId = messageId

        // Initialize to current time
        messageTime = Date().time
    }

    constructor()
}