package com.hmc.listacompracompose.features.items.presentation.itemList

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hmc.listacompracompose.app.util.filterByQuery
import com.hmc.listacompracompose.app.util.groupByCategory
import com.hmc.listacompracompose.features.categories.domain.CategoryModel
import com.hmc.listacompracompose.features.items.domain.ItemModel
import com.hmc.listacompracompose.features.supermarkets.presentation.SupermarketSelector
import com.hmc.listacompracompose.ui.components.CategoryHeader
import com.hmc.listacompracompose.ui.components.SearchField
import com.hmc.listacompracompose.ui.theme.appTopBarColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ItemListView(
    onOpenCategories: () -> Unit,
    onManageSupermarkets: () -> Unit,
    viewModel: ItemListViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ItemListContent(
        state = state,
        onAdd = viewModel::addItem,
        onDelete = viewModel::deleteCatalogItem,
        onErrorShown = viewModel::errorShown,
        onEdit = viewModel::updateItem,
        onOpenCategories = onOpenCategories,
        onManageSupermarkets = onManageSupermarkets
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ItemListContent(
    state: ItemListUiState,
    onAdd: (name: String, price: Double?, categoryId: Int?) -> Unit,
    onEdit: (ItemModel) -> Unit,
    onDelete: (ItemModel) -> Unit,
    onOpenCategories: () -> Unit,
    onManageSupermarkets: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ItemModel?>(null) }
    var itemToDelete by remember { mutableStateOf<ItemModel?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val groups = remember(state.itemList, state.categories, query) {
        groupByCategory(
            items = state.itemList.filterByQuery(query) { it.name },
            categories = state.categories,
            categoryIdOf = { it.categoryId }
        )
    }

    LaunchedEffect(state.error) {
        state.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { SupermarketSelector(onManage = onManageSupermarkets) },
                actions = {
                    TextButton(onClick = onOpenCategories) { Text("Categorías") }
                },
                colors = appTopBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir al catálogo")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.itemList.isNotEmpty()) {
                SearchField(
                    query = query,
                    onQueryChange = { query = it },
                    placeholder = "Buscar producto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    state.isLoading && state.itemList.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    state.itemList.isEmpty() -> {
                        Text(
                            text = "Tu catálogo está vacío.\nPulsa + para añadir un producto.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    groups.isEmpty() -> {
                        Text(
                            text = "No hay productos que coincidan con \"$query\".",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            groups.forEach { group ->
                                stickyHeader(key = "header-${group.categoryId ?: "none"}") {
                                    CategoryHeader(name = group.title)
                                }
                                items(items = group.items, key = { it.id }) { item ->
                                    CatalogItemRow(
                                        item = item,
                                        onClick = { itemToEdit = item },
                                        onDelete = { itemToDelete = item }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CatalogItemDialog(
            title = "Nuevo Producto al Catálogo",
            confirmText = "Añadir",
            categories = state.categories,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, price, categoryId ->
                onAdd(name, price, categoryId)
                showAddDialog = false
            }
        )
    }

    itemToEdit?.let { item ->
        CatalogItemDialog(
            title = "Editar producto",
            confirmText = "Guardar",
            categories = state.categories,
            initialName = item.name,
            initialPrice = item.price,
            initialCategoryId = item.categoryId,
            onDismiss = { itemToEdit = null },
            onConfirm = { name, price, categoryId ->
                onEdit(item.copy(name = name, price = price, categoryId = categoryId))
                itemToEdit = null
            }
        )
    }

    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Borrar producto") },
            text = {
                Text("Se borrará \"${item.name}\" del catálogo y desaparecerá de todas las listas donde esté.")
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(item)
                    itemToDelete = null
                }) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun CatalogItemRow(
    item: ItemModel,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = item.price?.let { "Precio habitual: %.2f €".format(it) } ?: "Sin precio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar del catálogo",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CatalogItemDialog(
    title: String,
    confirmText: String,
    categories: List<CategoryModel>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Double?, categoryId: Int?) -> Unit,
    initialName: String = "",
    initialPrice: Double? = null,
    initialCategoryId: Int? = null
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var priceText by rememberSaveable {
        mutableStateOf(initialPrice?.let { "%.2f".format(it) } ?: "")
    }
    // Si la categoría del producto ya no existe (se borró), se parte de "Sin categoría"
    var categoryId by rememberSaveable {
        mutableStateOf(initialCategoryId?.takeIf { id -> categories.any { it.id == id } })
    }
    var menuExpanded by remember { mutableStateOf(false) }

    val price = priceText.replace(',', '.').toDoubleOrNull()
    val priceInvalid = priceText.isNotBlank() && (price == null || price < 0)
    val canConfirm = name.isNotBlank() && !priceInvalid
    val categoryName = categories.firstOrNull { it.id == categoryId }?.name ?: "Sin categoría"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del producto") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Precio (opcional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = priceInvalid,
                    supportingText = { if (priceInvalid) Text("Introduce un precio válido") }
                )
                Box {
                    OutlinedButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Categoría: $categoryName")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin categoría") },
                            onClick = {
                                categoryId = null
                                menuExpanded = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    categoryId = category.id
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canConfirm,
                onClick = { onConfirm(name.trim(), price, categoryId) }
            ) { Text(confirmText) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}