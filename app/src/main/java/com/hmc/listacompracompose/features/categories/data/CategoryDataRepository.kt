package com.hmc.listacompracompose.features.categories.data

import com.hmc.listacompracompose.features.categories.data.db.CategoryLocalDataSource
import com.hmc.listacompracompose.features.categories.domain.CategoryModel
import com.hmc.listacompracompose.features.categories.domain.CategoryRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class CategoryDataRepository(
    private val localDataSource: CategoryLocalDataSource
) : CategoryRepository {

    override fun observeCategories(): Flow<List<CategoryModel>> =
        localDataSource.observeCategories()

    override suspend fun createCategory(name: String) = localDataSource.createCategory(name)

    override suspend fun renameCategory(id: Int, name: String) =
        localDataSource.renameCategory(id, name)

    override suspend fun deleteCategory(id: Int) = localDataSource.deleteCategory(id)

    override suspend fun reorderCategories(orderedIds: List<Int>) =
        localDataSource.reorderCategories(orderedIds)
}