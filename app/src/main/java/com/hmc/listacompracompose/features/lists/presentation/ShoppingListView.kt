package com.hmc.listacompracompose.features.lists.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hmc.listacompracompose.features.lists.domain.ShoppingListModel
import com.hmc.listacompracompose.features.supermarkets.presentation.SupermarketSelector
import com.hmc.listacompracompose.ui.theme.appTopBarColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ShoppingListsView(
    onListClick: (listId: Int) -> Unit,
    onManageSupermarkets: () -> Unit,
    viewModel: ShoppingListsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ShoppingListsContent(
        state = state,
        onCreate = viewModel::createList,
        onDelete = { viewModel.deleteList(it.id) },
        onListClick = onListClick,
        onErrorShown = viewModel::errorShown,
        onRename = viewModel::renameList,
        onManageSupermarkets = onManageSupermarkets
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsContent(
    state: ShoppingListsUiState,
    onCreate: (String) -> Unit,
    onDelete: (ShoppingListModel) -> Unit,
    onListClick: (Int) -> Unit,
    onErrorShown: () -> Unit,
    onManageSupermarkets: () -> Unit,
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
                title = { SupermarketSelector(onManage = onManageSupermarkets) },
                colors = appTopBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Crear lista")
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = state.lists, key = { it.id }) { list ->
                            ShoppingListRow(
                                list = list,
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

    if (showCreateDialog) {
        ListNameDialog(
            title = "Nueva lista",
            confirmText = "Crear",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                onCreate(name)
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
            Text(
                text = list.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
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