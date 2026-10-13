package com.hmc.listacompracompose.features.supermarkets.data

import com.hmc.listacompracompose.features.data.local.db.dao.SupermarketDao
import com.hmc.listacompracompose.features.supermarkets.data.xml.ActiveSupermarketPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Single

@Single
class ActiveSupermarketProvider(
    dao: SupermarketDao,
    preferences: ActiveSupermarketPreferences
) {
    // ID del supermercado activo; se vuelve a emitir cuando cambia
    val activeId: Flow<Int> = combine(
        dao.observeSupermarkets(),
        preferences.activeId
    ) { supermarkets, savedId ->
        (supermarkets.firstOrNull { it.id == savedId } ?: supermarkets.firstOrNull())?.id
    }.filterNotNull().distinctUntilChanged()

    // Para las operaciones puntuales (crear algo en el supermercado activo)
    suspend fun current(): Int = activeId.first()
}