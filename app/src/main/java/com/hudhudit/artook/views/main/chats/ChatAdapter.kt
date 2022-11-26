package com.hudhudit.artook.views.main.chats

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide


import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.getTimeAgo
import com.hudhudit.artook.apputils.base.BaseAdapter
import com.hudhudit.artook.apputils.base.BaseBindingViewHolder
import com.hudhudit.artook.apputils.modules.chat.UserChatModel
import com.hudhudit.artook.databinding.RowConversationBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class ChatAdapter(
    private var viewModel: MutableList<UserChatModel>,

    private val onclickListener: ((UserChatModel?) -> Unit),


    ) : BaseAdapter<UserChatModel, ChatAdapter.ViewHolder>(viewModel) {
    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowConversationBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    inner class ViewHolder(val binding: RowConversationBinding) : BaseBindingViewHolder<UserChatModel>(binding) {
        @SuppressLint("ResourceAsColor")
        override fun bind(position: Int, item: UserChatModel?) {
            bind<RowConversationBinding> {
                this.modual = item

                var date = getTimeAgo(item!!.date!!.toLong(), context)
                if (!date.isNullOrEmpty()){
                    binding.time.text = date
                }else{
                    binding.time.text=context.resources.getString(R.string.just_now)
                }

                 if (item!!.userOwnerItemId!!.equals(AppDefs.user.results!!.id)) {
                    binding.userName.text = item.nameUser
                    setImageUrl(binding.image, item?.imageUser)

                } else {
                    binding.userName.text = item.nameOwnerItemUser
                    setImageUrl(binding.image, item?.imageOwnerItem)


                }


              if (AppDefs.user.results!!.id == item.userOwnerItemId) {
                    if (item.countMassage == "0"){
                        binding.statusRed.visibility=View.INVISIBLE
                    }else{
                        binding.statusRed.visibility=View.VISIBLE

                    }

                }else{
                    if (item.countMessageOwnerItem == "0"){
                        binding.statusRed.visibility=View.INVISIBLE
                    }else{
                        binding.statusRed.visibility=View.VISIBLE

                    }

                }
            }

            binding.constraint.setOnClickListener {
                onclickListener(item)
                notifyDataSetChanged()
            }
        }
    }



    @BindingAdapter("imageUrl")
    fun setImageUrl(imgView: ImageView, imgUrl: String?) {
        imgUrl?.let {
            val imgUri = it.toUri().buildUpon().scheme("http").build()
            Glide.with(imgView.context)
                .load(imgUri).placeholder(R.drawable.category_icon)
                .error(R.drawable.category_icon)
                .into(imgView)

        }

    }

}


