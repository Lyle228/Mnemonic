package com.example.mnemonic.chatgpt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mnemonic.chatgpt.repository.ChatGPTRepository

class ChatGPTViewModelFactory(private val repository: ChatGPTRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatGPTViewModel::class.java)) {
            return ChatGPTViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
