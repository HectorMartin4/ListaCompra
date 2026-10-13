package com.hmc.listacompracompose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hmc.listacompracompose.features.categories.presentation.CategoriesView
import com.hmc.listacompracompose.features.items.presentation.itemList.ItemListView
import com.hmc.listacompracompose.features.lists.presentation.ShoppingListsView
import com.hmc.listacompracompose.features.lists.presentation.detail.ShoppingListDetailView
import com.hmc.listacompracompose.ui.theme.ListaCompraComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d("AppListaCompra", "ACTIVITY: onCreate iniciado")

        setContent {
            ListaCompraComposeTheme {
                App()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object ShoppingList : Screen("shopping_list", "Lista", Icons.AutoMirrored.Filled.List)
    data object Catalog : Screen("catalog", "Catálogo", Icons.Default.ShoppingCart)
}

object Routes {
    const val LIST_DETAIL = "list_detail/{listId}"
    const val CATEGORIES = "categories"
    fun listDetail(listId: Int) = "list_detail/$listId"
}

@Composable
fun App() {
    val navController = rememberNavController()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            BottomBar(navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.ShoppingList.route,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            composable(Screen.ShoppingList.route) {
                ShoppingListsView(
                    onListClick = { listId -> navController.navigate(Routes.listDetail(listId)) }
                )
            }

            composable(Screen.Catalog.route) {
                ItemListView(
                    onOpenCategories = { navController.navigate(Routes.CATEGORIES) }
                )
            }

            composable(Routes.CATEGORIES) {
                CategoriesView(onBack = { navController.popBackStack() })
            }

            composable(
                route = Routes.LIST_DETAIL,
                arguments = listOf(navArgument("listId") { type = NavType.IntType })
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getInt("listId") ?: return@composable
                ShoppingListDetailView(
                    listId = listId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf(Screen.ShoppingList, Screen.Catalog)

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route ||
                        (screen == Screen.ShoppingList && currentRoute == Routes.LIST_DETAIL) ||
                        (screen == Screen.Catalog && currentRoute == Routes.CATEGORIES),
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) }
            )
        }
    }
}

