package com.hmc.listacompracompose.features.categories.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hmc.listacompracompose.features.categories.domain.CategoryModel
import com.hmc.listacompracompose.ui.theme.appTopBarColors
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun CategoriesView(
    onBack: () -> Unit,
    viewModel: CategoriesViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CategoriesContent(
        state = state,
        onBack = onBack,
        onCreate = viewModel::createCategory,
        onRename = viewModel::renameCategory,
        onDelete = viewModel::deleteCategory,
        onReorder = viewModel::reorder,
        onErrorShown = viewModel::errorShown
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesContent(
    state: CategoriesUiState,
    onBack: () -> Unit,
    onCreate: (String) -> Unit,
    onRename: (id: Int, name: String) -> Unit,
    onDelete: (id: Int) -> Unit,
    onReorder: (orderedIds: List<Int>) -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var categoryToRename by remember { mutableStateOf<CategoryModel?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryModel?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    var localList by remember(state.categories) { mutableStateOf(state.categories) }
    val latestList by rememberUpdatedState(localList)

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localList = localList.toMutableList().apply { add(to.index, removeAt(from.index)) }
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
                title = { Text("Categorías") },
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
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Crear categoría")
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

                state.categories.isEmpty() -> {
                    Text(
                        text = "Aún no tienes categorías.\nPulsa + para crear la primera.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = localList, key = { it.id }) { category ->
                            ReorderableItem(reorderableState, key = category.id) { isDragging ->
                                val elevation by animateDpAsState(
                                    targetValue = if (isDragging) 8.dp else 2.dp,
                                    label = "dragElevation"
                                )
                                Card(
                                    onClick = { categoryToRename = category },
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = elevation)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Arrastrar para reordenar",
                                            modifier = Modifier
                                                .draggableHandle(
                                                    onDragStopped = {
                                                        onReorder(latestList.map { it.id })
                                                    }
                                                )
                                                .padding(12.dp)
                                        )
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = { categoryToDelete = category }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Borrar categoría",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CategoryNameDialog(
            title = "Nueva categoría",
            confirmText = "Crear",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                onCreate(name)
                showCreateDialog = false
            }
        )
    }

    categoryToRename?.let { category ->
        CategoryNameDialog(
            title = "Renombrar categoría",
            confirmText = "Guardar",
            initialName = category.name,
            onDismiss = { categoryToRename = null },
            onConfirm = { name ->
                onRename(category.id, name)
                categoryToRename = null
            }
        )
    }

    categoryToDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Borrar categoría") },
            text = {
                Text("Se borrará \"${category.name}\". Sus productos no se borran: quedarán sin categoría.")
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(category.id)
                    categoryToDelete = null
                }) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun CategoryNameDialog(
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
                label = { Text("Nombre de la categoría") },
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