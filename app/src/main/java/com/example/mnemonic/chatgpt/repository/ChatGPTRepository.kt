package com.example.mnemonic.chatgpt.repository

import com.example.mnemonic.chatgpt.api.ChatGPTApiService
import com.example.mnemonic.chatgpt.model.ChatGPTRequest
import com.example.mnemonic.chatgpt.model.ChatGPTResponse
import retrofit2.Response

class ChatGPTRepository(private val chatGPTApiService: ChatGPTApiService) {
    suspend fun sendChatGPTRequest(request: ChatGPTRequest) : Response<ChatGPTResponse> {
        return chatGPTApiService.sendChatGPTRequest(request)
    }
}