package com.hudhudit.artook.apputils.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dominate.talabyeh.app.core.base.BaseSwipeRefreshLayout

import kotlin.properties.Delegates

abstract class BaseAdapter<M, V : BaseViewHolder<M>> @JvmOverloads constructor(
    var items: MutableList<M>? = mutableListOf()
) : RecyclerView.Adapter<V>() {

    private var onItemClickListener: ((view: View, position: Int, item: M?) -> Unit)? = null
    private var onEmptyPageActionClickListener: (() -> Unit)? = null

    var animationDuration: Long = DEFAULT_DURATION
    var animationDelta: Float = DEFAULT_DELTA

    var emptyPageEnabled: Boolean = false
        set(value) {
            notifyDataSetChanged()
            field = value
        }

    protected var context: Context by Delegates.notNull()
    protected var inflater: LayoutInflater by Delegates.notNull()

    protected var refreshLayout: BaseSwipeRefreshLayout? = null
    protected var recyclerView: RecyclerView? = null
    private var animationEnabled: Boolean = false

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        context = recyclerView.context
        inflater = LayoutInflater.from(context)

        if (recyclerView.parent is BaseSwipeRefreshLayout) {
            refreshLayout = recyclerView.parent as BaseSwipeRefreshLayout
        }

        handleAnimations()
        refreshLayout?.isRefreshing = false
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
        refreshLayout = null
    }

    private fun handleAnimations() {
        if (animationEnabled) {
            val manager = recyclerView?.layoutManager
            if (manager is GridLayoutManager) {
                handleGridAnimations(manager)
            } else if (manager is LinearLayoutManager) {
                handleLinearAnimations(manager)
            }
            if ((items?.size ?: 0) > 0) {
                recyclerView?.post {
                    recyclerView?.scheduleLayoutAnimation()
                }
            }
        }
    }

    private fun handleGridAnimations(manager: GridLayoutManager?) {
        val animationSet = if (manager?.orientation == GridLayoutManager.VERTICAL) {
            getBottomAnimationSet()
        } else {
            getEndAnimationSet()
        }
        val controller = AnimationUtils.loadLayoutAnimation(context, android.R.anim.fade_in)
        controller.animation = animationSet
        recyclerView?.layoutAnimation = controller
    }

    private fun handleLinearAnimations(manager: LinearLayoutManager?) {
        val animationSet = if (manager?.orientation == GridLayoutManager.VERTICAL) {
            getBottomAnimationSet()
        } else {
            getEndAnimationSet()
        }
        val controller = AnimationUtils.loadLayoutAnimation(context, android.R.anim.fade_in)
        controller.animation = animationSet
        recyclerView?.layoutAnimation = controller
    }

    private fun getBottomAnimationSet(): AnimationSet {
        val animationSet = AnimationSet(true)
        animationSet.addAnimation(getTranslateAnimation(0f, 0f, animationDelta, 0f))
        animationSet.addAnimation(getAlphaAnimation())

        return animationSet
    }

    private fun getEndAnimationSet(): AnimationSet {
        val animationSet = AnimationSet(true)
        animationSet.addAnimation(getTranslateAnimation(animationDelta, 0f, 0f, 0f))
        animationSet.addAnimation(getAlphaAnimation())
        return animationSet
    }

    private fun getAlphaAnimation(): Animation {
        val alphaAnimation = AlphaAnimation(0f, 1f)
        alphaAnimation.interpolator = OvershootInterpolator()
        alphaAnimation.duration = animationDuration
        return alphaAnimation
    }

    private fun getTranslateAnimation(
        fromXDelta: Float,
        toXDelta: Float,
        fromYDelta: Float,
        toYDelta: Float
    ): Animation {
        val translateAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, fromXDelta,
            Animation.RELATIVE_TO_PARENT, toXDelta,
            Animation.RELATIVE_TO_PARENT, fromYDelta,
            Animation.RELATIVE_TO_PARENT, toYDelta
        )
        translateAnimation.interpolator = OvershootInterpolator()
        translateAnimation.duration = animationDuration
        return translateAnimation
    }

    fun setOnItemClickListener(onItemClickListener: (view: View, position: Int, item: M?) -> Unit) {
        this.onItemClickListener = onItemClickListener
    }

    fun getOnItemClickListener(): ((view: View, position: Int, item: M?) -> Unit)? {
        return onItemClickListener
    }

    fun setOnEmptyPageActionClickListener(onEmptyPageActionClickListener: () -> Unit) {
        this.onEmptyPageActionClickListener = onEmptyPageActionClickListener
    }

    fun getOnEmptyPageActionClickListener(): (() -> Unit)? {
        return onEmptyPageActionClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): V = getViewHolder(parent, viewType)

    override fun getItemCount(): Int = if ((items == null || items?.size == 0) && emptyPageEnabled) {
        1
    } else {
        items?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        if ((items == null || items?.size == 0) && emptyPageEnabled) {
            return EMPTY_TYPE
        }
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: V, position: Int) {
        if (onItemClickListener != null) {
            handleClickListener(holder)
        }

        bind(holder, position)

    }

    open fun bind(holder: V, position: Int) {
        holder.bind(position, items?.getOrNull(position))
    }

    private fun handleClickListener(holder: V) {
        holder.itemView.setOnClickListener { view ->
            val clickedPosition = holder.adapterPosition
            handleOnItemClicked(view, clickedPosition)
        }
    }

    protected fun handleOnItemClicked(view: View, clickedPosition: Int) {
        if (clickedPosition != RecyclerView.NO_POSITION) {
            onItemClickListener?.invoke(view, clickedPosition, items?.get(clickedPosition))
        }
    }

    protected fun disableAnimations() {
        animationEnabled = false
    }

    abstract fun getViewHolder(parent: ViewGroup, viewType: Int): V

    open fun add(item: M) {
        items?.let { items ->
            items.add(item)
            notifyItemInserted(items.size)
        }
    }

    open fun add(position: Int, item: M) {
        items?.let { items ->
            items.add(position, item)
            notifyItemInserted(position)
        }
    }

    open fun add(itemsToAdd: MutableList<M>) {
        handleRefreshLayout()
        if ((items?.size ?: 0) == 0) {
            submitList(itemsToAdd)
            return
        }
        this.items?.let { items ->
            val oldSize = itemCount
            items.addAll(itemsToAdd)
            notifyItemRangeInserted(oldSize, itemsToAdd.size)
        }
    }

    private fun handleRefreshLayout() {
        if (refreshLayout?.isRefreshing == true) {
            items?.clear()
            refreshLayout?.isRefreshing = false
        }
    }

    private fun submitList(items: MutableList<M>) {
        this.items = items
        notifyDataSetChanged()
        if (animationEnabled) {
            recyclerView?.scheduleLayoutAnimation()
        }
    }

    fun get(position: Int): M? {
        return items?.getOrNull(position)
    }

    open fun add(position: Int, itemsToAdd: MutableList<M>) {
        this.items?.let { items ->
            val oldSize = itemCount
            items.addAll(position, itemsToAdd)
            notifyItemRangeInserted(oldSize, itemsToAdd.size)
        }
    }

    fun remove(item: M) {
        this.items?.let { items ->
            val position = items.indexOf(item)
            if (position > -1) {
                items.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    fun remove(position: Int) {
        this.items?.let { items ->
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun remove(itemsToAdd: MutableList<M>) {
        this.items?.let { items ->
            val oldSize = itemCount
            items.removeAll(itemsToAdd)
            notifyItemRangeRemoved(oldSize, itemsToAdd.size)
        }
    }

    fun reset() {
        this.items?.clear()
        notifyDataSetChanged()
    }

    fun reset(items: MutableList<M>?) {
        this.items = items
        notifyDataSetChanged()
    }

    private companion object {

        private const val DEFAULT_DELTA = 0.5f
        private const val DEFAULT_DURATION = 600L

        const val EMPTY_TYPE = 5213
    }
}