package com.hudhudit.artook.views.main.competition.previouscompetition.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hudhudit.artook.apputils.modules.competition.Winner
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.competition.previouscompetition.PreviousCompetitionDetailsFragment
import de.hdodenhof.circleimageview.CircleImageView

class WinnersAdapter(
    private var previousCompetitionDetailsFragment: PreviousCompetitionDetailsFragment,
    private var winners: ArrayList<Winner>
) :
    RecyclerView.Adapter<WinnersAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.winner_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val winner = winners[position]

        holder.participantName.text = (position+1).toString()+". "+winner.name
        holder.participantPrize.text = winner.price+" JD"
        if (winner.image.isNotEmpty()){
            Glide.with(context!!).load(winner.image).into(holder.contestImage)
        }
        if (winner.image_client.isNotEmpty()){
            Glide.with(context!!).load(winner.image_client).into(holder.participantImg)
        }

        holder.itemView.setOnClickListener { previousCompetitionDetailsFragment.viewWinnerDetails(winner) }
    }

    override fun getItemCount(): Int {
        return winners.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var contestImage: ImageView = itemView.findViewById(R.id.contest_image)
        var participantImg: CircleImageView = itemView.findViewById(R.id.participant_img)
        var participantName: TextView = itemView.findViewById(R.id.participate_name)
        var participantPrize: TextView = itemView.findViewById(R.id.participate_prize)
    }

}