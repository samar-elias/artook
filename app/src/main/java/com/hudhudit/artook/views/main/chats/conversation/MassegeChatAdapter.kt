package com.hudhudit.artook.views.main.chats.conversation


import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.hudhudit.artook.apputils.base.BaseBindingViewHolder

import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppConstants.Companion.VIEW_RESEVER
import com.hudhudit.artook.apputils.appdefs.AppConstants.Companion.VIEW_SENDER
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.getMyPrettyDate
import com.hudhudit.artook.apputils.base.BaseAdapter
import com.hudhudit.artook.apputils.modules.chat.MessageModel
import com.hudhudit.artook.apputils.modules.chat.UserChatModel
import com.hudhudit.artook.databinding.RowResivedBinding
import com.hudhudit.artook.databinding.RowSendBinding
import java.text.SimpleDateFormat
import java.util.*

class MessageChatAdapter(
    private var marketSections: MutableList<MessageModel>,
    private var userChatModel: UserChatModel
) : BaseAdapter<MessageModel, BaseBindingViewHolder<MessageModel>>(marketSections) {
    var x = ""
    var y = ""
    override fun getViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<MessageModel> {
        return when (viewType) {
            VIEW_SENDER -> {
                val binding = RowSendBinding.inflate(inflater, parent, false)
                SectionViewHolder(binding)
            }

            VIEW_RESEVER -> {
                val binding = RowResivedBinding.inflate(inflater, parent, false)
                CategoryViewHolder(binding)
            }
            else -> {
                val binding = RowSendBinding.inflate(inflater, parent, false)
                SectionViewHolder(binding)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        var userid=AppDefs.user.results!!.id


        return if (marketSections[position].sender ==  userid)
            VIEW_SENDER
        else
            VIEW_RESEVER


    }



    inner class CategoryViewHolder(
        private val binding: RowResivedBinding,

    ) : BaseBindingViewHolder<MessageModel>(binding) {
        override fun bind(position: Int, messageModel: MessageModel?) {
            bind<RowResivedBinding> {
                if (messageModel is MessageModel) {
                    modual = messageModel
                    val originalString: Long = messageModel!!.messageTime!!.toLong()
                    val sdf = SimpleDateFormat("h:mm aaa")
                    val resultdate = Date(originalString)
                    binding.time.text = sdf.format(resultdate)
                    setImageUrl(binding.senderImg, userChatModel.imageOwnerItem)

                }

                val originalString: Long = messageModel!!.messageTime!!.toLong()

                val sdf = SimpleDateFormat("yyyy-MM-dd")
                val resultdate = Date(originalString)
                y = sdf.format(resultdate)
                if (x == y) {

                    binding.tvDate.visibility = View.GONE
                } else {
                    binding.tvDate.visibility = View.VISIBLE
                    binding.tvDate.text = getMyPrettyDate(
                        originalString,context)
                    x = y
                }

            }


        }
    }

    inner class SectionViewHolder(
        private val binding: RowSendBinding,
    ) : BaseBindingViewHolder<MessageModel>(binding) {
        override fun bind(position: Int, messageModel: MessageModel?) {
            bind<RowSendBinding> {
                if (messageModel is MessageModel) {
                    modual = messageModel
                    val originalString: Long = messageModel!!.messageTime!!.toLong()
                    val sdf = SimpleDateFormat("h:mm aaa")
                    val resultdate = Date(originalString)
                    binding.time.text = sdf.format(resultdate)
                    setImageUrl(binding.senderImg, userChatModel.imageUser)



                }



                val originalString: Long = messageModel!!.messageTime!!.toLong()

                val sdf = SimpleDateFormat("yyyy-MM-dd")
                val resultdate = Date(originalString)
                // println(sdf.format(resultdate))
                y = sdf.format(resultdate)
                if (x == y) {

                    binding.tvDate.visibility = View.GONE
                } else {
                    binding.tvDate.visibility = View.VISIBLE
                    binding.tvDate.text = getMyPrettyDate(
                        originalString,context)
                    x = y
                }

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