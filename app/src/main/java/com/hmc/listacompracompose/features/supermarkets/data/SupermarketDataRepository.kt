package com.hmc.listacompracompose.features.supermarkets.data

import com.hmc.listacompracompose.features.supermarkets.data.db.SupermarketLocalDataSource
import com.hmc.listacompracompose.features.supermarkets.data.xml.ActiveSupermarketPreferences
import com.hmc.listacompracompose.features.supermarkets.domain.SupermarketModel
import com.hmc.listacompracompose.features.supermarkets.domain.SupermarketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Single

@Single
class SupermarketDataRepository(
    private val localDataSource: SupermarketLocalDataSource,
    private val activePreferences: ActiveSupermarketPreferences
) : SupermarketRepository {

    override fun observeSupermarkets(): Flow<List<SupermarketModel>> =
        localDataSource.observeSupermarkets()

    override fun observeActiveSupermarket(): Flow<SupermarketModel?> =
        combine(
            localDataSource.observeSupermarkets(),
            activePreferences.activeId
        ) { list, activeId ->
            list.firstOrNull { it.id == activeId } ?: list.firstOrNull()
        }

    override fun setActiveSupermarket(id: Int) = activePreferences.setActive(id)

    override suspend fun createSupermarket(name: String): Int =
        localDataSource.createSupermarket(name)

    override suspend fun renameSupermarket(id: Int, name: String) =
        localDataSource.renameSupermarket(id, name)

    override suspend fun deleteSupermarket(id: Int): Boolean =
        localDataSource.deleteSupermarket(id)
}