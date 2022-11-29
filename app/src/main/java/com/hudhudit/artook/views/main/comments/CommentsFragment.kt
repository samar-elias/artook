package com.hudhudit.artook.views.main.comments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.BuildConfig
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.modules.post.*
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.databinding.FragmentCommentsBinding
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.main.comments.adapters.CommentsAdapter
import com.hudhudit.artook.views.main.comments.adapters.PostMediaAdapter
import com.hudhudit.artook.views.main.profile.ProfileFragment
import com.hudhudit.artook.views.main.profile.userprofile.UserProfileFragment
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class CommentsFragment : Fragment() {

    lateinit var binding: FragmentCommentsBinding
    lateinit var mainActivity: MainActivity
    lateinit var adapter: CommentsAdapter
    var comments: ArrayList<Comment> = ArrayList()
    lateinit var post: Post
    var postId: String? = null
    var page = 1
    var isMyPost = false
    var isOptionsOpen = false
    var position = 0
    var videoPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_comments, container, false)
        binding = FragmentCommentsBinding.inflate(layoutInflater)
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
        comments.clear()
        val mSnapHelper: SnapHelper = PagerSnapHelper()
        mSnapHelper.attachToRecyclerView(binding.postLayout.mediaRV)
        binding.toolbarLayout.title.text = resources.getString(R.string.comments)
        postId = requireArguments().getString("postId")

        if (postId != null){
            getPostById(postId!!)
        }else{
            isMyPost = post.client_id == AppDefs.user.results!!.id
            if (isMyPost){
                binding.postLayout.optionsLayout.visibility = View.GONE
                binding.postLayout.report.visibility = View.GONE
                binding.postLayout.options.visibility = View.VISIBLE
            }else{
                binding.postLayout.optionsLayout.visibility = View.GONE
                binding.postLayout.report.visibility = View.VISIBLE
                binding.postLayout.options.visibility = View.GONE
            }
            setData()
            comments.clear()
            getComments()
        }

        mainActivity.invisibleBottomBar()
    }

    @SuppressLint("NewApi")
    private fun onClick(){
        if (AppDefs.lang == "ar"){
            binding.toolbarLayout.navigateBack.scaleX = (-1).toFloat()
        }else{
            binding.toolbarLayout.navigateBack.scaleX = (1).toFloat()
        }
        binding.toolbarLayout.navigateBack.setOnClickListener {
            mainActivity.supportFragmentManager.popBackStack()
            mainActivity.visibleBottomBar()
        }
        binding.commentsNSV.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                page++
                getComments()
            }
        }
        binding.sendComment.setOnClickListener {
            val comment = binding.commentEdt.text.toString()
            if (comment.isNotEmpty()){
                binding.commentEdt.setText("")
                mainActivity.hideKeyboard()
                addComment(post.id, comment)
            }
        }
        binding.postLayout.options.setOnClickListener {
            if (isOptionsOpen){
                binding.postLayout.optionsLayout.visibility = View.GONE
                isOptionsOpen = false
            }else{
                binding.postLayout.optionsLayout.visibility = View.VISIBLE
                isOptionsOpen = true
            }
        }
        binding.postLayout.like.setOnClickListener {
            if (post.is_like == "0"){
                likePost(post.id)
                post.is_like = "1"
                Glide.with(mainActivity).load(R.drawable.liked).into(binding.postLayout.likeIcon)
            }else{
                unlikePost(post.id)
                post.is_like = "0"
                Glide.with(mainActivity).load(R.drawable.like).into(binding.postLayout.likeIcon)
            }
        }
        binding.postLayout.save.setOnClickListener {
            if (post.is_save == "0"){
                savePost(post.id)
                post.is_save = "1"
                Glide.with(mainActivity).load(R.drawable.selected_save_post).into(binding.postLayout.save)
            }else{
                unSavePost(post.id)
                post.is_save = "0"
                Glide.with(mainActivity).load(R.drawable.save_post).into(binding.postLayout.save)
            }
        }
        binding.postLayout.report.setOnClickListener { reportPopUp(post.id) }
        binding.postLayout.delete.setOnClickListener { deletePopUp(post.id) }
        binding.postLayout.edit.setOnClickListener { updatePopUp(post) }
        binding.postLayout.share.setOnClickListener { shareApp() }
        binding.postLayout.userInfoLayout.setOnClickListener { navigateToMyUserProfile(post.client_id) }
        binding.postLayout.userImg.setOnClickListener { navigateToMyUserProfile(post.client_id) }
        binding.playVideo.setOnClickListener {
            if (binding.fullVideo.isPlaying){
                Glide.with(mainActivity).load(R.drawable.play).into(binding.playIcon)
                binding.fullVideo.pause()
            }else{
                Glide.with(mainActivity).load(R.drawable.pause).into(binding.playIcon)
                binding.fullVideo.start()
            }
        }
        binding.close.setOnClickListener {
            binding.fullVideo.pause()
            binding.fullLayout.visibility = View.GONE
        }
    }

    private fun setData(){
        if (post.image_client.isNotEmpty()){
            Glide.with(mainActivity).load(post.image_client).into(binding.postLayout.userImg)
        }
        binding.postLayout.userName.text = post.name
        binding.postLayout.postDate.text = post.time+" "+post.date
        binding.postLayout.likesCount.text = post.number_posts_like
        binding.postLayout.commentCount.text = post.number_posts_comments
        binding.postLayout.postCaption.text = post.description
        if (post.is_like == "1"){
            Glide.with(mainActivity).load(R.drawable.liked).into(binding.postLayout.likeIcon)
        }else{
            Glide.with(mainActivity).load(R.drawable.like).into(binding.postLayout.likeIcon)
        }

        if (post.is_save == "1"){
            Glide.with(mainActivity).load(R.drawable.selected_save_post).into(binding.postLayout.save)
        }else{
            Glide.with(mainActivity).load(R.drawable.save_post).into(binding.postLayout.save)
        }

        val adapter = PostMediaAdapter(this, post.posts_media)
        val layoutManager =
            LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.postLayout.mediaRV.adapter = adapter
        binding.postLayout.mediaRV.layoutManager = layoutManager

        binding.postLayout.mediaRV.viewTreeObserver
            .addOnScrollChangedListener {
                binding.postLayout.mediaCounter.text = ((binding.postLayout.mediaRV.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()+1).toString()+"/"+post.posts_media.size
            }


    }



    private fun setMediaRV(){
        val adapter = PostMediaAdapter(this, post.posts_media)
        val layoutManager =
            LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL, false)
        binding.postLayout.mediaRV.adapter = adapter
        binding.postLayout.mediaRV.layoutManager = layoutManager
    }

    private fun getComments(){
        binding.progressBar.visibility = View.VISIBLE
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", AppDefs.user.token!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val commentsCall: Call<CommentsResult> =
            retrofit.create(RetrofitAPIs::class.java).getPostComments(page.toString(), post.id)
        commentsCall.enqueue(object : Callback<CommentsResult> {
            override fun onResponse(call: Call<CommentsResult>, response: Response<CommentsResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (comment in response.body()!!.results.data){
                        comments.add(comment)
                    }
                    updateDisplay()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CommentsResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
//                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun setCommentsRV(){
        adapter = CommentsAdapter(this, comments)
        binding.commentsRV.adapter = adapter
        binding.commentsRV.layoutManager = LinearLayoutManager(mainActivity)
    }

//    private fun setCommentsRV2(){
//        val fragment: Fragment = CommentsFragment()
//        val adapter = CommentsAdapter(fragment as CommentsFragment, comments)
//        binding.commentsRV.adapter = adapter
//    }

    private fun likePost(postId: String){
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
        val likeCall: Call<CountsResult> =
            retrofit.create(RetrofitAPIs::class.java).likePost(postId)
        likeCall.enqueue(object : Callback<CountsResult> {
            override fun onResponse(call: Call<CountsResult>, response: Response<CountsResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    binding.postLayout.likesCount.text = response.body()!!.results.number_posts_like
                    binding.postLayout.commentCount.text = response.body()!!.results.number_posts_comments
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CountsResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun unlikePost(postId: String){
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
        val likeCall: Call<CountsResult> =
            retrofit.create(RetrofitAPIs::class.java).unlikePost(postId)
        likeCall.enqueue(object : Callback<CountsResult> {
            override fun onResponse(call: Call<CountsResult>, response: Response<CountsResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    binding.postLayout.likesCount.text = response.body()!!.results.number_posts_like
                    binding.postLayout.commentCount.text = response.body()!!.results.number_posts_comments
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CountsResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun savePost(postId: String){
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
        val saveCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).savePost(postId)
        saveCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){

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

    private fun unSavePost(postId: String){
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
        val saveCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).unSavePost(postId)
        saveCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){

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

    private fun reportPopUp(postId: String){
        val alertView: View =
            LayoutInflater.from(context).inflate(R.layout.report_popup, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.setCancelable(false)

        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val reportEdt: TextInputEditText = alertView.findViewById(R.id.report_edt)
        val submit: MaterialButton = alertView.findViewById(R.id.submit)
        val close: ImageView = alertView.findViewById(R.id.close)

        close.setOnClickListener { alertBuilder.dismiss() }
        submit.setOnClickListener {
            val note = reportEdt.text.toString()
            if (note.isEmpty()){
                Toast.makeText(mainActivity, resources.getString(R.string.report_note), Toast.LENGTH_SHORT).show()
            }else{
                reportPost(postId, note)
                alertBuilder.dismiss()
            }
        }

    }

    private fun reportPost(postId: String, note: String){
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
        val reportCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).reportPost(postId, note)
        reportCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    Toast.makeText(mainActivity, resources.getString(R.string.reported_successfully), Toast.LENGTH_SHORT).show()
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

    private fun deletePopUp(postId: String){
        val alertView: View =
            LayoutInflater.from(context).inflate(R.layout.delete_post_popup, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.setCancelable(false)

        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val delete: MaterialButton = alertView.findViewById(R.id.delete)
        val close: ImageView = alertView.findViewById(R.id.close)

        close.setOnClickListener { alertBuilder.dismiss() }
        delete.setOnClickListener {
            deletePost(postId)
            alertBuilder.dismiss()
        }

    }

    private fun deletePost(postId: String){
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
        val reportCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).deletePost(postId)
        reportCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    Toast.makeText(mainActivity, resources.getString(R.string.deleted_successfully), Toast.LENGTH_SHORT).show()
                    mainActivity.supportFragmentManager.popBackStack()
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

    private fun updatePopUp(post: Post){
        val alertView: View =
            LayoutInflater.from(context).inflate(R.layout.edit_post_popup, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.setCancelable(false)

        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val descriptionEdt: TextInputEditText = alertView.findViewById(R.id.description_edt)
        val save: MaterialButton = alertView.findViewById(R.id.save)
        val close: ImageView = alertView.findViewById(R.id.close)

        descriptionEdt.setText(post.description)
        close.setOnClickListener { alertBuilder.dismiss() }
        save.setOnClickListener {
            val description = descriptionEdt.text.toString()
            if (description.isEmpty()){
                Toast.makeText(mainActivity, resources.getString(R.string.enter_description), Toast.LENGTH_SHORT).show()
            }else{
                updatePost(post.id, description)
                alertBuilder.dismiss()
            }
        }

    }

    private fun updatePost(postId: String, description: String){
        binding.progressBar.visibility = View.VISIBLE
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", AppDefs.user.token!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val reportCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).updatePost(description, postId)
        reportCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                if (response.isSuccessful){
                    getPostById()
                }else{
                    binding.progressBar.visibility = View.GONE
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

    private fun getPostById(){
        binding.progressBar.visibility = View.VISIBLE
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", AppDefs.user.token!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val reportCall: Call<PostResult> =
            retrofit.create(RetrofitAPIs::class.java).getPostById(post.id)
        reportCall.enqueue(object : Callback<PostResult> {
            override fun onResponse(call: Call<PostResult>, response: Response<PostResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    post = response.body()!!.results
                    setData()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<PostResult>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<PostResult>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<PostResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun getPostById(id: String){
        binding.progressBar.visibility = View.VISIBLE
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", AppDefs.user.token!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val reportCall: Call<PostResult> =
            retrofit.create(RetrofitAPIs::class.java).getPostById(id)
        reportCall.enqueue(object : Callback<PostResult> {
            override fun onResponse(call: Call<PostResult>, response: Response<PostResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    post = response.body()!!.results
                    isMyPost = post.client_id == AppDefs.user.results!!.id
                    if (isMyPost){
                        binding.postLayout.optionsLayout.visibility = View.GONE
                        binding.postLayout.report.visibility = View.GONE
                        binding.postLayout.options.visibility = View.VISIBLE
                    }else{
                        binding.postLayout.optionsLayout.visibility = View.GONE
                        binding.postLayout.report.visibility = View.VISIBLE
                        binding.postLayout.options.visibility = View.GONE
                    }
                    setData()
                    comments.clear()
                    getComments()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<PostResult>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<PostResult>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<PostResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun addComment(postId: String, comment: String){
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
        val addCommentCall: Call<CountsResult> =
            retrofit.create(RetrofitAPIs::class.java).addComment(postId, comment)
        addCommentCall.enqueue(object : Callback<CountsResult> {
            override fun onResponse(call: Call<CountsResult>, response: Response<CountsResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    binding.postLayout.likesCount.text = response.body()!!.results.number_posts_like
                    binding.postLayout.commentCount.text = response.body()!!.results.number_posts_comments
                    comments.clear()
                    page = 1
                    getComments()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<CountsResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    fun deleteCommentPopUp(commentId: String){
        val alertView: View =
            LayoutInflater.from(context).inflate(R.layout.delete_popup, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()

        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val deleteComment: MaterialButton = alertView.findViewById(R.id.delete)

        deleteComment.setOnClickListener {
            deleteComment(commentId)
            alertBuilder.dismiss()
        }
    }

    private fun deleteComment(commentId: String){
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
        val deleteCommentCall: Call<CountsResult> =
            retrofit.create(RetrofitAPIs::class.java).deleteComment(post.id, commentId)
        deleteCommentCall.enqueue(object : Callback<CountsResult> {
            override fun onResponse(call: Call<CountsResult>, response: Response<CountsResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    binding.postLayout.likesCount.text = response.body()!!.results.number_posts_like
                    binding.postLayout.commentCount.text = response.body()!!.results.number_posts_comments
                    comments.clear()
                    page = 1
                    getComments()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<CountsResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    fun navigateToMyUserProfile(id: String){
        val fragment: Fragment = UserProfileFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("clientId", id)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("UserProfile")
        fragmentTransaction.commit()
    }

    fun navigateToMyProfile(){
        AppDefs.isFeed = true
        mainActivity.hasBack = true
        val fragment: Fragment = ProfileFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("Profile")
        fragmentTransaction.commit()
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Wash @ Home")
            var shareMessage = ""
            shareMessage = """${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            Log.d("er", e.message!!)
        }
    }

    fun openFullMedia(media: String){
        binding.fullLayout.visibility = View.VISIBLE
        if (media.endsWith(".mp4")){
            binding.fullImage.visibility = View.GONE
            binding.videoLayout.visibility = View.VISIBLE
            try {
                val video: Uri = Uri.parse(media)
                binding.fullVideo.setVideoURI(video)
            } catch (e: Exception) {
            }
        }else{
            binding.videoLayout.visibility = View.GONE
            binding.fullImage.visibility = View.VISIBLE
            Glide.with(mainActivity).load(media).into(binding.fullImage)
        }
    }

    fun updateDisplay() {
        for (comment in comments){
            comment.convertedTime = covertTimeToText(comment.time)!!
        }
        setCommentsRV()
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            @SuppressLint("NotifyDataSetChanged")
            override fun run() {
                Handler(Looper.getMainLooper()).post(Runnable {
                    for (comment in comments){
                        comment.convertedTime = covertTimeToText(comment.time)!!
                    }
                    adapter.notifyDataSetChanged()
                })
            }
        }, 1000, 20000) //Update text every 10 second


    }

    fun covertTimeToText(dataDate: String?): String? {
        var convTime: String? = null
        val prefix = ""
        val suffix = "Ago"
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val pasTime = dateFormat.parse(dataDate)
            val nowTime = Date()
            val dateDiff = nowTime.time - pasTime.time
            val second = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
            val minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
            val hour = TimeUnit.MILLISECONDS.toHours(dateDiff)
            val day = TimeUnit.MILLISECONDS.toDays(dateDiff)
            if (second < 60) {
                convTime = "$second Seconds $suffix"
            } else if (minute < 60) {
                convTime = "$minute Minutes $suffix"
            } else if (hour < 24) {
                convTime = "$hour Hours $suffix"
            } else if (day >= 7) {
                convTime = if (day > 360) {
                    (day / 360).toString() + " Years " + suffix
                } else if (day > 30) {
                    (day / 30).toString() + " Months " + suffix
                } else {
                    (day / 7).toString() + " Week " + suffix
                }
            } else if (day < 7) {
                convTime = "$day Days $suffix"
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.e("ConvTimeE", e.message!!)
        }
        return convTime
    }
}
