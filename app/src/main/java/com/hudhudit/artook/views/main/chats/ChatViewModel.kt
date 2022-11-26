package com.hudhudit.artook.views.main.chats


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.hudhudit.artook.apputils.modules.chat.UserChatModel
import com.hudhudit.artook.apputils.remote.repostore.ChateRepository
import com.hudhudit.artook.apputils.remote.utill.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(

    val repository: ChateRepository,

    ) : ViewModel() {

    val chatStatus = MutableLiveData<Resource<MutableList<UserChatModel>>?>()
    val addChatStatus = MutableLiveData<Resource<Pair<UserChatModel,String>>>()


    fun addChat(note: UserChatModel){
        viewModelScope.launch {
            // _addNote.value = UiState.Loading
            repository.addChat(note) { addChatStatus.value = it }

        }

    }

    fun getChat(id:String) {
        viewModelScope.launch {
         repository.getChat(id) { chatStatus.postValue(it)}

        }
    }


    fun reset() {
        chatStatus.postValue(null)
    }

}

