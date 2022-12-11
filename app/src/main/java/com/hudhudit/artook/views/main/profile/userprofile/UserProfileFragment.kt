package com.hudhudit.artook.views.main.profile.userprofile

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.modules.post.Post
import com.hudhudit.artook.apputils.modules.post.PostsResult
import com.hudhudit.artook.apputils.modules.profile.UserProfile
import com.hudhudit.artook.apputils.modules.profile.UserProfileData
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.modules.chat.UserChatModel
import com.hudhudit.artook.apputils.remote.utill.Resource
import com.hudhudit.artook.views.main.comments.CommentsFragment
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.main.notifications.NotificationsFragment
import com.hudhudit.artook.views.main.profile.followers.FollowersFragment
import com.hudhudit.artook.views.main.profile.following.FollowingFragment
import com.hudhudit.artook.views.main.profile.userprofile.adapters.PostsAdapter
import com.hudhudit.artook.databinding.FragmentUserProfileBinding
import com.hudhudit.artook.views.main.chats.ChatViewModel
import com.hudhudit.artook.views.main.chats.ChatsFragment
import com.hudhudit.artook.views.main.chats.conversation.ConversationFragment

import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    lateinit var binding: FragmentUserProfileBinding
    lateinit var mainActivity: MainActivity
    lateinit var userProfile: UserProfile
    var posts: ArrayList<Post> = ArrayList()
    var clientId = ""
    var listChatModel: MutableList<UserChatModel>? = null
    //  lateinit var navController: NavController

    var page = 1
    private val viewModel by viewModels<ChatViewModel>()
    var userChatModel: UserChatModel? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_user_profile, container, false)
        binding = FragmentUserProfileBinding.inflate(layoutInflater)
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

        init(view)
        onClick()
        getOpenedNotifications()
        getUserProfile()
    }

    private fun init(view: View) {
        posts.clear()
        clientId = requireArguments().getString("clientId")!!
        mainActivity.visibleBottomBar()
        binding.toolbarLayout.navigateBack.visibility = View.VISIBLE
        if (AppDefs.lang == "ar"){
            binding.toolbarLayout.navigateBack.scaleX = (-1).toFloat()
        }else{
            binding.toolbarLayout.navigateBack.scaleX = (1).toFloat()
        }

    }

    private fun onClick() {
        binding.toolbarLayout.navigateBack.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
        binding.followersLayout.setOnClickListener { navigateToMyFollowers() }
        binding.followingLayout.setOnClickListener { navigateToMyFollowings() }
        binding.toolbarLayout.notifications.setOnClickListener { navigateToNotifications() }
        binding.postsNSV.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                page++
                getUserPosts()
            }
        }
        binding.followBtn.setOnClickListener {
            if (userProfile.is_following == "0") {
                followUser()
            } else {
                unFollowUser()
            }
        }
        binding.chatsBtn.setOnClickListener {

            viewModel.getChat(AppDefs.user.results!!.id)
            getChat()


        }
    }

    private fun getOpenedNotifications() {
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
        val reportCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).getOpenedNotifications()
        reportCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(
                call: Call<BooleanResponse>,
                response: Response<BooleanResponse>
            ) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    if (response.body()!!.results!!) {
                        binding.toolbarLayout.notificationsBadge.visibility = View.VISIBLE
                    } else {
                        binding.toolbarLayout.notificationsBadge.visibility = View.INVISIBLE
                    }
                } else {
                    val gson = Gson()
                    val type = object :
                        TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(
                        response.errorBody()!!.charStream(),
                        type
                    ) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun getUserProfile() {
        binding.progressBar.visibility = View.VISIBLE
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
        val userProfileCall: Call<UserProfileData> =
            retrofit.create(RetrofitAPIs::class.java).getUserProfile(clientId)
        userProfileCall.enqueue(object : Callback<UserProfileData> {
            override fun onResponse(
                call: Call<UserProfileData>,
                response: Response<UserProfileData>
            ) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    userProfile = response.body()!!.results
                    getUserPosts()
                    setData()
                } else {
                    val gson = Gson()
                    val type = object :
                        TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(
                        response.errorBody()!!.charStream(),
                        type
                    ) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(
                        mainActivity,
                        errorResponse.status.massage.toString(),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }

            override fun onFailure(call: Call<UserProfileData>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun setData() {
        binding.followersCount.text = userProfile.followers
        binding.followingCount.text = userProfile.following
        binding.fullName.text = userProfile.name
        binding.userName.text = userProfile.user_name
        binding.bio.text = userProfile.bio
        if (userProfile.image_client.isNotEmpty()) {
            Glide.with(mainActivity).load(userProfile.image_client).into(binding.userImg)
        }
        if (userProfile.is_following == "1") {
            binding.followText.text = resources.getText(R.string.unfollow)
        } else {
            binding.followText.text = resources.getText(R.string.follow)
        }
    }

    private fun getUserPosts() {
        binding.progressBar.visibility = View.VISIBLE
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
        val postsCallCall: Call<PostsResult> =
            retrofit.create(RetrofitAPIs::class.java).getUserPosts(userProfile.id, page.toString())
        postsCallCall.enqueue(object : Callback<PostsResult> {
            override fun onResponse(call: Call<PostsResult>, response: Response<PostsResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    for (post in response.body()!!.results!!.data) {
                        posts.add(post)
                    }
                    setPostsRV()
                } else {
                    val gson = Gson()
                    val type = object :
                        TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(
                        response.errorBody()!!.charStream(),
                        type
                    ) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                    binding.noPosts.visibility = View.VISIBLE
                    binding.noPosts.text = resources.getString(R.string.no_feed_posts_yet)
                    binding.postsNSV.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<PostsResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun setPostsRV() {
        binding.noPosts.visibility = View.GONE
        binding.postsNSV.visibility = View.VISIBLE
        val adapter = PostsAdapter(this, posts)
        binding.myPostsRV.adapter = adapter
        binding.myPostsRV.layoutManager = GridLayoutManager(mainActivity, 3)
    }

    private fun followUser() {
        binding.progressBar.visibility = View.VISIBLE
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
        val postsCallCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).followUser(userProfile.id)
        postsCallCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(
                call: Call<BooleanResponse>,
                response: Response<BooleanResponse>
            ) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    userProfile.is_following = "1"
                    posts.clear()
                    getUserProfile()
                } else {
                    val gson = Gson()
                    val type = object :
                        TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(
                        response.errorBody()!!.charStream(),
                        type
                    ) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun unFollowUser() {
        binding.progressBar.visibility = View.VISIBLE
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
        val postsCallCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).unFollowUser(userProfile.id)
        postsCallCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(
                call: Call<BooleanResponse>,
                response: Response<BooleanResponse>
            ) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    userProfile.is_following = "0"
                    posts.clear()
                    getUserProfile()
                } else {
                    val gson = Gson()
                    val type = object :
                        TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(
                        response.errorBody()!!.charStream(),
                        type
                    ) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    fun openPost(post: Post) {
        val fragment: Fragment = CommentsFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("postId", post.id)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("Post")
        fragmentTransaction.commit()
    }

    private fun navigateToMyFollowers() {
        val fragment: Fragment = FollowersFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("clientId", userProfile.id)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("Followers")
        fragmentTransaction.commit()
    }

    private fun navigateToMyFollowings() {
        val fragment: Fragment = FollowingFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("clientId", userProfile.id)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("Followings")
        fragmentTransaction.commit()
    }

    private fun navigateToNotifications() {
        val fragment: Fragment = NotificationsFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("Notifications")
        fragmentTransaction.commit()
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

    private fun getChat() {
        viewModel.chatStatus.observe(viewLifecycleOwner, Observer {

            if (it!!.status == Resource.Status.SUCCESS) {
                Log.d("mydata", it!!.data!!.toMutableList().toString())
                listChatModel = it.data!!.toMutableList()
                //  var userOwnerItemId: String = (viewModel3.item!!.company_id).toString()
                var x = listChatModel!!.filter {

                    it.userOwnerItemId!! == userProfile.id.toString() && it.userLoginId == AppDefs.user.results!!.id.toString() || it.userOwnerItemId!! == AppDefs.user.results!!.id.toString() && it.userLoginId == userProfile.id.toString()

                }
                if (!x.isNullOrEmpty()) {
                    x[0].chatId
                    Toast.makeText(requireContext(), x[0].chatId, Toast.LENGTH_SHORT).show()
                    /*  if (findNavController().currentDestination?.id == R.id.companyProfileFragment) {
                          findNavController().navigate(R.id.action_companyProfileFragment_to_chatDetailsFragment5,
                              bundleOf(Pair(ChatDetailsFragment.KEY, x[0]))
                          )

                      }*/
                    userChatModel=x[0]
                    openChat()


                } else {
                    addNewChat()

                }
            }
            if (it!!.status == Resource.Status.ERROR) {
                // binding.progressBar.hide()
                if (it.message.toString().equals("no data")) {
                    Toast.makeText(requireContext(), "add first chat", Toast.LENGTH_SHORT).show()
                    addNewChat()

                } else {
                    Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT)
                        .show()

                }
            }
        })

    }

    fun addNewChat() {
        var userloginId: String = (AppDefs.user.results!!.id).toString()

        viewModel.addChat(
            UserChatModel(
                userChatModel?.chatId ?: "",
                userloginId,
                userProfile.id,
                userProfile.name,
                AppDefs.user.results!!.user_name,
                "",
                userProfile.image_client,
                System.currentTimeMillis().toString(),
                AppDefs.user.results!!.image,
                "0",
                "0"

                )
        )
        viewModel.addChatStatus.observe(viewLifecycleOwner, Observer {
            if (it.status == Resource.Status.SUCCESS) {
                Toast.makeText(requireContext(), it.data!!.second, Toast.LENGTH_SHORT).show()
                userChatModel = it.data!!.first
                /*  if (findNavController().currentDestination?.id == R.id.companyProfileFragment) {
                      findNavController().navigate(R.id.action_companyProfileFragment_to_chatDetailsFragment5,
                          bundleOf(Pair(ChatDetailsFragment.KEY, userChatModel))
                      )
                  }*/
                openChat()

            }
            if (it.status == Resource.Status.ERROR) {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()

            }
        })
    }
}
