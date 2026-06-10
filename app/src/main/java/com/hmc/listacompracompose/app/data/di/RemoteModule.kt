package com.hmc.listacompracompose.app.data.di

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class RemoteModule {

    private val url = "https://rikicompras-001-site1.rtempurl.com/"

    @Single
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(url)
            .build()
}