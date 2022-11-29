package com.hudhudit.artook.views.main.competition.details

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.apputils.modules.competition.Contest
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.views.main.competition.participate.ParticipateFragment
import com.hudhudit.artook.views.main.competition.vote.VoteFragment
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.databinding.FragmentCompetitionDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.file.Files

@AndroidEntryPoint
class CompetitionDetailsFragment : Fragment() {

    lateinit var binding: FragmentCompetitionDetailsBinding
    lateinit var mainActivity: MainActivity
    lateinit var contest: Contest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_competition_details, container, false)
        binding = FragmentCompetitionDetailsBinding.inflate(layoutInflater)
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
        checkUserParticipation()
    }

    private fun init(){
        binding.toolbarLayout.title.text = resources.getString(R.string.details)
        contest = requireArguments().getParcelable("contest")!!
        setData()
        if (AppDefs.lang == "ar"){
            binding.toolbarLayout.navigateBack.scaleX = (-1).toFloat()
        }else{
            binding.toolbarLayout.navigateBack.scaleX = (1).toFloat()
        }
    }

    private fun onClick(){
        binding.toolbarLayout.navigateBack.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
        binding.participate.setOnClickListener { navigateToParticipate() }
        binding.vote.setOnClickListener { navigateToVote() }
    }

    private fun checkUserParticipation(){
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
            retrofit.create(RetrofitAPIs::class.java).checkUserParticipation(contest.id)
        reportCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    if (response.body()!!.results!!){
                        binding.participate.background = resources.getDrawable(R.drawable.purple_blue_gradient_background)
                        binding.participate.setTextColor(resources.getColor(R.color.white))
                        binding.participate.isEnabled = true
                    }else{
                        binding.participate.background = resources.getDrawable(R.drawable.gray_raduis)
                        binding.participate.setTextColor(resources.getColor(R.color.light_gray))
                        binding.participate.isEnabled = false
                    }
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                    binding.participate.background = resources.getDrawable(R.drawable.gray_raduis)
                    binding.participate.setTextColor(resources.getColor(R.color.light_gray))
                    binding.participate.isEnabled = false

                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun setData(){
        binding.competitionTitle.text = contest.title
        binding.description.text = contest.description
        binding.competitionDate.text = contest.date
        if (contest.image.isNotEmpty()){
            Glide.with(mainActivity).load(contest.image).into(binding.contestImage)
        }
        if (contest.flag == "1"){
            binding.vote.visibility = View.GONE
            binding.participate.visibility = View.VISIBLE
        }else{
            binding.vote.visibility = View.VISIBLE
            binding.participate.visibility = View.GONE
        }
    }

    private fun navigateToParticipate(){
        val fragment: Fragment = ParticipateFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("contestId", contest.id)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("Participate")
        fragmentTransaction.commit()
    }

    private fun navigateToVote(){
        val fragment: Fragment = VoteFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putParcelable("contest", contest)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("Vote")
        fragmentTransaction.commit()
    }
}