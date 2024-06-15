package com.example.mnemonic.chatgpt.api

import com.example.mnemonic.apikey.ApiKey
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ChatGPTRetrofitInstance {
    private const val BASE_URL = "https://api.openai.com/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .header("Authorization", "Bearer ${ApiKey.CHATGPT_SECRET_KEY}")
                .header("Content-Type", "application/json")
                .method(originalRequest.method(), originalRequest.body())

            val newRequest = requestBuilder.build()
            chain.proceed(newRequest)
    }.build()

    val api: ChatGPTApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatGPTApiService::class.java)
    }
}