package com.hmc.listacompracompose.features.supermarkets.domain

import com.hmc.listacompracompose.app.util.normalizeName
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ObserveSupermarketsUseCase(private val repository: SupermarketRepository) {
    operator fun invoke(): Flow<List<SupermarketModel>> = repository.observeSupermarkets()
}

@Single
class ObserveActiveSupermarketUseCase(private val repository: SupermarketRepository) {
    operator fun invoke(): Flow<SupermarketModel?> = repository.observeActiveSupermarket()
}

@Single
class SetActiveSupermarketUseCase(private val repository: SupermarketRepository) {
    operator fun invoke(id: Int) = repository.setActiveSupermarket(id)
}

@Single
class CreateSupermarketUseCase(private val repository: SupermarketRepository) {
    suspend operator fun invoke(name: String) {
        val id = repository.createSupermarket(name.normalizeName())
        repository.setActiveSupermarket(id)
    }
}

@Single
class RenameSupermarketUseCase(private val repository: SupermarketRepository) {
    suspend operator fun invoke(id: Int, name: String) =
        repository.renameSupermarket(id, name.normalizeName())
}

@Single
class DeleteSupermarketUseCase(private val repository: SupermarketRepository) {
    suspend operator fun invoke(id: Int): Boolean = repository.deleteSupermarket(id)
}