package com.hudhudit.artook.apputils.remote.repostore

import com.hudhudit.artook.apputils.modules.chat.MessageModel
import com.hudhudit.artook.apputils.modules.chat.UserChatModel
import com.hudhudit.artook.apputils.remote.utill.Resource


interface ChateRepository {
    suspend fun getChat(id:String,result: (Resource<MutableList<UserChatModel>>) -> Unit)
    suspend fun addChat(userChatModel: UserChatModel,result: (Resource<Pair<UserChatModel, String>>) -> Unit)
    suspend fun sendMessage(chatId:String,messageModel: MessageModel, result: (Resource<Pair<MessageModel,
            String>>) -> Unit)
    suspend fun getMessage( chatId:String,result: (Resource<MutableList<MessageModel>>) -> Unit)


    suspend fun updateMassage( id:String,countMessageOwnerItem:String,
                               countMassage:String,userChatModel:UserChatModel,lastMessage:String,result: (Resource<String>) -> Unit)
    suspend fun updateCountMassage(id:String,userChatModel: UserChatModel,count:String,result: (Resource<String>) -> Unit)


}