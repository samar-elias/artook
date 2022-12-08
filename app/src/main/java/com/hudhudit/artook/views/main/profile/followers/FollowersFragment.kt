package com.hudhudit.artook.views.main.profile.followers

import android.content.Context
import android.os.Bundle
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
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.profile.Follower
import com.hudhudit.artook.apputils.modules.profile.FollowersResult
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.main.profile.followers.adapters.FollowersAdapter
import com.hudhudit.artook.views.main.profile.userprofile.UserProfileFragment
import com.hudhudit.artook.databinding.FragmentFollowersBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class FollowersFragment : Fragment() {

    lateinit var binding: FragmentFollowersBinding
    lateinit var mainActivity: MainActivity
    var followers: ArrayList<Follower> = ArrayList()
    var page = 1
    var clientId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_followers, container, false)
        binding = FragmentFollowersBinding.inflate(layoutInflater)
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
        getFollowers()
        followers.clear()
    }

    private fun init(){
        binding.toolbarLayout.title.text = resources.getString(R.string.followers)
        clientId = requireArguments().getString("clientId")!!
        mainActivity.visibleBottomBar()
        if (AppDefs.lang == "ar"){
            binding.toolbarLayout.navigateBack.scaleX = (-1).toFloat()
        }else{
            binding.toolbarLayout.navigateBack.scaleX = (1).toFloat()
        }
    }

    private fun onClick(){
        binding.toolbarLayout.navigateBack.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
        binding.followersNSV.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                page++
                getFollowers()
            }
        }
    }

    private fun getFollowers(){
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
        val followersCall: Call<FollowersResult> =
            retrofit.create(RetrofitAPIs::class.java).getFollowers(clientId, page.toString())
        followersCall.enqueue(object : Callback<FollowersResult> {
            override fun onResponse(call: Call<FollowersResult>, response: Response<FollowersResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (follower in response.body()!!.results.data) {
                        followers.add(follower)
                    }
                    setFollowersRV()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                    binding.toolbarLayout.title.text = "0 "+resources.getString(R.string.followers)
                    binding.noFollowers.visibility = View.VISIBLE
                    binding.followersRV.visibility = View.GONE

                }
            }

            override fun onFailure(call: Call<FollowersResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun setFollowersRV(){
        binding.toolbarLayout.title.text = followers.size.toString()+" "+resources.getString(R.string.followers)
        if (followers.size == 0){
            binding.noFollowers.visibility = View.VISIBLE
            binding.followersRV.visibility = View.GONE
        }else{
            binding.noFollowers.visibility = View.GONE
            binding.followersRV.visibility = View.VISIBLE
            val adapter = FollowersAdapter(this, followers)
            binding.followersRV.adapter = adapter
            binding.followersRV.layoutManager = LinearLayoutManager(mainActivity)
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
}