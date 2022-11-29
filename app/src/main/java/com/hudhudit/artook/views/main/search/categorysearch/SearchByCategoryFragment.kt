package com.hudhudit.artook.views.main.search.categorysearch

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.search.Category
import com.hudhudit.artook.apputils.modules.user.SearchUser
import com.hudhudit.artook.apputils.modules.user.SearchUsersResult
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.main.profile.userprofile.UserProfileFragment
import com.hudhudit.artook.databinding.FragmentSearchByCategoryBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class SearchByCategoryFragment : Fragment() {

    lateinit var binding: FragmentSearchByCategoryBinding
    lateinit var mainActivity: MainActivity
    var searchUsers: ArrayList<SearchUser> = ArrayList()
    var page = 1
    lateinit var category: Category
    var getData = true
    var searchText = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_search_by_category, container, false)
        binding = FragmentSearchByCategoryBinding.inflate(layoutInflater)
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
        search("")
        searchUsers.clear()
    }

    private fun init(){
        category = requireArguments().getParcelable("category")!!
        binding.toolbarLayout.title.text = resources.getString(R.string.search_in)+" "+category.title
        mainActivity.visibleBottomBar()
        if (AppDefs.lang == "ar"){
            binding.toolbarLayout.navigateBack.scaleX = (-1).toFloat()
        }else{
            binding.toolbarLayout.navigateBack.scaleX = (1).toFloat()
        }
    }

    private fun onClick(){
        binding.toolbarLayout.navigateBack.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
        binding.resultsNSV.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                page++
                search(searchText)
            }
        }
        binding.searchEdt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (s.isEmpty()){
                    if (getData){
                        searchUsers.clear()
                        search("")
                        searchText = ""
                    }
                }
                if (s.length >= 3){
                    if (getData){
                        searchUsers.clear()
                        search(s.toString())
                        searchText = s.toString()
                    }
                }
            }
        })
    }

    private fun search(search: String){
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
        val searchCall: Call<SearchUsersResult> =
            retrofit.create(RetrofitAPIs::class.java).search(page.toString(), category.id, search)
        searchCall.enqueue(object : Callback<SearchUsersResult> {
            override fun onResponse(call: Call<SearchUsersResult>, response: Response<SearchUsersResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (user in response.body()!!.results.data){
                        searchUsers.add(user)
                    }
                    getData = true
                    setUsersRV()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SearchUsersResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
//                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setUsersRV(){
        if (searchUsers.size == 0){
            binding.noResults.visibility = View.VISIBLE
            binding.resultsNSV.visibility = View.GONE
        }else{
            binding.noResults.visibility = View.GONE
            binding.resultsNSV.visibility = View.VISIBLE
        }
        val adapter = SearchAdapter(this, searchUsers)
        binding.resultsRV.adapter = adapter
        binding.resultsRV.layoutManager = LinearLayoutManager(mainActivity)
    }

    fun navigateToMyUserProfile(id: String){
        mainActivity.hideKeyboard()
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