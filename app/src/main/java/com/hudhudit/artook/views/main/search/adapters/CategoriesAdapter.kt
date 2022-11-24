package com.hudhudit.artook.views.main.search.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hudhudit.artook.apputils.modules.search.Category
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.search.SearchFragment

class CategoriesAdapter(
    private var searchFragment: SearchFragment,
    private var categories: ArrayList<Category>
) :
    RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {
    var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.category_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val category = categories[position]

        holder.categoryTitle.text = category.title

        holder.itemView.setOnClickListener { searchFragment.navigateToMySearchByCategory(category) }
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var categoryTitle: TextView = itemView.findViewById(R.id.category_title)
    }

}