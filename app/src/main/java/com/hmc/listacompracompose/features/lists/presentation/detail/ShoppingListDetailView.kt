package com.hmc.listacompracompose.features.lists.presentation.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hmc.listacompracompose.app.util.filterByQuery
import com.hmc.listacompracompose.app.util.formatPrice
import com.hmc.listacompracompose.app.util.groupByCategory
import com.hmc.listacompracompose.features.categories.domain.CategoryModel
import com.hmc.listacompracompose.features.items.domain.ItemModel
import com.hmc.listacompracompose.features.lists.domain.ListItemModel
import com.hmc.listacompracompose.ui.components.CategoryHeader
import com.hmc.listacompracompose.ui.components.SearchField
import com.hmc.listacompracompose.ui.components.UndoSnackbarEffect
import com.hmc.listacompracompose.ui.theme.appTopBarColors
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ShoppingListDetailView(
    listId: Int,
    onBack: () -> Unit,
    viewModel: ShoppingListDetailViewModel = koinViewModel(parameters = { parametersOf(listId) })
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ShoppingListDetailContent(
        state = state,
        onBack = onBack,
        onOpenPicker = viewModel::openPicker,
        onClosePicker = viewModel::closePicker,
        onPick = { viewModel.addItem(it.id) },
        onCheckedChange = viewModel::setChecked,
        onAmountChange = viewModel::setAmount,
        onRemove = viewModel::removeItem,
        onUndo = viewModel::undoRemove,
        onUndoShown = viewModel::undoShown,
        onErrorShown = viewModel::errorShown
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListDetailContent(
    state: ListDetailUiState,
    onBack: () -> Unit,
    onOpenPicker: () -> Unit,
    onClosePicker: () -> Unit,
    onPick: (ItemModel) -> Unit,
    onCheckedChange: (itemId: Int, checked: Boolean) -> Unit,
    onAmountChange: (itemId: Int, amount: Int) -> Unit,
    onRemove: (ListItemModel) -> Unit,
    onUndo: () -> Unit,
    onUndoShown: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val groups = remember(state.items) { state.items.groupBy { it.categoryId } }

    LaunchedEffect(state.error) {
        state.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            onErrorShown()
        }
    }

    UndoSnackbarEffect(
        prompt = state.undo,
        snackbarHostState = snackbarHostState,
        onUndo = onUndo,
        onPromptShown = onUndoShown
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(state.listName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = appTopBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenPicker) {
                Icon(Icons.Default.Add, contentDescription = "Añadir productos")
            }
        },
        bottomBar = {
            if (state.items.isNotEmpty()) {
                Surface(tonalElevation = 3.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "%.2f €".format(state.total),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        if (state.itemsWithoutPrice > 0) {
                            Text(
                                text = if (state.itemsWithoutPrice == 1) {
                                    "1 producto sin precio"
                                } else {
                                    "${state.itemsWithoutPrice} productos sin precio"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.items.isEmpty() -> {
                    Text(
                        text = "Esta lista está vacía.\nPulsa + para añadir productos del catálogo.",
                        modifier = Modifier.align(Alignment.Center)
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
                        groups.forEach { (categoryId, groupItems) ->
                            stickyHeader(key = "header-${categoryId ?: "none"}") {
                                CategoryHeader(
                                    name = groupItems.first().categoryName ?: "Sin categoría"
                                )
                            }
                            items(items = groupItems, key = { it.itemId }) { item ->
                                ListItemRow(
                                    item = item,
                                    onCheckedChange = { onCheckedChange(item.itemId, it) },
                                    onAmountChange = { onAmountChange(item.itemId, it) },
                                    onRemove = { onRemove(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    state.picker?.let { picker ->
        CatalogPickerSheet(
            catalogItems = picker.items,
            categories = picker.categories,
            onPick = onPick,
            onDismiss = onClosePicker
        )
    }
}

@Composable
fun ListItemRow(
    item: ListItemModel,
    onCheckedChange: (Boolean) -> Unit,
    onAmountChange: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = item.isChecked, onCheckedChange = onCheckedChange)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
                )
                Text(
                    text = item.price?.let { "%.2f € × %d".format(it, item.amount) }
                        ?: "Sin precio × ${item.amount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onAmountChange(item.amount - 1) },
                        enabled = item.amount > 1
                    ) { Text("−", style = MaterialTheme.typography.titleLarge) }
                    Text("${item.amount}", style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = { onAmountChange(item.amount + 1) }) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Quitar de la lista",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CatalogPickerSheet(
    catalogItems: List<ItemModel>,
    categories: List<CategoryModel>,
    onPick: (ItemModel) -> Unit,
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val groups = remember(catalogItems, categories, query) {
        groupByCategory(
            items = catalogItems.filterByQuery(query) { it.name },
            categories = categories,
            categoryIdOf = { it.categoryId }
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = "Añadir desde el catálogo",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (catalogItems.isEmpty()) {
            Text(
                text = "No quedan productos por añadir.\nCrea más en la pestaña Catálogo.",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            SearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Buscar producto",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (groups.isEmpty()) {
                Text(
                    text = "No hay productos que coincidan con \"$query\".",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    groups.forEach { group ->
                        stickyHeader(key = "header-${group.categoryId ?: "none"}") {
                            CategoryHeader(
                                name = group.title,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                containerColor = BottomSheetDefaults.ContainerColor
                            )
                        }
                        items(items = group.items, key = { it.id }) { item ->
                            ListItem(
                                headlineContent = { Text(item.name) },
                                supportingContent = { Text(item.price.formatPrice()) },
                                trailingContent = {
                                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                                },
                                modifier = Modifier.clickable { onPick(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}