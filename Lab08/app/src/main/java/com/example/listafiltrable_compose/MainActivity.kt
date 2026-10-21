package com.example.listafiltrable_compose

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.listafiltrable_compose.ui.theme.ListaFiltrable_ComposeTheme

data class Edificio(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val imagen: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListaFiltrable_ComposeTheme {
                // Ejercicio 4: la app arranca en la navegación del Lab 06
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaEdificios(
    viewModel: EdificiosViewModel = viewModel(),
    onEdificioClick: (Edificio) -> Unit = {}
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.cargarDatosDesdeArchivo(context)
    }

    // Ejercicio 3: categorías disponibles para el filtro
    val categorias = listOf("Todas", "Académico", "Investigación", "Deportes", "Servicios")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Directorio - Campus Ingenierías",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = viewModel.textoBusqueda,
            onValueChange = { viewModel.textoBusqueda = it },
            label = { Text("Buscar por nombre o descripción...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categorias) { cat ->
                FilterChip(
                    selected = viewModel.categoriaSeleccionada == cat,
                    onClick = { viewModel.categoriaSeleccionada = cat },
                    label = { Text(cat) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val resultados = viewModel.edificiosFiltrados
        if (resultados.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron edificios con los filtros aplicados",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(resultados, key = { it.id }) { edificio ->
                    FilaEdificio(edificio, onClick = { onEdificioClick(edificio) })
                }
            }
        }
    }
}

// Ejercicio 2: ícono representativo según la categoría
fun iconoPorCategoria(categoria: String): ImageVector = when (categoria) {
    "Académico" -> Icons.Default.School
    "Investigación" -> Icons.Default.Build
    "Deportes" -> Icons.Default.SportsSoccer
    "Servicios" -> Icons.AutoMirrored.Filled.MenuBook
    else -> Icons.Default.School
}

// Ejercicio 4: colores de fondo del Lab 06, ahora asignados por categoría
fun colorPorCategoria(categoria: String): Color = when (categoria) {
    "Académico" -> Color(0xFFE8EAF6)
    "Investigación" -> Color(0xFFE3F2FD)
    "Deportes" -> Color(0xFFE8F5E9)
    "Servicios" -> Color(0xFFFFF3E0)
    else -> Color(0xFFF5F5F5)
}

// Ejercicio 4: cada fila de la lista es una TarjetaLugar del Lab 06
@SuppressLint("DiscouragedApi")
@Composable
fun FilaEdificio(edificio: Edificio, onClick: () -> Unit = {}) {
    val context = LocalContext.current
    val resources = LocalResources.current
    // Busca en res/drawable la foto indicada en edificios.txt (0 si no existe)
    val imagenRes = remember(edificio.imagen) {
        if (edificio.imagen.isBlank()) 0
        else resources.getIdentifier(edificio.imagen, "drawable", context.packageName)
    }

    TarjetaLugar(
        nombre = edificio.nombre,
        categoria = edificio.categoria,
        descripcion = edificio.descripcion,
        imagen = if (imagenRes != 0) painterResource(id = imagenRes) else null,
        icono = iconoPorCategoria(edificio.categoria),
        colorFondo = colorPorCategoria(edificio.categoria),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    )
}

@Preview(showBackground = true)
@Composable
fun ListaEdificiosPreview() {
    ListaFiltrable_ComposeTheme {
        ListaEdificios()
    }
}
