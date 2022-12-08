package com.hudhudit.artook.views.main.chats

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.modules.chat.UserChatModel
import com.hudhudit.artook.apputils.modules.chatmessage.ChatMessage
import com.hudhudit.artook.apputils.modules.chatmessage.ChatRoom
import com.hudhudit.artook.apputils.modules.profile.UserProfile
import com.hudhudit.artook.apputils.remote.utill.Resource
import com.hudhudit.artook.databinding.FragmentChatsBinding
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.main.chats.conversation.ConversationFragment
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ChatsFragment : Fragment() {

    lateinit var binding: FragmentChatsBinding
    lateinit var mainActivity: MainActivity
    var chatList: ArrayList<ChatRoom> = ArrayList()


    private val viewModel by viewModels<ChatViewModel>()

    var userChatModel: UserChatModel? = null
    var listChatModel: MutableList<UserChatModel>? = null

    lateinit var adapter: ChatAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_chats, container, false)
        binding = FragmentChatsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (AppDefs.lang == "ar"){
            binding.toolbarLayout.navigateBack.scaleX = (-1).toFloat()
        }else{
            binding.toolbarLayout.navigateBack.scaleX = (1).toFloat()
        }

        binding.toolbarLayout.navigateBack.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
        binding.toolbarLayout.title.text= resources.getString(R.string.chats)

        viewModel.getChat(AppDefs.user.results!!.id)
        setUpRecyclerView()
        getChat()
        binding.searchLayout.setOnClickListener {
            binding.serch.isFocusableInTouchMode = true
        }
        binding.serch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchCompany(query)

                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                searchCompany(newText)
                return true
            }
        })


    }
    fun setUpRecyclerView(){
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.recycler.layoutManager = linearLayoutManager

    }

    private fun getChat() {

        viewModel.chatStatus.observe(viewLifecycleOwner, Observer {
            if(it != null){
                if (it!!.status == Resource.Status.SUCCESS) {
                    listChatModel=it.data!!.toMutableList()
                    listChatModel!!.sortByDescending { it.date }

                    adapter =  ChatAdapter(listChatModel!!
                    ) {

                    /*    if (findNavController().currentDestination?.id == R.id.chatFragment) {
                            findNavController().navigate(R.id.action_chatFragment_to_chatDetailsFragment,
                                bundleOf(Pair(ChatDetailsFragment.KEY, it!!)))
                        }

*/                    userChatModel=it!!
                        openChat()
                    }
                    //  adapter.updateList(list)


                    binding.recycler.adapter = adapter
                    adapter.notifyDataSetChanged()

                }
                if (it!!.status == Resource.Status.ERROR) {
                    Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
            viewModel.reset()
        })

    }
    fun searchCompany(str:String){
        var result= listChatModel!!.filter {  it.nameUser!!.contains(str,true) ||  it.nameOwnerItemUser!!.contains(str,true)}
        adapter = ChatAdapter(result as MutableList<UserChatModel>
        ) {
            userChatModel=it!!
            openChat()

        }
        binding.recycler.adapter = adapter
        adapter.notifyDataSetChanged()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mainActivity = context
        }

    }

    private fun openChat() {
        val fragment: Fragment = ConversationFragment()
        //  val fragment: Fragment = ChatsFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putParcelable("profile", userChatModel)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("chat")
        fragmentTransaction.commit()
    }


}