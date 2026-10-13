package com.hmc.listacompracompose.features.di

import com.hmc.listacompracompose.app.data.db.ItemDatabase
import com.hmc.listacompracompose.features.data.local.db.dao.CategoryDao
import com.hmc.listacompracompose.features.data.local.db.dao.ItemDao
import com.hmc.listacompracompose.features.data.local.db.dao.ShoppingListDao
import com.hmc.listacompracompose.features.items.data.remote.ApiService
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import retrofit2.Retrofit

@Module
class FeatureModule {

    @Single
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Single
    fun provideItemDao(db: ItemDatabase): ItemDao {
        return db.itemDao()
    }

    @Single
    fun provideShoppingListDao(db: ItemDatabase): ShoppingListDao {
        return db.shoppingListDao()
    }

    @Single
    fun provideCategoryDao(db: ItemDatabase): CategoryDao {
        return db.categoryDao()
    }
}