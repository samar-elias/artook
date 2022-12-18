package com.hudhudit.artook.views.main.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.modules.notification.Notification
import com.hudhudit.artook.apputils.modules.notification.NotificationsResult
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.modules.competition.PreviousContest
import com.hudhudit.artook.apputils.modules.post.Post
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.main.notifications.adapters.NotificationsAdapter
import com.hudhudit.artook.databinding.FragmentNotificationsBinding
import com.hudhudit.artook.views.main.comments.CommentsFragment
import com.hudhudit.artook.views.main.competition.previouscompetition.PreviousCompetitionDetailsFragment
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
import kotlin.collections.ArrayList

@AndroidEntryPoint
class NotificationsFragment : Fragment() {

    lateinit var binding: FragmentNotificationsBinding
    lateinit var mainActivity: MainActivity
    lateinit var adapter : NotificationsAdapter
    var notifications: ArrayList<Notification> = ArrayList()
    var page = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_notifications, container, false)
        binding = FragmentNotificationsBinding.inflate(layoutInflater)
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
        getNotifications()
        notifications.clear()
        page = 1
    }

    private fun init(){
        binding.toolbarLayout.title.text = resources.getString(R.string.notifications)
        mainActivity.visibleBottomBar()
        if (AppDefs.lang == "ar"){
            binding.toolbarLayout.navigateBack.scaleX = (-1).toFloat()
        }else{
            binding.toolbarLayout.navigateBack.scaleX = (1).toFloat()
        }
    }

    private fun onClick(){
        binding.toolbarLayout.navigateBack.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
        binding.notificationsNCS.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                page++
                getNotifications()
            }
        }
    }

    private fun getNotifications(){
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
        val notificationsCall: Call<NotificationsResult> =
            retrofit.create(RetrofitAPIs::class.java).getNotifications(page.toString())
        notificationsCall.enqueue(object : Callback<NotificationsResult> {
            override fun onResponse(call: Call<NotificationsResult>, response: Response<NotificationsResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    if (response.body()!!.results != null){
                        for (notification in response.body()!!.results!!.data){
                            notifications.add(notification)
                        }
                        updateDisplay()
                    }
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<NotificationsResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
//                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
                if (notifications.size == 0){
                    binding.notificationsNCS.visibility = View.GONE
                    binding.noNotifications.visibility = View.VISIBLE
                }
            }

        })
    }

    private fun setNotificationsRV(){
        binding.notificationsNCS.visibility = View.VISIBLE
        binding.noNotifications.visibility = View.GONE
        adapter = NotificationsAdapter(this, notifications)
        binding.notificationsRV.adapter = adapter
        binding.notificationsRV.layoutManager = LinearLayoutManager(mainActivity)
    }

    fun deleteNotification(id: String){
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
        val notificationsCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).deleteNotification(id)
        notificationsCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                if (response.isSuccessful){
                    page = 1
                    notifications.clear()
                    getNotifications()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
//                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
                binding.notificationsNCS.visibility = View.GONE
                binding.noNotifications.visibility = View.VISIBLE
            }

        })
    }

    fun readNotification(notification: Notification){
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
        val notificationsCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).readNotification(notification.id)
        notificationsCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                if (response.isSuccessful){
                    if (notification.data_id != "0"){
                        openNotification(notification)
                    }else{
                        notifications.clear()
                        getNotifications()
                    }
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
//                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
                binding.notificationsNCS.visibility = View.GONE
                binding.noNotifications.visibility = View.VISIBLE
            }

        })
    }

    fun openNotification(notification: Notification){
        when (notification.page_id) {
            "profile" -> {
                navigateToMyUserProfile(notification.data_id)
            }
            "post" -> {
                openPost(notification.data_id)
            }
            "contests" -> {
                openPreviousContest(notification.data_id)
            }
        }
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

    private fun openPost(id: String) {
        val fragment: Fragment = CommentsFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("postId", id)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("Post")
        fragmentTransaction.commit()
    }

    private fun openPreviousContest(id: String){
        val fragment: Fragment = PreviousCompetitionDetailsFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("previousContest", id)
        bundle.putInt("position", 0)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("Details")
        fragmentTransaction.commit()
    }

    fun updateDisplay() {
        for (notification in notifications){
            notification.convertedTime = covertTimeToText(notification.time)!!
        }
        setNotificationsRV()
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            @SuppressLint("NotifyDataSetChanged")
            override fun run() {
                Handler(Looper.getMainLooper()).post(Runnable {
                    for (notification in notifications){
                        notification.convertedTime = covertTimeToText(notification.time)!!
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