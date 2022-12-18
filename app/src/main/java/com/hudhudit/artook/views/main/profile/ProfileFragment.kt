package com.hudhudit.artook.views.main.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.modules.post.Post
import com.hudhudit.artook.apputils.modules.post.PostsResult
import com.hudhudit.artook.apputils.modules.profile.ProfileCounts
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppConstants
import com.hudhudit.artook.apputils.modules.chat.UserChatModel
import com.hudhudit.artook.apputils.remote.utill.Resource
import com.hudhudit.artook.views.main.comments.CommentsFragment
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.main.notifications.NotificationsFragment
import com.hudhudit.artook.views.main.profile.adapters.MyPostsAdapter
import com.hudhudit.artook.views.main.profile.edit.EditProfileFragment
import com.hudhudit.artook.views.main.profile.followers.FollowersFragment
import com.hudhudit.artook.views.main.profile.following.FollowingFragment
import com.hudhudit.artook.databinding.FragmentProfileBinding
import com.hudhudit.artook.views.main.chats.ChatAdapter
import com.hudhudit.artook.views.main.chats.ChatViewModel
import com.hudhudit.artook.views.main.chats.ChatsFragment
import com.hudhudit.artook.views.main.chats.conversation.ConversationFragment
import com.hudhudit.artook.views.main.newpost.UploadMediaNewPostFragment
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    lateinit var binding: FragmentProfileBinding
    lateinit var mainActivity: MainActivity
    var posts: ArrayList<Post> = ArrayList()
    var page = 1
    var isFeeds = true

    private val viewModel by viewModels<ChatViewModel>()
    var listChatModel: MutableList<UserChatModel>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_profile, container, false)
        binding = FragmentProfileBinding.inflate(layoutInflater)
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
        onClick()
        getOpenedNotifications()
        setData()
        getProfileCounts()
        mainActivity.visibleBottomBar()
        AppDefs.media1Uri = null
        AppDefs.media2Uri = null
        AppDefs.media3Uri = null
        AppDefs.media4Uri = null
        AppDefs.media5Uri = null
        AppDefs.media6Uri = null
        getChat()
    }

    private fun onClick(){
        binding.toolbarLayout.navigateBack.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
        binding.toolbarLayout.newPost.setOnClickListener { navigateToNewPost() }
        binding.profileBtn.setOnClickListener { navigateToEditProfile() }
        binding.followersLayout.setOnClickListener { navigateToMyFollowers() }
        binding.followingLayout.setOnClickListener { navigateToMyFollowings() }
        binding.postsNSV.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                page++
                if (isFeeds){
                    getFeeds()
                }else{
                    getSaved()
                }
            }
        }
        binding.feedLayout.setOnClickListener {
            posts.clear()
            isFeeds = true
            page = 1
            Glide.with(mainActivity).load(resources.getDrawable(R.drawable.selected_feeds)).into(binding.feedIcon)
            binding.feedTxt.setTextColor(resources.getColor(R.color.black))
            Glide.with(mainActivity).load(resources.getDrawable(R.drawable.unselected_saved)).into(binding.savedIcon)
            binding.savedTxt.setTextColor(resources.getColor(R.color.gray))
            getFeeds()
        }
        binding.savedLayout.setOnClickListener {
            posts.clear()
            isFeeds = false
            page = 1
            Glide.with(mainActivity).load(resources.getDrawable(R.drawable.selected_save_post)).into(binding.savedIcon)
            binding.savedTxt.setTextColor(resources.getColor(R.color.black))
            Glide.with(mainActivity).load(resources.getDrawable(R.drawable.unselected_feeds)).into(binding.feedIcon)
            binding.feedTxt.setTextColor(resources.getColor(R.color.gray))
            getSaved()
        }
        binding.toolbarLayout.notifications.setOnClickListener { navigateToNotifications() }
        binding.refreshLayout.setOnRefreshListener {
            getOpenedNotifications()
            setData()
            getProfileCounts()
            binding.refreshLayout.isRefreshing = false
        }
        binding.chatsBtn.setOnClickListener {
            openChat()
        }

    }

    private fun openChat(){
        val fragment: Fragment = ChatsFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("chat")
        fragmentTransaction.commit()
    }

    private fun setData(){
        if (AppDefs.isFeed){
            posts.clear()
            isFeeds = true
            page = 1
            Glide.with(mainActivity).load(resources.getDrawable(R.drawable.selected_feeds)).into(binding.feedIcon)
            binding.feedTxt.setTextColor(resources.getColor(R.color.black))
            Glide.with(mainActivity).load(resources.getDrawable(R.drawable.unselected_saved)).into(binding.savedIcon)
            binding.savedTxt.setTextColor(resources.getColor(R.color.gray))
            getFeeds()
        }else{
            posts.clear()
            isFeeds = false
            page = 1
            Glide.with(mainActivity).load(resources.getDrawable(R.drawable.selected_saved)).into(binding.savedIcon)
            binding.savedTxt.setTextColor(resources.getColor(R.color.black))
            Glide.with(mainActivity).load(resources.getDrawable(R.drawable.unselected_feeds)).into(binding.feedIcon)
            binding.feedTxt.setTextColor(resources.getColor(R.color.gray))
            getSaved()
        }
        if (mainActivity.hasBack){
            binding.toolbarLayout.navigateBack.visibility = View.VISIBLE
        }else{
            binding.toolbarLayout.navigateBack.visibility = View.GONE
        }
        binding.fullName.text = AppDefs.user.results!!.name
        binding.userName.text = AppDefs.user.results!!.user_name
        if (AppDefs.user.results!!.bio.isEmpty()){
            binding.bio.visibility = View.GONE
        }else{
            binding.bio.visibility = View.VISIBLE
            binding.bio.text = AppDefs.user.results!!.bio
        }

        if (!AppDefs.user.results!!.image.isNullOrEmpty()){
            Glide.with(mainActivity).load(AppDefs.user.results!!.image).into(binding.userImage)
        }
    }

    private fun getOpenedNotifications(){
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
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    if (response.body()!!.results!!){
                        binding.toolbarLayout.notificationsBadge.visibility = View.VISIBLE
                    }else{
                        binding.toolbarLayout.notificationsBadge.visibility = View.INVISIBLE
                    }
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun getProfileCounts(){
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
        val profileCountsCall: Call<ProfileCounts> =
            retrofit.create(RetrofitAPIs::class.java).getProfileCounts(AppDefs.user.results!!.id)
        profileCountsCall.enqueue(object : Callback<ProfileCounts> {
            override fun onResponse(call: Call<ProfileCounts>, response: Response<ProfileCounts>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    binding.followersCount.text = response.body()!!.results.followers
                    binding.followingCount.text = response.body()!!.results.following
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<ProfileCounts>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun getFeeds(){
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
            retrofit.create(RetrofitAPIs::class.java).getMyPosts(page.toString())
        postsCallCall.enqueue(object : Callback<PostsResult> {
            override fun onResponse(call: Call<PostsResult>, response: Response<PostsResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (post in response.body()!!.results!!.data){
                        posts.add(post)
                    }
                    setPostsRV()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
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

    private fun getSaved(){
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
        val postsCall: Call<PostsResult> =
            retrofit.create(RetrofitAPIs::class.java).getSavedPosts(page.toString())
        postsCall.enqueue(object : Callback<PostsResult> {
            override fun onResponse(call: Call<PostsResult>, response: Response<PostsResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (post in response.body()!!.results!!.data){
                        posts.add(post)
                    }
                    setPostsRV()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                    binding.noPosts.visibility = View.VISIBLE
                    binding.noPosts.text = resources.getString(R.string.no_saved_posts_yet)
                    binding.postsNSV.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<PostsResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun setPostsRV(){
        binding.noPosts.visibility = View.GONE
        binding.postsNSV.visibility = View.VISIBLE
        val adapter = MyPostsAdapter(this, posts)
        binding.myPostsRV.adapter = adapter
        binding.myPostsRV.layoutManager = GridLayoutManager(mainActivity, 3)
    }

    fun openPost(post: Post){
        AppDefs.isFeed = isFeeds
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

    private fun navigateToEditProfile(){
        val fragment: Fragment = EditProfileFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("EditProfile")
        fragmentTransaction.commit()
    }

    private fun navigateToMyFollowers(){
        val fragment: Fragment = FollowersFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("clientId", AppDefs.user.results!!.id)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("Followers")
        fragmentTransaction.commit()
    }

    private fun navigateToMyFollowings(){
        val fragment: Fragment = FollowingFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("clientId", AppDefs.user.results!!.id)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("Followings")
        fragmentTransaction.commit()
    }

    private fun navigateToNotifications(){
        val fragment: Fragment = NotificationsFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("Notifications")
        fragmentTransaction.commit()
    }
    private fun getChat() {
        viewModel.getChat(AppDefs.user.results!!.id)
        viewModel.chatStatus.observe(viewLifecycleOwner, Observer {
            if(it != null){
                if (it!!.status == Resource.Status.SUCCESS) {
                    listChatModel=it.data!!.toMutableList()

                   listChatModel!!.filter { AppDefs.user.results!!.id == it.userLoginId}.forEach {
                        it.countMassage
                        Log.d("countmes",it.toString())
                        //Log.d("countmes", it.countMassage.toString())


                    }
                    var x=getCount(listChatModel!!)
                    if (x>0){
                        binding.statusRed.visibility=View.VISIBLE
                    }else{
                        binding.statusRed.visibility=View.GONE
                    }
                    Log.d("countmes",x.toString())


                 /*   if (AppDefs.user.results!!.id == item.userOwnerItemId) {
                        if (item.countMassage == "0"){
                            binding.statusRed.visibility=View.INVISIBLE
                        }else{
                            binding.statusRed.visibility=View.VISIBLE

                        }

                    }*/

                }
                if (it!!.status == Resource.Status.ERROR) {
                    Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
            viewModel.reset()
        })

    }
    fun getCount(listChatModel1:MutableList<UserChatModel>):Int{
        var msgCount=0
        listChatModel1!!.forEach {
            if (AppDefs.user.results!!.id == it.userLoginId){
                msgCount+=it.countMessageOwnerItem!!.toInt()
            }else{
                msgCount+=it.countMassage!!.toInt()
            }

        }
        return msgCount
    }

    private fun navigateToNewPost(){
        val fragment: Fragment = UploadMediaNewPostFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("NewPost")
        fragmentTransaction.commit()
    }
}