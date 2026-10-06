package com.example.tarjetascampus_compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tarjetascampus_compose.ui.theme.TarjetasCampus_ComposeTheme

// ───────────────────────────────────────────────
// Modelo de datos para cada edificio del campus
// ───────────────────────────────────────────────
data class Edificio(
    val nombre: String,
    val descripcion: String,
    val imagenRes: Int,
    val colorFondo: Color
)

// Lista de al menos 4 edificios del campus (Ejercicio propuesto 3)
val listaEdificios = listOf(
    Edificio(
        nombre = "Edificio Principal UNSA",
        descripcion = "Sede central de la Universidad Nacional de San Agustín de Arequipa",
        imagenRes = R.drawable.unsa_principal,
        colorFondo = Color(0xFFE8EAF6)
    ),
    Edificio(
        nombre = "Biblioteca Central",
        descripcion = "Zona de estudio y préstamo de libros",
        imagenRes = R.drawable.biblioteca,
        colorFondo = Color(0xFFE3F2FD)
    ),
    Edificio(
        nombre = "Comedor Universitario",
        descripcion = "Servicio de alimentación para estudiantes",
        imagenRes = R.drawable.comedor,
        colorFondo = Color(0xFFFFF3E0)
    ),
    Edificio(
        nombre = "Pabellón de Ingenierías",
        descripcion = "Facultad de Ingeniería de Producción y Servicios",
        imagenRes = R.drawable.pabellon_ingenierias,
        colorFondo = Color(0xFFE8F5E9)
    )
)

// ───────────────────────────────────────────────
// Rutas de navegación (mismo patrón del Lab 05)
// Se agrega la ruta "detalle/{indice}" para el Lab 06
// ───────────────────────────────────────────────
sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Home")
    object Edificios : Screen("edificios", "Edificios")
    object Mapa : Screen("mapa", "Mapa")
    object Perfil : Screen("perfil", "Perfil")
}

// ───────────────────────────────────────────────
// MainScreen con bottom navigation (Lab 05)
// + ruta de detalle añadida (Lab 06)
// ───────────────────────────────────────────────
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
            // Pantalla Home con ViewModel compartido (Lab 05)
            composable(Screen.Home.route) {
                HomeScreen(viewModel = viewModel)
            }

            // EJERCICIO PROPUESTO 3: EdificiosScreen ahora usa TarjetaLugar/TarjetaCompacta
            composable(Screen.Edificios.route) {
                EdificiosScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable(Screen.Mapa.route) {
                MapaScreen()
            }

            composable(Screen.Perfil.route) {
                PerfilScreen()
            }

            // EJERCICIO PROPUESTO 1 y 2: Ruta de detalle del edificio (Lab 06)
            composable("detalle/{indice}") { backStackEntry ->
                val indice = backStackEntry.arguments?.getString("indice")?.toIntOrNull() ?: 0
                val edificio = listaEdificios[indice]
                DetalleLugarScreen(
                    nombre = edificio.nombre,
                    descripcion = edificio.descripcion,
                    imagen = painterResource(id = edificio.imagenRes),
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// ───────────────────────────────────────────────
// HomeScreen (Lab 05) — lee el estado del ViewModel
// ───────────────────────────────────────────────
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
// EJERCICIO RESUELTO - Estilo 1: TarjetaLugar
// (Row / Franja Horizontal con imagen circular)
// ───────────────────────────────────────────────
@Composable
fun TarjetaLugar(
    nombre: String,
    descripcion: String,
    imagen: Painter,
    colorFondo: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colorFondo,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = imagen,
                contentDescription = nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = descripcion,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ───────────────────────────────────────────────
// EJERCICIO RESUELTO - Estilo 2: TarjetaCompacta
// (Box / Apilado con Scrim)
// ───────────────────────────────────────────────
@Composable
fun TarjetaCompacta(
    nombre: String,
    imagen: Painter,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = imagen,
            contentDescription = nombre,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.35f))
        )
        Text(
            text = nombre,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )
    }
}

