package com.hudhudit.artook.views.main.chats.conversation

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppConstants.Companion.NOTIFICATION_URL
import com.hudhudit.artook.apputils.appdefs.AppConstants.Companion.SERVER_KEY
import com.hudhudit.artook.apputils.appdefs.AppConstants.Companion.USER
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.modules.chat.MessageModel
import com.hudhudit.artook.apputils.modules.chat.TokenChatResponse
import com.hudhudit.artook.apputils.modules.chat.UserChatModel

import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.apputils.remote.utill.Resource
import com.hudhudit.artook.databinding.FragmentConversationBinding
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.main.chats.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class ConversationFragment : Fragment() {

    lateinit var binding: FragmentConversationBinding
    lateinit var mainActivity: MainActivity

    lateinit var database: DatabaseReference
    lateinit var userChatModel: UserChatModel
    private val viewModel by viewModels<ConversationViewModel>()
    var messagesArrayList: MutableList<MessageModel>? = mutableListOf()
    var messageModel: MessageModel? = null
    lateinit var countMessageOwnerItem: String
    var countMassage: String = "0"
    lateinit var adapter: MessageChatAdapter
    var count: String = "-1"
    var x = 1
    var userId=""
    var recevertoken=""
    var userName=""
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
        binding.toolbarLayout.navigateBack.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
        userChatModel = requireArguments().getParcelable("profile")!!
        if (AppDefs.user.results!!.id == userChatModel!!.userOwnerItemId) {
            binding.toolbarLayout.title.text= userChatModel!!.nameUser
            userId=userChatModel.userLoginId.toString()
            userName=userChatModel.nameUser.toString()

        } else {
            binding.toolbarLayout.title.text= userChatModel!!.nameOwnerItemUser
            userId=userChatModel.userOwnerItemId.toString()
            userName=userChatModel.nameOwnerItemUser.toString()

        }


        Log.d("userChatModel",userChatModel.toString())
        init()
        onClick()
        getUserToken()
        checkOnlineStatus()
        updateCountMessage()
        getMessage()


    }

    private fun init(){
        database= FirebaseDatabase.getInstance().reference
        mainActivity.invisibleBottomBar()
        binding.sendMessage.setOnClickListener {
            val message = binding.messageEdt.text.toString()
            if (message.isEmpty())
                Toast.makeText(requireContext(), "Enter Message", Toast.LENGTH_SHORT).show()
            else {
                sendMessage(message, "text")
                getToken(message)

                binding.messageEdt.setText(" ")

            }
        }



    }

    private fun onClick(){

    }
    private fun sendMessage(message: String, typeMessage: String) {

        viewModel.addMessage(userChatModel!!.chatId.toString(),
            MessageModel(
                AppDefs.user.results!!.id,
                message,
                System.currentTimeMillis().toString(),
                userChatModel!!.chatId!!,
                messageModel?.messageId ?: "",
                typeMessage,
            )
        )
        viewModel.addMessageStatus.observe(viewLifecycleOwner, Observer {

            if (it != null) {
                if (it.status == Resource.Status.SUCCESS) {
                    messageModel = it!!.data!!.first

                    updateLastMassage(messageModel!!.message!!)
                }
                if (it.status == Resource.Status.ERROR) {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()

                }
            }

            viewModel.restAddMessageStatus()
        })
        x = 1
        updateCountMessage()

    }
    fun updateLastMassage(message: String) {


        viewModel.updateUserStatus(AppDefs.user.results!!.id,countMessageOwnerItem, countMassage, userChatModel!!, message)
        viewModel.updateStatus.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it.status == Resource.Status.SUCCESS) {
                    //  count=count+1
                    //   Toast.makeText(requireContext(), it.data.toString(), Toast.LENGTH_SHORT).show()
                    checkOnlineStatus()

                }
                if (it.status == Resource.Status.ERROR) {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()

                }
            }
            viewModel.resetCount()

        })
    }
    fun getMessage() {

        messagesArrayList!!.clear()
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.getMessage(userChatModel!!.chatId.toString())
        viewModel.messageStatus.observe(viewLifecycleOwner, Observer {
            //   Toast.makeText(requireContext(), count, Toast.LENGTH_SHORT).show()
            if (it != null) {
                if (it!!.status == Resource.Status.SUCCESS) {
                    messagesArrayList = it.data!!.toMutableList()



                   /* if (count == "0") {
                        messagesArrayList!!.forEach {
                            it.isCheck = true
                            it.whoBlock = viewModel.userChatModel!!.whoBlock
                            it.status = viewModel.userChatModel!!.status.toString()

                        }


                    } else {
                        //  Toast.makeText(requireContext(), count, Toast.LENGTH_SHORT).show()
                        messagesArrayList!!.forEach {
                            Log.d("myitem", x.toString() + "  " + (messagesArrayList!!.size - (count.toInt())))
                            it.isCheck = x < messagesArrayList!!.size - (count.toInt())
                            x += 1

                        }

                    }*/

                    adapter = MessageChatAdapter(messagesArrayList!!,userChatModel)
                    iJustWantToScroll()

                    binding.messageRecyclerView.adapter = adapter
                    adapter.notifyDataSetChanged()

                }
                if (it!!.status == Resource.Status.ERROR) {
                    Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT).show()

                }
            }
            viewModel.reset()


        })
    }

    fun iJustWantToScroll() {
        binding.messageRecyclerView.scrollToPosition(messagesArrayList!!.size - 1)
        binding.messageRecyclerView.smoothScrollToPosition(messagesArrayList!!.size - 1)
        binding.messageRecyclerView.layoutManager?.scrollToPosition(messagesArrayList!!.size - 1)
        (binding.messageRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(messagesArrayList!!.size - 1,
            messagesArrayList!!.size)
    }
    private fun checkOnlineStatus() {

        val databaseReference =
            FirebaseDatabase.getInstance().getReference(USER).child(userChatModel!!.chatId.toString())
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userModel = snapshot.getValue(UserChatModel::class.java)


                    countMessageOwnerItem = userModel!!.countMessageOwnerItem!!
                    countMassage = userModel!!.countMassage!!
                    if (AppDefs.user.results!!.id == userChatModel!!.userOwnerItemId) {
                        count = countMessageOwnerItem
                    } else {
                        count = countMassage
                    }



                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
   
    fun updateCountMessage() {
        viewModel.updateMassageCountUserStatus(AppDefs.user.results!!.id,userChatModel!!, "0")
        viewModel.updateCountStatus.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it.status == Resource.Status.SUCCESS) {
                    //  Toast.makeText(requireContext(), it.data.toString(), Toast.LENGTH_SHORT).show()
                }
                if (it.status == Resource.Status.ERROR) {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()

                }
            }
            viewModel.restCountStatus()
        })
    }
    private fun getUserToken(){
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", AppDefs.user.token!!)
                    builder.header("Lang", AppDefs.lang!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val participantsCall: Call<TokenChatResponse> =
            retrofit.create(RetrofitAPIs::class.java).getClientToken(userId.toInt())
        participantsCall.enqueue(object : Callback<TokenChatResponse> {
            override fun onResponse(call: Call<TokenChatResponse>, response: Response<TokenChatResponse>) {

                if (response.isSuccessful){
                    recevertoken=response.body()!!.results.fcm_token.toString()
                    println("myusertoken"+response.body()!!.results.fcm_token.toString())
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TokenChatResponse>, t: Throwable) {

                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }
    private fun getToken(message: String) {
        val to = JSONObject()
        val data = JSONObject()
        data.put("title", userName)
        data.put("body", message)
        to.put("to", recevertoken)
        to.put("data", data)
        sendNotification(to)
    }

    private fun sendNotification(to: JSONObject) {

        val request: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            NOTIFICATION_URL,
            to,
            com.android.volley.Response.Listener { response: JSONObject ->

                Log.d("TAG", "onResponse: $response")
            },
            com.android.volley.Response.ErrorListener {

                Log.d("TAG", "onError: $it")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val map: MutableMap<String, String> = HashMap()

                map["Authorization"] = "key=" + SERVER_KEY
                map["Content-type"] = "application/json"
                return map
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        val requestQueue = Volley.newRequestQueue(requireContext())
        request.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        requestQueue.add(request)

    }


}