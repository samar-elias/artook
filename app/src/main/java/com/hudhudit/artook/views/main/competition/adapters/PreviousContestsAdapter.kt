package com.hudhudit.artook.views.main.competition.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hudhudit.artook.apputils.modules.competition.PreviousContest
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.competition.CompetitionFragment

class PreviousContestsAdapter(
    private var competitionFragment: CompetitionFragment,
    private var previousContests: ArrayList<PreviousContest>
) :
    RecyclerView.Adapter<PreviousContestsAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.previous_contest_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val previousContest = previousContests[position]

        holder.contestTitle.text = previousContest.title

        if (previousContest.image.isNotEmpty()){
            Glide.with(context!!).load(previousContest.image).into(holder.contestImage)
        }

        holder.itemView.setOnClickListener { competitionFragment.navigateToPreviousContestDetails(previousContest, position) }
    }

    override fun getItemCount(): Int {
        return previousContests.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var contestImage: ImageView = itemView.findViewById(R.id.contest_image)
        var contestTitle: TextView = itemView.findViewById(R.id.contest_title)
    }

}