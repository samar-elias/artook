package com.hudhudit.artook.apputils.remote.repostore

import android.text.format.DateFormat
import com.hudhudit.artook.apputils.modules.chat.AddReportModel
import com.hudhudit.artook.apputils.modules.chat.MessageModel
import com.hudhudit.artook.apputils.modules.chat.TokenUserModel
import com.hudhudit.artook.apputils.modules.chat.UserChatModel


import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hudhudit.artook.apputils.appdefs.AppConstants.Companion.MESSAGEMODEL
import com.hudhudit.artook.apputils.appdefs.AppConstants.Companion.USER

import com.hudhudit.artook.apputils.remote.utill.Resource


class ChatRepositoryImp(
    val database: FirebaseDatabase,


) : ChateRepository {
    override suspend fun getChat( id:String,result: (Resource<MutableList<UserChatModel>>) -> Unit) {

        var userloginId  = (id).toString()


        database.getReference(USER).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // userChat.clear()
                    var userChat = mutableListOf<UserChatModel>()
                    for (dataSnapshot1 in dataSnapshot.children) {
                        val userChatModel: UserChatModel? = dataSnapshot1.getValue(UserChatModel::class.java)
                        if (userChatModel!!.userLoginId.equals(userloginId) || userChatModel.userOwnerItemId.equals(
                                userloginId)
                        ) {
                            userChat.add(userChatModel)
                        }
                    }

                    result.invoke(
                        Resource.success(userChat)
                    )

                } else {
                    result.invoke(
                        Resource.error("no data", null)
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                result.invoke(
                    Resource.error(
                        error.toString(), null
                    )
                )
            }

        })

    }

    override suspend fun addChat(
        userChatModel: UserChatModel,
        result: (Resource<Pair<UserChatModel, String>>) -> Unit,
    ) {
        val keyDatabaseReference = database.getReference(USER).push()
        val key = keyDatabaseReference.key
        userChatModel.chatId = key!!
        userChatModel.messageModel = null
        database.getReference(USER).child(key).setValue(userChatModel).addOnSuccessListener {
            result.invoke(
                Resource.success(Pair(userChatModel, "chat has been created successfully"))
            )

        }.addOnFailureListener {
            result.invoke(
                Resource.error(
                    it.localizedMessage
                )
            )
        }


    }

    override suspend fun sendMessage(

        chatId: String,
        messageModel: MessageModel,
        result: (Resource<Pair<MessageModel, String>>) -> Unit,
    ) {
        val keyDatabaseReference = database.getReference(USER).child(chatId).child(
            MESSAGEMODEL).push()
        val key = keyDatabaseReference.key
        messageModel.messageId = key!!
        keyDatabaseReference.setValue(messageModel).addOnSuccessListener {
            result.invoke(
                Resource.success(Pair(messageModel, "message create success"))
            )

        }.addOnFailureListener {
            result.invoke(
                Resource.error(
                    it.localizedMessage
                )
            )
        }


    }

    override suspend fun getMessage( chatId: String, result: (Resource<MutableList<MessageModel>>) -> Unit) {
        database.getReference(USER).child(chatId).child(MESSAGEMODEL)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {

                        var userChat = mutableListOf<MessageModel>()
                        for (dataSnapshot1 in dataSnapshot.children) {
                            val userChatModel: MessageModel? = dataSnapshot1.getValue(MessageModel::class.java)
                            userChat.add(userChatModel!!)
                        }

                        result.invoke(
                            Resource.success(userChat)
                        )

                    } else {
                        result.invoke(
                            Resource.error("no data", null)
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    result.invoke(
                        Resource.error(
                            error.toString(), null
                        )
                    )
                }

            })


    }


    override suspend fun updateMassage(
        id:String,
        countMessageOwnerItem:String,
        countMassage:String,
        userChatModel: UserChatModel,
        lastMessage: String,
        result: (Resource<String>) -> Unit,
    ) {

        var userloginId = id.toString()

        val map = HashMap<String, Any>()
        map["lastMessage"] = lastMessage
        map["date"] = System.currentTimeMillis().toString()
        if (userloginId == userChatModel.userOwnerItemId) {
            var count1: String = ((countMessageOwnerItem.toInt())+1).toString().trim()
            map["countMessageOwnerItem"] =count1

        } else {
            var count1: String =((countMassage.toInt())+1).toString().trim()

            map["countMassage"] = count1

        }


        database.getReference(USER).child(userChatModel.chatId!!).updateChildren(map).addOnSuccessListener {
            result.invoke(
                Resource.success("update successfully")
            )

        }.addOnFailureListener {
            result.invoke(
                Resource.error(
                    it.localizedMessage
                )
            )
        }
    }

    override suspend fun updateCountMassage(id:String,userChatModel: UserChatModel,count:String,
                                            result: (Resource<String>) -> Unit) {

        var userloginId =id.toString()

        val map = HashMap<String, Any>()

        if (userloginId == userChatModel.userOwnerItemId) {
            map["countMassage"] = count
        } else {
            map["countMessageOwnerItem"] = count

        }

        database.getReference(USER).child(userChatModel.chatId!!).updateChildren(map).addOnSuccessListener {
            result.invoke(
                Resource.success("update successfully")
            )

        }.addOnFailureListener {
            result.invoke(
                Resource.error(
                    it.localizedMessage
                )
            )
        }

    }




}