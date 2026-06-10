package com.hmc.listacompracompose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hmc.listacompracompose.features.items.presentation.ListaCompraPruebaScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AppListaCompra", "ACTIVITY: onCreate iniciado")

        setContent {
            App()
        }
    }
}

@Composable
fun App() {

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomBar(navController)
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = "item_list",
            modifier = Modifier.padding(padding)
        ) {
            composable("item_list") {
                ListaCompraPruebaScreen()
            }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {

    NavigationBar {

        val currentRoute = navController
            .currentBackStackEntryAsState().value?.destination?.route

        NavigationBarItem(
            selected = currentRoute == "item_list",
            onClick = {
                navController.navigate("item_list") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.List, null) },
            label = { Text("Lista") }
        )
    }
}

