package com.hmc.listacompracompose.features.supermarkets.data.db

import com.hmc.listacompracompose.features.data.local.db.dao.SupermarketDao
import com.hmc.listacompracompose.features.data.local.db.entities.SupermarketEntity
import com.hmc.listacompracompose.features.supermarkets.domain.SupermarketModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class SupermarketLocalDataSource(private val dao: SupermarketDao) {

    fun observeSupermarkets(): Flow<List<SupermarketModel>> =
        dao.observeSupermarkets().map { list -> list.map { it.toModel() } }

    suspend fun createSupermarket(name: String): Int =
        dao.insert(SupermarketEntity(name = name)).toInt()

    suspend fun renameSupermarket(id: Int, name: String) = dao.rename(id, name)

    suspend fun deleteSupermarket(id: Int): Boolean = dao.deleteIfNotLast(id)
}
