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
import com.hudhudit.artook.apputils.modules.competition.Contest
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.competition.participate.ParticipateFragment
import com.hudhudit.artook.views.main.competition.vote.VoteFragment
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.databinding.FragmentCompetitionDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

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
    }

    private fun init(){
        binding.toolbarLayout.title.text = resources.getString(R.string.details)
        contest = requireArguments().getParcelable("contest")!!
        setData()
    }

    private fun onClick(){
        binding.toolbarLayout.navigateBack.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
        binding.participate.setOnClickListener { navigateToParticipate() }
        binding.vote.setOnClickListener { navigateToVote() }
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