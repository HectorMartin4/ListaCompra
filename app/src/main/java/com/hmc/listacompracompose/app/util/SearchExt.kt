package com.hmc.listacompracompose.app.util

import com.hmc.listacompracompose.features.categories.domain.CategoryModel
import java.text.Normalizer

private val diacritics = Regex("\\p{InCombiningDiacriticalMarks}+")

fun String.normalizeForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(diacritics, "")
        .lowercase()
        .trim()

fun <T> List<T>.filterByQuery(query: String, nameOf: (T) -> String): List<T> {
    val normalizedQuery = query.normalizeForSearch()
    return if (normalizedQuery.isEmpty()) this
    else filter { nameOf(it).normalizeForSearch().contains(normalizedQuery) }
}

data class CategoryGroup<T>(
    val categoryId: Int?,
    val title: String,
    val items: List<T>
)

// Grupos en el orden de las categorías; lo que no tenga categoría válida va al final
fun <T> groupByCategory(
    items: List<T>,
    categories: List<CategoryModel>,
    categoryIdOf: (T) -> Int?
): List<CategoryGroup<T>> {
    val byCategory = items.groupBy(categoryIdOf)
    val groups = categories.mapNotNull { category ->
        byCategory[category.id]?.let { CategoryGroup(category.id, category.name, it) }
    }
    val withoutCategory = items.filter { item ->
        val id = categoryIdOf(item)
        id == null || categories.none { it.id == id }
    }
    return if (withoutCategory.isEmpty()) groups
    else groups + CategoryGroup(null, "Sin categoría", withoutCategory)
}

// Filtrado también por nombre de categoría
fun <T> List<T>.filterByQueryWithCategory(
    query: String,
    nameOf: (T) -> String,
    categories: List<CategoryModel>,
    categoryIdOf: (T) -> Int?
): List<T> {
    val normalizedQuery = query.normalizeForSearch()
    return if (normalizedQuery.isEmpty()) this
    else filter { item ->
        val nameMatch = nameOf(item).normalizeForSearch().contains(normalizedQuery)
        val categoryMatch = categories.firstOrNull { it.id == categoryIdOf(item) }
            ?.name?.normalizeForSearch()
            ?.contains(normalizedQuery) == true
        nameMatch || categoryMatch
    }
}