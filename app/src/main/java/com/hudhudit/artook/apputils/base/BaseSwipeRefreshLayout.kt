package com.dominate.talabyeh.app.core.base

import android.content.Context
import android.util.AttributeSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class BaseSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

    private val listeners: MutableList<(() -> Unit)> by lazy { mutableListOf<(() -> Unit)>() }

    fun addOnRefreshListener(listener: (() -> Unit)) {
        if (listeners.size == 0) {
            super.setOnRefreshListener {
                listeners.forEach {
                    it()
                }
            }
        }
        listeners.add(listener)
    }

    fun removedOnRefreshListener(listener: (() -> Unit)) {
        listeners.remove(listener)
    }

    @Deprecated(
        message = "Use addOnRefreshListener(listener) instead.",
        replaceWith = ReplaceWith("addOnRefreshListener(listener)"),
        level = DeprecationLevel.ERROR
    )
    override fun setOnRefreshListener(listener: OnRefreshListener?) {
    }
}