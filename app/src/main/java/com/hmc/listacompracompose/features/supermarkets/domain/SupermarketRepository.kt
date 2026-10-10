package com.hmc.listacompracompose.features.supermarkets.domain

import kotlinx.coroutines.flow.Flow

interface SupermarketRepository {
    fun observeSupermarkets(): Flow<List<SupermarketModel>>

    fun observeActiveSupermarket(): Flow<SupermarketModel?>

    fun setActiveSupermarket(id: Int)

    suspend fun createSupermarket(name: String): Int

    suspend fun renameSupermarket(id: Int, name: String)

    suspend fun deleteSupermarket(id: Int): Boolean
}