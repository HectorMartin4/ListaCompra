package com.hmc.listacompracompose.app.data.di

import com.google.gson.Gson
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
class AppModule {

    @Single
    fun provideGson() = Gson()
}