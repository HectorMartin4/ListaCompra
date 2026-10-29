package com.hmc.listacompracompose.features.lists.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hmc.listacompracompose.features.lists.domain.ShoppingListModel
import com.hmc.listacompracompose.features.supermarkets.domain.SupermarketModel
import com.hmc.listacompracompose.ui.theme.appTopBarColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShoppingListsView(
    onListClick: (listId: Int) -> Unit,
    viewModel: ShoppingListsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ShoppingListsContent(
        state = state,
        onCreate = { name, supermarketId -> viewModel.createList(name, supermarketId) },
        onDelete = { viewModel.deleteList(it.id) },
        onListClick = onListClick,
        onErrorShown = viewModel::errorShown,
        onRename = viewModel::renameList
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ShoppingListsContent(
    state: ShoppingListsUiState,
    onCreate: (name: String, supermarketId: Int) -> Unit,
    onDelete: (ShoppingListModel) -> Unit,
    onListClick: (Int) -> Unit,
    onErrorShown: () -> Unit,
    onRename: (id: Int, name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var listToDelete by remember { mutableStateOf<ShoppingListModel?>(null) }
    var listToRename by remember { mutableStateOf<ShoppingListModel?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

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
                title = { Text("Listas") },
                colors = appTopBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Crear lista")
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
                state.lists.isEmpty() -> {
                    Text(
                        text = "Aún no tienes listas.\nPulsa + para crear la primera.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    val groups = remember(state.lists) {
                        state.lists.groupBy { it.supermarketId }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 16.dp, bottom = 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groups.forEach { (supermarketId, lists) ->
                            val supermarketName = lists.first().supermarketName
                            stickyHeader(key = "header-sm-$supermarketId") {
                                SupermarketHeader(
                                    name = supermarketName,
                                    listCount = lists.size
                                )
                            }
                            items(items = lists, key = { it.id }) { list ->
                                val isComplete = state.listCompletion[list.id] == true
                                ShoppingListRow(
                                    list = list,
                                    isComplete = isComplete,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    onClick = { onListClick(list.id) },
                                    onRename = { listToRename = list },
                                    onDelete = { listToDelete = list }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateListDialog(
            title = "Nueva lista",
            confirmText = "Crear",
            supermarkets = state.supermarkets,
            initialSupermarketId = state.activeSupermarketId,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, supermarketId ->
                onCreate(name, supermarketId)
                showCreateDialog = false
            }
        )
    }

    listToRename?.let { list ->
        ListNameDialog(
            title = "Renombrar lista",
            confirmText = "Guardar",
            initialName = list.name,
            onDismiss = { listToRename = null },
            onConfirm = { name ->
                onRename(list.id, name)
                listToRename = null
            }
        )
    }

    listToDelete?.let { list ->
        AlertDialog(
            onDismissRequest = { listToDelete = null },
            title = { Text("Borrar lista") },
            text = { Text("Se borrará \"${list.name}\" con todos sus productos. Los productos del catálogo no se borran.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(list)
                    listToDelete = null
                }) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { listToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun ShoppingListRow(
    list: ShoppingListModel,
    isComplete: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = list.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (isComplete) {
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text("Completada", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Renombrar") },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Borrar") },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SupermarketHeader(
    name: String,
    listCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Store,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$listCount ${if (listCount == 1) "lista" else "listas"}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun CreateListDialog(
    title: String,
    confirmText: String,
    supermarkets: List<SupermarketModel>,
    initialSupermarketId: Int?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, supermarketId: Int) -> Unit,
    initialName: String = ""
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var supermarketId by rememberSaveable {
        mutableStateOf(initialSupermarketId ?: supermarkets.firstOrNull()?.id)
    }
    var menuExpanded by remember { mutableStateOf(false) }

    val currentName = supermarkets.firstOrNull { it.id == supermarketId }?.name ?: ""

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la lista") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    singleLine = true
                )
                Box {
                    OutlinedButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Supermercado: $currentName")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        supermarkets.forEach { supermarket ->
                            DropdownMenuItem(
                                text = { Text(supermarket.name) },
                                onClick = {
                                    supermarketId = supermarket.id
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
                enabled = name.isNotBlank() && supermarketId != null,
                onClick = { supermarketId?.let { onConfirm(name.trim(), it) } }
            ) { Text(confirmText) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun ListNameDialog(
    title: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit,
    initialName: String = ""
) {
    var name by rememberSaveable { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de la lista") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name.trim()) }
            ) { Text(confirmText) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
