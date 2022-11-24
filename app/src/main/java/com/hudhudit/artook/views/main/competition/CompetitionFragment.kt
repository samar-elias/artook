package com.hudhudit.artook.views.main.competition

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.modules.competition.Contest
import com.hudhudit.artook.apputils.modules.competition.ContestResult
import com.hudhudit.artook.apputils.modules.competition.PreviousContest
import com.hudhudit.artook.apputils.modules.competition.PreviousContestsResult
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.competition.adapters.PreviousContestsAdapter
import com.hudhudit.artook.views.main.competition.details.CompetitionDetailsFragment
import com.hudhudit.artook.views.main.competition.participate.ParticipateFragment
import com.hudhudit.artook.views.main.competition.previouscompetition.PreviousCompetitionDetailsFragment
import com.hudhudit.artook.views.main.competition.vote.VoteFragment
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.databinding.FragmentCompetitionBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class CompetitionFragment : Fragment() {

    lateinit var binding: FragmentCompetitionBinding
    lateinit var mainActivity: MainActivity
    lateinit var contest: Contest
    var previousContests: ArrayList<PreviousContest> = ArrayList()
    var page = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_competition, container, false)
        binding = FragmentCompetitionBinding.inflate(layoutInflater)
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
        getCurrentContest()
        previousContests.clear()
        page = 1
        getPreviousContests()

    }

    private fun onClick(){
        binding.details.setOnClickListener { navigateToContestDetails() }
        binding.contestNSV.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                page++
                getPreviousContests()
            }
        }
        binding.participate.setOnClickListener { navigateToParticipate() }
        binding.vote.setOnClickListener { navigateToVote() }
    }

    private fun getCurrentContest(){
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
        val contestCall: Call<ContestResult> =
            retrofit.create(RetrofitAPIs::class.java).getContest()
        contestCall.enqueue(object : Callback<ContestResult> {
            override fun onResponse(call: Call<ContestResult>, response: Response<ContestResult>) {
                if (response.isSuccessful){
                    contest = response.body()!!.results
                    setData()
                    checkUserParticipation()
                }else{
                    binding.progressBar.visibility = View.GONE
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ContestResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun setData(){
        binding.contestTitle.text = contest.title
        binding.contestDescription.text = contest.description
        binding.firstWinnerPrize.text = contest.first_prize+" JD"
        binding.secondWinnerPrize.text = contest.secand_prize+" JD"
        binding.thirdWinnerPrize.text = contest.third_prize+" JD"
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
        convertToMilliSeconds(contest.date_time)
    }

    private fun getPreviousContests(){
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
        val previousContestsCall: Call<PreviousContestsResult> =
            retrofit.create(RetrofitAPIs::class.java).getPreviousContests(page.toString())
        previousContestsCall.enqueue(object : Callback<PreviousContestsResult> {
            override fun onResponse(call: Call<PreviousContestsResult>, response: Response<PreviousContestsResult>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (previousContest in response.body()!!.results.data){
                        previousContests.add(previousContest)
                    }
                    setPreviousContestsRV()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PreviousContestsResult>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun setPreviousContestsRV(){
        val adapter = PreviousContestsAdapter(this, previousContests)
        binding.previousContestsRV.adapter = adapter
        binding.previousContestsRV.layoutManager = GridLayoutManager(mainActivity, 3)
    }

    @SuppressLint("NewApi")
    private fun convertToMilliSeconds(date: String) {
        val newDate = date.replace("/", " ")
        val formatter = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss", Locale.ENGLISH)
        val localDate = LocalDateTime.parse(newDate, formatter)
        val timeInMilliseconds = localDate.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
        Log.d("timeDate", timeInMilliseconds.toString())
        val c = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyy MM dd HH:mm:ss", Locale.ENGLISH)
        val formattedDate = df.format(c)
        val formatterCurrent = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss", Locale.ENGLISH)
        val localDateCurrent = LocalDateTime.parse(formattedDate, formatterCurrent)
        val timeInMillisecondsCurrent =
            localDateCurrent.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
        Log.d("timeDate", timeInMillisecondsCurrent.toString())
        startCountDown(timeInMilliseconds - timeInMillisecondsCurrent)
    }

    private fun startCountDown(daysIMillis: Long) {

        object : CountDownTimer(daysIMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                /*            converting the milliseconds into days, hours, minutes and seconds and displaying it in textviews             */
                val s = millisUntilFinished / 1000
                val m = s / 60
                val h = m / 60
                val d = h / 24
                binding.days.text = d.toString()
                binding.hours.text = (h % 24).toString()
                binding.minutes.text = (m % 60).toString()
                binding.seconds.text = (s % 60).toString()
            }

            override fun onFinish() {
                binding.days.text = "0"
                binding.hours.text = "00"
                binding.minutes.text = "00"
                binding.seconds.text = "00"
            }
        }.start()
    }

    private fun navigateToContestDetails(){
        val fragment: Fragment = CompetitionDetailsFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putParcelable("contest", contest)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("Details")
        fragmentTransaction.commit()
    }

    fun navigateToPreviousContestDetails(previousContest: PreviousContest, position: Int){
        val fragment: Fragment = PreviousCompetitionDetailsFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putParcelable("previousContest", previousContest)
        bundle.putInt("position", position)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("Details")
        fragmentTransaction.commit()
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
}