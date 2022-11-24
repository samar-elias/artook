package com.hudhudit.artook.views.main.chats.conversation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.modules.chatmessage.ChatMessage
import com.hudhudit.artook.apputils.modules.chatmessage.ChatRoom
import com.hudhudit.artook.apputils.modules.profile.UserProfile
import com.hudhudit.artook.databinding.FragmentConversationBinding
import com.hudhudit.artook.views.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConversationFragment : Fragment() {

    lateinit var binding: FragmentConversationBinding
    lateinit var mainActivity: MainActivity
    lateinit var user: UserProfile
    lateinit var database: DatabaseReference
    lateinit var chatDataReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_conversation, container, false)
        binding = FragmentConversationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mainActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        onClick()
    }

    private fun init(){
        database= FirebaseDatabase.getInstance().reference
        mainActivity.invisibleBottomBar()
        user = requireArguments().getParcelable("profile")!!
    }

    private fun onClick(){
        binding.sendMessage.setOnClickListener {
            if (binding.messageEdt.text.toString().isNotEmpty()){
                sendMessage()
            }
        }
    }

    private fun sendMessage(){
//        FirebaseDatabase.getInstance()
//            .reference
//            .push()
//            .setValue(
//                ChatMessage(
//                    binding.messageEdt.text.toString(),
//                    AppDefs.user.results!!.id
//                )
//            )
        binding.messageEdt.setText("")

//        FirebaseDatabase.getInstance().reference
    }

}