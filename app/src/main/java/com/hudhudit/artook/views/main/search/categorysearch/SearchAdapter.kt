package com.hudhudit.artook.views.main.search.categorysearch

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hudhudit.artook.apputils.modules.user.SearchUser
import com.hudhudit.artook.R
import de.hdodenhof.circleimageview.CircleImageView

class SearchAdapter(
    private var searchByCategoryFragment: SearchByCategoryFragment,
    private var users: ArrayList<SearchUser>
) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.search_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val user = users[position]

        holder.userName.text = user.user_name
        holder.fullName.text = user.name
        if (user.image_client.isNotEmpty()){
            Glide.with(context!!).load(user.image_client).into(holder.userImg)
        }

        holder.itemView.setOnClickListener { searchByCategoryFragment.navigateToMyUserProfile(user.client_id) }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userImg: CircleImageView = itemView.findViewById(R.id.user_img)
        var userName: TextView = itemView.findViewById(R.id.user_name)
        var fullName: TextView = itemView.findViewById(R.id.full_name)
    }

}