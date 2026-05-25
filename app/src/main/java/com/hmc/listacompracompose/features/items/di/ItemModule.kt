package com.hmc.listacompracompose.features.items.di

import com.hmc.listacompracompose.app.data.db.ItemDatabase
import com.hmc.listacompracompose.features.items.data.local.db.ItemDao
import com.hmc.listacompracompose.features.items.data.remote.ApiService
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import retrofit2.Retrofit

@Module
@ComponentScan("com.hmc.listacompracompose")
class ItemModule {

    @Single
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Single
    fun provideItemDao(db: ItemDatabase): ItemDao {
        return db.itemDao()
    }
}