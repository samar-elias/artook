package com.hudhudit.artook.views.main.competition.previouscompetition

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.modules.competition.*
import com.hudhudit.artook.views.main.competition.previouscompetition.adapters.WinnersAdapter
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.databinding.FragmentPreviousCompetitionDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class PreviousCompetitionDetailsFragment : Fragment() {

    lateinit var binding: FragmentPreviousCompetitionDetailsBinding
    lateinit var mainActivity: MainActivity
    lateinit var previousContestId: String
    lateinit var previousContest: PreviousContest
    var position: Int = 0
    var page = 1
    var winners: ArrayList<Winner> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_previous_competition_details, container, false)
        binding = FragmentPreviousCompetitionDetailsBinding.inflate(layoutInflater)
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
        binding.toolbarLayout.title.text = resources.getString(R.string.details)
        previousContestId = requireArguments().getString("previousContest")!!
        position = requireArguments().getInt("position")
        getContestById(previousContestId)
    }

    private fun onClick(){
        binding.toolbarLayout.navigateBack.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
        binding.close.setOnClickListener { binding.detailsLayout.visibility = View.GONE }
    }

    private fun getContestById(id: String){
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
        val previousContestsCall: Call<PreviousContestResult> =
            retrofit.create(RetrofitAPIs::class.java).getContestById(id)
        previousContestsCall.enqueue(object : Callback<PreviousContestResult> {
            override fun onResponse(call: Call<PreviousContestResult>, response: Response<PreviousContestResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    previousContest = response.body()!!.results
                    setData()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PreviousContestResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun setData(){
        binding.competitionTitle.text = previousContest.title
        binding.competitionDate.text = previousContest.date
        if (previousContest.image.isNotEmpty()){
            Glide.with(mainActivity).load(previousContest.image).into(binding.contestImage)
        }
        if (position == 0){
            getWinners("1")
        }else{
            getWinners("10")
        }
    }

    private fun getWinners(noWinners: String){
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
        val winnersCall: Call<WinnersResult> =
            retrofit.create(RetrofitAPIs::class.java).getWinners(previousContest.id, page.toString(), noWinners)
        winnersCall.enqueue(object : Callback<WinnersResult> {
            override fun onResponse(call: Call<WinnersResult>, response: Response<WinnersResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (winner in response.body()!!.results.data){
                        winners.add(winner)
                    }
                    setWinnersRV()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WinnersResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun setWinnersRV(){
        val adapter = WinnersAdapter(this, winners)
        binding.winnersRV.adapter = adapter
        binding.winnersRV.layoutManager = GridLayoutManager(mainActivity, 3)
    }

    fun viewWinnerDetails(winner: Winner){
        binding.detailsLayout.visibility = View.VISIBLE
        if (winner.image_client.isNotEmpty()){
            Glide.with(mainActivity).load(winner.image_client).into(binding.userImg)
        }
        if (winner.image.isNotEmpty()){
            Glide.with(mainActivity).load(winner.image).into(binding.fullContestImage)
        }
        binding.userName.text = winner.name
    }
}