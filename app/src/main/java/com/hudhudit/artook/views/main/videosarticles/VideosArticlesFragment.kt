package com.hudhudit.artook.views.main.videosarticles

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.modules.videosarticles.*
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.databinding.FragmentVideosArticlesBinding
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.main.newpost.UploadMediaNewPostFragment
import com.hudhudit.artook.views.main.notifications.NotificationsFragment
import com.hudhudit.artook.views.main.videosarticles.adapters.ArticlesAdapter
import com.hudhudit.artook.views.main.videosarticles.adapters.VideosAdapter
import com.hudhudit.artook.views.main.videosarticles.articledetails.ArticleDetailsFragment
import com.hudhudit.artook.views.video.FullVideoActivity
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.ceil
import kotlin.math.roundToInt

@AndroidEntryPoint
class VideosArticlesFragment : Fragment() {

    lateinit var binding: FragmentVideosArticlesBinding
    lateinit var mainActivity: MainActivity
    var articles: ArrayList<Article> = ArrayList()
    var videos: ArrayList<Video> = ArrayList()
    var videosGroups: ArrayList<VideosGroup> = ArrayList()
    var page = 1
    var searchPage = 1
    var isVideos = true
    var getData = true
    var search = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_videos_articles, container, false)
        binding = FragmentVideosArticlesBinding.inflate(layoutInflater)
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
        mainActivity.isHome = false
        onClick()
        getOpenedNotifications()
        getCounts()
        mainActivity.visibleBottomBar()
        if (getData){
            if (videos.size == 0){
                videos.clear()
                videosGroups.clear()
                getVideos()
            }
            if (isVideos){
                binding.articles.alpha = 0.5F
                binding.videos.alpha = 1F
            }else{
                binding.articles.alpha = 1F
                binding.videos.alpha = 0.5F
            }
        }
        AppDefs.media1Uri = null
        AppDefs.media2Uri = null
        AppDefs.media3Uri = null
        AppDefs.media4Uri = null
        AppDefs.media5Uri = null
        AppDefs.media6Uri = null
    }

    private fun onClick(){
        binding.articles.setOnClickListener {
            articles.clear()
            binding.articles.alpha = 1F
            binding.videos.alpha = 0.5F
            page = 1
            isVideos = false
            getArticles()
            if (search.isNotEmpty()){
                search = ""
                binding.searchEdt.setText("")
            }
        }
        binding.videos.setOnClickListener {
            videos.clear()
            videosGroups.clear()
            binding.articles.alpha = 0.5F
            binding.videos.alpha = 1F
            page = 1
            isVideos = true
            getVideos()
            if (search.isNotEmpty()){
                search = ""
                binding.searchEdt.setText("")
            }
        }
        binding.dataNSV.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                page++
                if (isVideos){
                    getVideos()
                }else{
                    getArticles()
                }
            }
        }
        binding.toolbarLayout.notifications.setOnClickListener { navigateToNotifications() }
        binding.toolbarLayout.newPost.setOnClickListener { navigateToNewPost() }
        binding.searchEdt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (s.isEmpty()){
                    page = 1
                    if (getData){
                        if (isVideos){
                            videos.clear()
                            videosGroups.clear()
                            getVideos()
                        }else{
                            articles.clear()
                            getArticles()
                        }
                    }
                }
                if (s.length >= 3){
                    if (getData){
                        searchPage = 1
                        if (isVideos){
                            videos.clear()
                            videosGroups.clear()
                            search = s.toString()
                            searchVideos()
                        }else{
                            articles.clear()
                            search = s.toString()
                            searchArticles()
                        }
                    }
                }
            }
        })
        binding.refreshLayout.setOnRefreshListener {
            getOpenedNotifications()
            getCounts()
            videos.clear()
            articles.clear()
            if (getData){
                if (isVideos){
                    getVideos()
                }else{
                    getArticles()
                }
            }
            binding.refreshLayout.isRefreshing = false
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

    private fun getCounts(){
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
        val countsCall: Call<VideosArticlesCountResult> =
            retrofit.create(RetrofitAPIs::class.java).getVideosArticlesCount()
        countsCall.enqueue(object : Callback<VideosArticlesCountResult> {
            override fun onResponse(call: Call<VideosArticlesCountResult>, response: Response<VideosArticlesCountResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    binding.videosCount.text = response.body()!!.results.no_video+" "+resources.getString(R.string.video)
                    binding.articlesCount.text = response.body()!!.results.no_articles+" "+resources.getString(R.string.article)
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VideosArticlesCountResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getArticles(){
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
        val articlesCall: Call<ArticlesResults> =
            retrofit.create(RetrofitAPIs::class.java).getArticles(page.toString())
        articlesCall.enqueue(object : Callback<ArticlesResults> {
            override fun onResponse(call: Call<ArticlesResults>, response: Response<ArticlesResults>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (article in response.body()!!.results!!.data){
                        articles.add(article)
                    }
                    setArticlesRV()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ArticlesResults>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun setArticlesRV(){
        val adapter = ArticlesAdapter(this, articles)
        binding.dataRV.adapter = adapter
        binding.dataRV.layoutManager = LinearLayoutManager(mainActivity)
    }

    private fun getVideos(){
        getData = false
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
        val articlesCall: Call<VideosResults> =
            retrofit.create(RetrofitAPIs::class.java).getVideos(page.toString())
        articlesCall.enqueue(object : Callback<VideosResults> {
            override fun onResponse(call: Call<VideosResults>, response: Response<VideosResults>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    val videosArr = response.body()!!.results!!.data
                    val videosGroup = VideosGroup(videosArr)
                    videosGroups.add(videosGroup)
                    for (video in response.body()!!.results!!.data){
                        videos.add(video)
                    }
                    setVideosRV()
                    getData = true
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VideosResults>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun setVideosRV(){
        val adapter = VideosAdapter(this, videosGroups)
        binding.dataRV.adapter = adapter
        binding.dataRV.layoutManager = LinearLayoutManager(mainActivity)
    }

    private fun searchArticles(){
        getData = false
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
        val articlesCall: Call<ArticlesResults> =
            retrofit.create(RetrofitAPIs::class.java).searchArticles(searchPage.toString(), search)
        articlesCall.enqueue(object : Callback<ArticlesResults> {
            override fun onResponse(call: Call<ArticlesResults>, response: Response<ArticlesResults>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (article in response.body()!!.results!!.data){
                        articles.add(article)
                    }
                    setArticlesRV()
                    getData = true
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                    if (errorResponse.results == null){
                        setArticlesRV()
                    }
                }
            }

            override fun onFailure(call: Call<ArticlesResults>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun searchVideos(){
        getData = false
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
        val articlesCall: Call<VideosResults> =
            retrofit.create(RetrofitAPIs::class.java).searchVideos(searchPage.toString(), search)
        articlesCall.enqueue(object : Callback<VideosResults> {
            override fun onResponse(call: Call<VideosResults>, response: Response<VideosResults>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    val videosArr = response.body()!!.results!!.data
                    val videosGroup = VideosGroup(videosArr)
                    videosGroups.add(videosGroup)
                    for (video in response.body()!!.results!!.data){
                        videos.add(video)
                    }
                    setVideosRV()
                    getData = true
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                    if (errorResponse.results == null){
                        setVideosRV()
                    }
                }
            }

            override fun onFailure(call: Call<VideosResults>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    fun openVideo(video: Video){
        val videoIntent = Intent(mainActivity, FullVideoActivity::class.java)
        videoIntent.putExtra("video", video)
        startActivity(videoIntent)
//        val fragment: Fragment = FullVideoFragment()
//        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
//        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
//        val bundle = Bundle()
//        bundle.putParcelable("video", video)
//        fragment.arguments = bundle
//        fragmentTransaction.replace(R.id.container, fragment)
//        fragmentTransaction.addToBackStack("Video")
//        fragmentTransaction.commit()
    }

    private fun navigateToNotifications(){
        val fragment: Fragment = NotificationsFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("Notifications")
        fragmentTransaction.commit()
    }

    fun readMore(article: Article){
         val fragment: Fragment = ArticleDetailsFragment()
         val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
         val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
         val bundle = Bundle()
         bundle.putParcelable("article", article)
         fragment.arguments = bundle
         fragmentTransaction.replace(R.id.container, fragment)
         fragmentTransaction.addToBackStack("ArticleDetails")
         fragmentTransaction.commit()
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