// ───────────────────────────────────────────────
// EJERCICIO PROPUESTO 3: EdificiosScreen reemplazada
// Antes: lista simple con nombre + botón "Ver"
// Ahora: lista de TarjetaLugar + TarjetaCompacta
//        con datos de 4 edificios del campus
//
// EJERCICIO PROPUESTO 2: Modifier.clickable para navegar
//        al detalle al pulsar una tarjeta
// ───────────────────────────────────────────────
@Composable
fun EdificiosScreen(viewModel: SeleccionViewModel, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Sección: Estilo 1 – Tarjeta Lugar (Row) ──
        item {
            Text(
                text = "Estilo 1 – Tarjeta Lugar (Row)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        itemsIndexed(listaEdificios) { indice, edificio ->
            TarjetaLugar(
                nombre = edificio.nombre,
                descripcion = edificio.descripcion,
                imagen = painterResource(id = edificio.imagenRes),
                colorFondo = edificio.colorFondo,
                // Ejercicio propuesto 2: Modifier.clickable para navegar al detalle
                modifier = Modifier.clickable {
                    viewModel.seleccionarEdificio(edificio.nombre)
                    navController.navigate("detalle/$indice")
                }
            )
        }

        // ── Separador entre estilos ──
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Estilo 2 – Tarjeta Compacta (Box)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // ── Sección: Estilo 2 – Tarjeta Compacta (Box) ──
        itemsIndexed(listaEdificios) { indice, edificio ->
            TarjetaCompacta(
                nombre = edificio.nombre,
                imagen = painterResource(id = edificio.imagenRes),
                // Ejercicio propuesto 2: Modifier.clickable para navegar al detalle
                modifier = Modifier.clickable {
                    viewModel.seleccionarEdificio(edificio.nombre)
                    navController.navigate("detalle/$indice")
                }
            )
        }
    }
}

// ───────────────────────────────────────────────
// EJERCICIO PROPUESTO 1: DetalleLugarScreen
// Muestra la información del lugar a tamaño completo,
// reutilizando el estilo de TarjetaLugar.
// Usa navController.navigate(...) y lambda onBack
// con el mismo patrón de navegación del Lab 05.
// ───────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleLugarScreen(
    nombre: String,
    descripcion: String,
    imagen: Painter,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = nombre) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen a tamaño completo
            Image(
                painter = imagen,
                contentDescription = nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nombre del lugar
            Text(
                text = nombre,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Descripción del lugar
            Text(
                text = descripcion,
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Reutilización del estilo TarjetaLugar dentro del detalle
            TarjetaLugar(
                nombre = nombre,
                descripcion = descripcion,
                imagen = imagen,
                colorFondo = Color(0xFFF5F5F5),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ───────────────────────────────────────────────
// MapaScreen (Lab 05)
// ───────────────────────────────────────────────
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

// ───────────────────────────────────────────────
// Previews
// ───────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun TarjetaLugarPreview() {
    TarjetasCampus_ComposeTheme {
        TarjetaLugar(
            nombre = "Biblioteca Central",
            descripcion = "Zona de estudio y préstamo de libros",
            imagen = painterResource(id = R.drawable.biblioteca),
            colorFondo = Color(0xFFE3F2FD)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TarjetaCompactaPreview() {
    TarjetasCampus_ComposeTheme {
        TarjetaCompacta(
            nombre = "Comedor Universitario",
            imagen = painterResource(id = R.drawable.comedor)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetalleLugarScreenPreview() {
    TarjetasCampus_ComposeTheme {
        DetalleLugarScreen(
            nombre = "Biblioteca Central",
            descripcion = "Zona de estudio y préstamo de libros",
            imagen = painterResource(id = R.drawable.biblioteca),
            onBack = {}
        )
    }
}
