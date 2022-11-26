package com.hudhudit.artook.apputils.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<M>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(position: Int, item: M?)

    fun adapterPosition(callback: (Int) -> Unit) {
        val position = adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            callback(position)
        }
    }
}