package com.hudhudit.artook.views.main.competition.vote

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hudhudit.artook.apputils.modules.competition.Participant
import com.hudhudit.artook.R
import de.hdodenhof.circleimageview.CircleImageView

class ParticipantsAdapter(
    private var voteFragment: VoteFragment,
    private var participants: ArrayList<Participant>
) :
    RecyclerView.Adapter<ParticipantsAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.participant_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val participant = participants[position]

        holder.participantName.text = participant.name
        if (participant.imageClient.isNotEmpty()){
            Glide.with(context!!).load(participant.imageClient).into(holder.participantImage)
        }
        if (participant.image.isNotEmpty()){
            Glide.with(context!!).load(participant.image).into(holder.contestImage)
        }
        if (participant.is_vote == "1"){
            holder.vote.background = context!!.resources.getDrawable(R.drawable.purple_blue_gradient_background)
            holder.vote.text = participant.count_vote+" "+context!!.resources.getString(R.string.votes)
        }else{
            holder.vote.background = context!!.resources.getDrawable(R.drawable.white_15_stroke1)
            holder.vote.text = context!!.resources.getString(R.string.vote)
        }

        holder.itemView.setOnClickListener { voteFragment.viewParticipantDetails(participant) }

        holder.vote.setOnClickListener {
            if (participant.is_vote == "0"){
                voteFragment.vote(participant)
            }
        }
    }

    override fun getItemCount(): Int {
        return participants.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var contestImage: ImageView = itemView.findViewById(R.id.contest_image)
        var participantImage: CircleImageView = itemView.findViewById(R.id.participant_img)
        var participantName: TextView = itemView.findViewById(R.id.participate_name)
        var vote: TextView = itemView.findViewById(R.id.vote)

    }

}