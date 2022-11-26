package com.hudhudit.artook.apputils.base

import androidx.databinding.ViewDataBinding

abstract class BaseBindingViewHolder<M>(viewDataBinding: ViewDataBinding) : BaseViewHolder<M>(viewDataBinding.root) {

    var viewDataBinding: ViewDataBinding? = viewDataBinding


    @Suppress("UNCHECKED_CAST")
    inline fun <T : ViewDataBinding> bind(binding: T.() -> Unit) {
        binding(viewDataBinding as T)

            viewDataBinding?.executePendingBindings()
    }


}
