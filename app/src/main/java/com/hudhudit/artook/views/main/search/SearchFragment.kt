package com.hudhudit.artook.views.main.search

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
import com.hudhudit.artook.apputils.modules.search.Categories
import com.hudhudit.artook.apputils.modules.search.Category
import com.hudhudit.artook.apputils.modules.user.SearchUser
import com.hudhudit.artook.apputils.modules.user.SearchUsersResult
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.main.profile.userprofile.UserProfileFragment
import com.hudhudit.artook.views.main.search.adapters.CategoriesAdapter
import com.hudhudit.artook.views.main.search.adapters.SearchAdapter
import com.hudhudit.artook.views.main.search.categorysearch.SearchByCategoryFragment
import com.hudhudit.artook.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class SearchFragment : Fragment() {

    lateinit var binding: FragmentSearchBinding
    lateinit var mainActivity: MainActivity
    var searchUsers: ArrayList<SearchUser> = ArrayList()
    var categories: ArrayList<Category> = ArrayList()
    var page = 1
    var categoryId = ""
    var getData = true
    var searchText = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_search, container, false)
        binding = FragmentSearchBinding.inflate(layoutInflater)
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
        getCategories()
        mainActivity.visibleBottomBar()
        searchUsers.clear()
    }

    private fun onClick(){
        binding.searchEdt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (s.isEmpty()){
                    binding.categoriesTitle.visibility = View.VISIBLE
                    setCategoriesRV()
                }
                if (s.length >= 3){
                    if (getData){
                        binding.categoriesTitle.visibility = View.GONE
                        searchUsers.clear()
                        search(s.toString())
                        searchText = s.toString()
                    }
                }
            }
        })
        binding.resultsNSV.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                page++
                search(searchText)
            }
        }
    }

    private fun getCategories(){
        categories.clear()
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
        val categoriesCall: Call<Categories> =
            retrofit.create(RetrofitAPIs::class.java).getCategories()
        categoriesCall.enqueue(object : Callback<Categories> {
            override fun onResponse(call: Call<Categories>, response: Response<Categories>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (category in response.body()!!.results){
                        categories.add(category)
                    }
                    setCategoriesRV()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Categories>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setCategoriesRV(){
        val adapter = CategoriesAdapter(this, categories)
        binding.resultsRV.adapter = adapter
        binding.resultsRV.layoutManager = LinearLayoutManager(mainActivity)
    }

    private fun search(search: String){
        getData = false
        binding.categoriesTitle.visibility = View.GONE
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
            retrofit.create(RetrofitAPIs::class.java).search(page.toString(), categoryId, search)
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

    fun navigateToMySearchByCategory(category: Category){
        mainActivity.hideKeyboard()
        val fragment: Fragment = SearchByCategoryFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putParcelable("category", category)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("SearchByCategory")
        fragmentTransaction.commit()
    }
}