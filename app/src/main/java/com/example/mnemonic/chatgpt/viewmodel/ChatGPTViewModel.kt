package com.example.mnemonic.chatgpt.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mnemonic.chatgpt.model.ChatGPTRequest
import com.example.mnemonic.chatgpt.model.Message
import com.example.mnemonic.chatgpt.repository.ChatGPTRepository
import kotlinx.coroutines.launch

class ChatGPTViewModel(private val repository: ChatGPTRepository) : ViewModel() {
    private val TAG = "chatGPTApi"
    private val _adviceMessage : MutableLiveData<String> = MutableLiveData()
    val adviceMessage : LiveData<String>
        get() = _adviceMessage
    fun getChatGPTResponse(prompt: String) {
        val request = ChatGPTRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(
                Message(role = "user", content = prompt)
            )
        )
        viewModelScope.launch {
            val response = repository.sendChatGPTRequest(request)
            if (response.isSuccessful) {
                val result = response.body()
                if (result != null && result.choices.isNotEmpty()) {
                    val advice = result.choices[0].message.content
                    _adviceMessage.postValue(advice)
                }
            } else {
                val exception = response.errorBody()?.string()
                Log.e(TAG, exception.toString())
            }
        }
    }

}