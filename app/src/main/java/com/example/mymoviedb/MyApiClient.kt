package com.example.mymoviedb

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApiClient {
    private var retrofit: Retrofit? = null
    private val BASE_URL = "https://api.github.com"

    fun getClient(): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit
    }
}