package com.hudhudit.artook.views.main.chats

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.modules.chatmessage.ChatMessage
import com.hudhudit.artook.apputils.modules.chatmessage.ChatRoom
import com.hudhudit.artook.databinding.FragmentChatsBinding
import com.hudhudit.artook.views.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ChatsFragment : Fragment() {

    lateinit var binding: FragmentChatsBinding
    lateinit var mainActivity: MainActivity
    var chatList: ArrayList<ChatRoom> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_chats, container, false)
        binding = FragmentChatsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mainActivity = context
        }
    }

    fun checkRes(query: Query){
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d("WasheeInboxViewModel.chatsValueEventListener.onCancelled")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                        gson.fromJson(dataSnapshot.toString(), ChatRoom.class)
                Timber.d("dataSnapshot : $dataSnapshot")
                chatList = ArrayList()
                for (roomSnapshot: DataSnapshot in dataSnapshot.children) {
                    val chatRoom: ChatRoom = roomSnapshot.getValue(ChatRoom::class.java)!!
                    chatRoom.chatId = roomSnapshot.key ?: ""

                    chatRoom.let {
                        it.messages = ArrayList()
                        val messageRef = roomSnapshot.child("messages")
                        for (msgSnapshot: DataSnapshot in messageRef.children) {
                            it.messages!!.add(msgSnapshot.getValue(ChatMessage::class.java)!!)
                        }
                    }
                    chatList.add(chatRoom)
                }

            }
        })
    }

}