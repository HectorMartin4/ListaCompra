package com.hmc.listacompracompose.features.lists.presentation.detail

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hmc.listacompracompose.app.util.filterByQuery
import com.hmc.listacompracompose.app.util.formatPrice
import com.hmc.listacompracompose.app.util.groupByCategory
import com.hmc.listacompracompose.features.categories.domain.CategoryModel
import com.hmc.listacompracompose.features.items.domain.ItemModel
import com.hmc.listacompracompose.features.lists.domain.ListItemModel
import com.hmc.listacompracompose.features.lists.presentation.ListNameDialog
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
        onModeChange = viewModel::setMode,
        onOpenPicker = viewModel::openPicker,
        onClosePicker = viewModel::closePicker,
        onPick = { viewModel.addItem(it.id) },
        onCheckedChange = viewModel::setChecked,
        onAmountChange = viewModel::setAmount,
        onPaidPriceChange = viewModel::setPaidPrice,
        onClearPaidPrice = viewModel::clearPaidPrice,
        onRemove = viewModel::removeItem,
        onUndo = viewModel::undoRemove,
        onUndoShown = viewModel::undoShown,
        onRename = viewModel::renameList,
        onDeleteList = { viewModel.deleteList(onSuccess = onBack) },
        onErrorShown = viewModel::errorShown
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListDetailContent(
    state: ListDetailUiState,
    onBack: () -> Unit,
    onModeChange: (ListMode) -> Unit,
    onOpenPicker: () -> Unit,
    onClosePicker: () -> Unit,
    onPick: (ItemModel) -> Unit,
    onCheckedChange: (itemId: Int, checked: Boolean) -> Unit,
    onAmountChange: (itemId: Int, amount: Int) -> Unit,
    onPaidPriceChange: (itemId: Int, price: Double) -> Unit,
    onClearPaidPrice: (itemId: Int) -> Unit,
    onRemove: (ListItemModel) -> Unit,
    onUndo: () -> Unit,
    onUndoShown: () -> Unit,
    onRename: (String) -> Unit,
    onDeleteList: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var menuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var itemToPrice by remember { mutableStateOf<ListItemModel?>(null) }

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
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Renombrar") },
                            onClick = { menuExpanded = false; showRenameDialog = true }
                        )
                        DropdownMenuItem(
                            text = { Text("Borrar") },
                            onClick = { menuExpanded = false; showDeleteDialog = true }
                        )
                    }
                },
                colors = appTopBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // En lista vacía siempre mostramos el FAB; si hay items, solo en modo EDICION
            if (state.items.isEmpty() || state.mode == ListMode.EDICION) {
                FloatingActionButton(onClick = onOpenPicker) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir productos")
                }
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
                        val checkedWithoutPrice =
                            state.items.count { it.isChecked && it.effectivePrice == null }
                        if (checkedWithoutPrice > 0) {
                            Text(
                                text = if (checkedWithoutPrice == 1) {
                                    "1 producto comprado sin precio"
                                } else {
                                    "$checkedWithoutPrice productos comprados sin precio"
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
                    val filtered = state.items.filterByQuery(query) { it.name }
                    val unchecked = filtered.filter { !it.isChecked }
                    val checkedItems = filtered.filter { it.isChecked }
                    val uncheckedGroups =
                        remember(unchecked) { unchecked.groupBy { it.categoryId } }
                    val totalCount = state.items.size
                    val checkedCount = state.items.count { it.isChecked }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FilterChip(
                                    selected = state.mode == ListMode.COMPRA,
                                    onClick = { onModeChange(ListMode.COMPRA) },
                                    label = { Text("Comprar") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                FilterChip(
                                    selected = state.mode == ListMode.EDICION,
                                    onClick = { onModeChange(ListMode.EDICION) },
                                    label = { Text("Editar") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                        SearchField(
                            query = query,
                            onQueryChange = { query = it },
                            placeholder = "Buscar producto…",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        val targetProgress =
                            if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f
                        val animatedProgress by animateFloatAsState(
                            targetValue = targetProgress,
                            label = "progressAnimation"
                        )
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            drawStopIndicator = {}
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uncheckedGroups.isNotEmpty()) {
                                stickyHeader(key = "header-por-comprar") {
                                    Text(
                                        text = "Por comprar",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                uncheckedGroups.forEach { (categoryId, groupItems) ->
                                    stickyHeader(key = "header-cat-${categoryId ?: "none"}") {
                                        CategoryHeader(
                                            name = groupItems.first().categoryName
                                                ?: "Sin categoría"
                                        )
                                    }
                                    items(items = groupItems, key = { it.itemId }) { item ->
                                        ListItemRow(
                                            item = item,
                                            mode = state.mode,
                                            onCheckedChange = { onCheckedChange(item.itemId, it) },
                                            onAmountChange = { onAmountChange(item.itemId, it) },
                                            onPriceClick = { itemToPrice = item },
                                            onRemove = { onRemove(item) },
                                            modifier = Modifier.animateItem()
                                        )
                                    }
                                }
                            }
                            if (checkedItems.isNotEmpty()) {
                                stickyHeader(key = "header-comprados") {
                                    Text(
                                        text = "Comprados (${checkedItems.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                items(items = checkedItems, key = { it.itemId }) { prod ->
                                    ListItemRow(
                                        item = prod,
                                        mode = state.mode,
                                        onCheckedChange = { onCheckedChange(prod.itemId, it) },
                                        onAmountChange = { onAmountChange(prod.itemId, it) },
                                        onPriceClick = { itemToPrice = prod },
                                        onRemove = { onRemove(prod) },
                                        modifier = Modifier.animateItem()
                                    )
                                }
                            }
                            if (filtered.isEmpty()) {
                                item {
                                    Text(
                                        text = if (state.items.isEmpty()) {
                                            "Esta lista está vacía.\nPulsa + para añadir productos del catálogo."
                                        } else {
                                            "No hay productos que coincidan con \"$query\"."
                                        },
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
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

    if (showRenameDialog) {
        ListNameDialog(
            title = "Renombrar lista",
            confirmText = "Guardar",
            initialName = state.listName,
            onDismiss = { showRenameDialog = false },
            onConfirm = { onRename(it); showRenameDialog = false }
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Borrar lista") },
            text = { Text("Se borrará \"${state.listName}\" con todos sus productos. Los productos del catálogo no se borran.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteList(); showDeleteDialog = false
                }) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    itemToPrice?.let { item ->
        PaidPriceDialog(
            itemName = item.name,
            currentPaidPrice = item.paidPrice,
            catalogPrice = item.price,
            onDismiss = { itemToPrice = null },
            onConfirm = { price ->
                if (price != null) onPaidPriceChange(item.itemId, price)
                else onClearPaidPrice(item.itemId)
                itemToPrice = null
            }
        )
    }
}

@Composable
fun ListItemRow(
    item: ListItemModel,
    mode: ListMode,
    onCheckedChange: (Boolean) -> Unit,
    onAmountChange: (Int) -> Unit,
    onPriceClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {

    // Animación de opacidad suave cuando está checked
    val alpha by animateFloatAsState(
        targetValue = if (item.isChecked) 0.5f else 1f,
        label = "alphaAnimation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (item.isChecked) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
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
                Crossfade(
                    targetState = item.isChecked,
                    label = "strikethroughCrossfade"
                ) { checked ->
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (checked) TextDecoration.LineThrough else null
                    )
                }
                if (mode == ListMode.COMPRA) {
                    Text(
                        text = "× ${item.amount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onPriceClick)
                        .padding(vertical = 6.dp, horizontal = 6.dp)
                ) {
                    Text(
                        text = when {
                            item.paidPrice != null -> "Pagado: ${item.paidPrice.formatPrice()}"
                            item.price != null -> item.price.formatPrice()
                            else -> "Sin precio"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (item.paidPrice != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Añadir precio pagado",
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (mode == ListMode.EDICION) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onAmountChange(item.amount - 1) },
                            enabled = item.amount > 1
                        ) { Text("−", style = MaterialTheme.typography.titleLarge) }
                        Text(
                            text = "${item.amount}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = { onAmountChange(item.amount + 1) }) {
                            Text("+", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
            if (mode == ListMode.EDICION) {
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
}

@Composable
fun PaidPriceDialog(
    itemName: String,
    currentPaidPrice: Double?,
    catalogPrice: Double?,
    onDismiss: () -> Unit,
    onConfirm: (price: Double?) -> Unit
) {
    var priceText by rememberSaveable {
        mutableStateOf(currentPaidPrice?.let { "%.2f".format(it) } ?: "")
    }

    val price = priceText.replace(',', '.').toDoubleOrNull()
    val priceInvalid = priceText.isNotBlank() && (price == null || price < 0)
    val canConfirm = !priceInvalid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Precio pagado") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = itemName,
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Precio pagado") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = priceInvalid,
                    supportingText = {
                        when {
                            priceInvalid -> Text("Introduce un precio válido")
                            catalogPrice != null ->
                                Text("Precio del catálogo: ${catalogPrice.formatPrice()}")
                        }
                    }
                )
                Text(
                    text = "Se guardará con la fecha de hoy en el histórico del producto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canConfirm && price != null,
                onClick = { price?.let { onConfirm(it) } }
            ) { Text("Guardar") }
        },
        dismissButton = {
            Row {
                if (currentPaidPrice != null) {
                    TextButton(onClick = { onConfirm(null) }) { Text("Quitar") }
                }
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        }
    )
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