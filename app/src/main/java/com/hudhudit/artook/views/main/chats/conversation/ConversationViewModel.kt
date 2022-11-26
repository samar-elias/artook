package com.hudhudit.artook.views.main.chats.conversation

import android.app.appsearch.ReportSystemUsageRequest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hudhudit.artook.apputils.modules.chat.MessageModel

import com.hudhudit.artook.apputils.modules.chat.UserChatModel
import com.hudhudit.artook.apputils.remote.repostore.ChateRepository
import com.hudhudit.artook.apputils.remote.utill.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(

    val repository: ChateRepository,


    ) : ViewModel() {


    val messageStatus = MutableLiveData<Resource<MutableList<MessageModel>>?>()
    val addMessageStatus = MutableLiveData<Resource<Pair<MessageModel, String>>?>()
    val updateStatus = MutableLiveData<Resource<String?>?>()
    val updateCountStatus = MutableLiveData<Resource<String?>?>()



    fun getMessage(chatId: String) {
        viewModelScope.launch {
            repository.getMessage(chatId) { messageStatus.postValue(it) }

        }
    }
    fun updateUserStatus(
        id:String,
        countMessageOwnerItem: String,
        countMassage: String, userChatModel: UserChatModel, status: String,
    ) {
        viewModelScope.launch {
            repository.updateMassage(id,countMessageOwnerItem, countMassage, userChatModel, status) {
                updateStatus.postValue(it)
            }
        }
    }
    fun updateMassageCountUserStatus(id:String,userChatModel: UserChatModel, status: String) {
        viewModelScope.launch {
            repository.updateCountMassage(id,userChatModel, status) { updateCountStatus.postValue(it) }

        }
    }


    fun addMessage(chatId: String, messageModel: MessageModel) {
        viewModelScope.launch {
            repository.sendMessage(chatId, messageModel) { addMessageStatus.postValue(it) }

        }

    }

    fun reset() {
        messageStatus.postValue(null)
    }

    fun resetCount() {
        updateStatus.postValue(null)
    }

    fun restAddMessageStatus() {
        addMessageStatus.postValue(null)
    }
    fun restCountStatus(){
        updateCountStatus.postValue(null)
    }



}

