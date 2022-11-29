package com.hudhudit.artook.views.main.competition.vote

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
import com.hudhudit.artook.apputils.modules.competition.*
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.databinding.FragmentVoteBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class VoteFragment : Fragment() {

    lateinit var binding: FragmentVoteBinding
    lateinit var mainActivity: MainActivity
    lateinit var contest: Contest
    var participants: ArrayList<Participant>  = ArrayList()
    var page = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_vote, container, false)
        binding = FragmentVoteBinding.inflate(layoutInflater)
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
        binding.toolbarLayout.title.text = resources.getString(R.string.vote)
        contest = requireArguments().getParcelable("contest")!!
        setData()
        getParticipants()
        if (AppDefs.lang == "ar"){
            binding.toolbarLayout.navigateBack.scaleX = (-1).toFloat()
        }else{
            binding.toolbarLayout.navigateBack.scaleX = (1).toFloat()
        }
    }

    private fun onClick(){
        binding.toolbarLayout.navigateBack.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
        binding.close.setOnClickListener { binding.detailsLayout.visibility = View.GONE }
    }

    private fun setData(){
        binding.competitionTitle.text = contest.title
        binding.description.text = contest.description
        binding.competitionDate.text = contest.date
        if (contest.image.isNotEmpty()){
            Glide.with(mainActivity).load(contest.image).into(binding.contestImage)
        }
    }

    private fun getParticipants(){
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
        val participantsCall: Call<ParticipantsResult> =
            retrofit.create(RetrofitAPIs::class.java).getContestParticipants(contest.id, page.toString())
        participantsCall.enqueue(object : Callback<ParticipantsResult> {
            override fun onResponse(call: Call<ParticipantsResult>, response: Response<ParticipantsResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (participant in response.body()!!.results.data){
                        participants.add(participant)
                    }
                    setParticipantsRV()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ParticipantsResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun setParticipantsRV(){
        val adapter = ParticipantsAdapter(this, participants)
        binding.participantsRV.adapter = adapter
        binding.participantsRV.layoutManager = GridLayoutManager(mainActivity, 3)
    }

    fun viewParticipantDetails(participant: Participant){
        binding.detailsLayout.visibility = View.VISIBLE
        binding.description.text = participant.description
        binding.userName.text = participant.name
        if (participant.imageClient.isNotEmpty()){
            Glide.with(mainActivity).load(participant.imageClient).into(binding.userImg)
        }
        if (participant.image.isNotEmpty()){
            Glide.with(mainActivity).load(participant.image).into(binding.fullContestImage)
        }
        if (participant.is_vote == "1"){
            binding.vote.background = resources.getDrawable(R.drawable.purple_blue_gradient_background)
            binding.vote.text = participant.count_vote+" "+resources.getString(R.string.votes)
        }else{
            binding.vote.background = resources.getDrawable(R.drawable.white_15_stroke1)
            binding.vote.text = resources.getString(R.string.vote)
        }

        binding.vote.setOnClickListener {
            if (participant.is_vote == "0"){
                vote(participant)
            }
        }
    }

    fun vote(participant: Participant){
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
        val voteCall: Call<VoteResult> =
            retrofit.create(RetrofitAPIs::class.java).vote(participant.id)
        voteCall.enqueue(object : Callback<VoteResult> {
            override fun onResponse(call: Call<VoteResult>, response: Response<VoteResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    participant.is_vote = "1"
                    participant.count_vote = response.body()!!.results.count_vote
                    if (binding.detailsLayout.visibility == View.VISIBLE){
                        binding.vote.text = response.body()!!.results.count_vote+" "+resources.getString(R.string.votes)
                        binding.vote.background = resources.getDrawable(R.drawable.purple_blue_gradient_background)
                    }
                    setParticipantsRV()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VoteResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }
}