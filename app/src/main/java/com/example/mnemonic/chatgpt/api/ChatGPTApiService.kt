package com.example.mnemonic.chatgpt.api

import com.example.mnemonic.chatgpt.model.ChatGPTRequest
import com.example.mnemonic.chatgpt.model.ChatGPTResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatGPTApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun sendChatGPTRequest(@Body request: ChatGPTRequest): Response<ChatGPTResponse>
}