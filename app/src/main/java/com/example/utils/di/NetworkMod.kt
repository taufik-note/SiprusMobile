package com.example.utils.di

import com.example.data.api.ApiService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkMod {
    fun provideApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
