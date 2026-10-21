package com.example.listafiltrable_compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Rutas de navegación (mismo patrón del Lab 05/06)

sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Home")
    object Edificios : Screen("edificios", "Edificios")
    object Mapa : Screen("mapa", "Mapa")
    object Perfil : Screen("perfil", "Perfil")
}

// MainScreen con bottom navigation (Lab 05/06)

@Composable
fun MainScreen(viewModel: SeleccionViewModel = viewModel()) {
    val navController = rememberNavController()

    // Lista de pantallas para la barra inferior
    val items = listOf(Screen.Home, Screen.Edificios, Screen.Mapa, Screen.Perfil)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    // Asignación de icono según la pantalla
                    val icon = when (screen) {
                        Screen.Home -> Icons.Default.Home
                        Screen.Edificios -> Icons.AutoMirrored.Filled.List
                        Screen.Mapa -> Icons.Default.Place
                        Screen.Perfil -> Icons.Default.Person
                    }

                    NavigationBarItem(
                        selected = currentRoute == screen.route,
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
                        icon = { Icon(icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(viewModel = viewModel)
            }

            // EJERCICIO PROPUESTO 4: la lista filtrable reemplaza a EdificiosScreen
            composable(Screen.Edificios.route) {
                EdificiosScreen(viewModel = viewModel)
            }

            composable(Screen.Mapa.route) {
                MapaScreen()
            }

            composable(Screen.Perfil.route) {
                PerfilScreen()
            }
        }
    }
}

// HomeScreen (Lab 05) — lee el estado del ViewModel
@Composable
fun HomeScreen(viewModel: SeleccionViewModel) {
    val edificio = viewModel.edificioSeleccionado

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Campus Navigator",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Bienvenido a la plataforma de navegación del campus universitario.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Último edificio consultado:",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = edificio,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ───────────────────────────────────────────────
// EJERCICIO 4: EdificiosScreen reemplazada
// lista filtrable del Lab 08 (texto + categoría)
//        donde cada fila es una TarjetaLugar
// ───────────────────────────────────────────────
@Composable
fun EdificiosScreen(viewModel: SeleccionViewModel) {
    ListaEdificios(
        onEdificioClick = { edificio -> viewModel.seleccionarEdificio(edificio.nombre) }
    )
}


// MapaScreen (Lab 05)

@Composable
fun MapaScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "📍 Mapa de ubicaciones del campus\n(Módulo en desarrollo)",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// ───────────────────────────────────────────────
// PerfilScreen (Lab 05)
// ───────────────────────────────────────────────
@Composable
fun PerfilScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Avatar de Perfil",
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Perfil del Estudiante",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Nombre: Estudiante IDNP", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Carrera: Ingeniería de Sistemas", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Rol: Alumno Regular", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
