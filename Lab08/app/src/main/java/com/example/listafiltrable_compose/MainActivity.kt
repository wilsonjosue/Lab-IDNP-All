package com.example.listafiltrable_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val descripcion: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListaFiltrable_ComposeTheme {
                // systemBarsPadding evita que el buscador quede debajo de la barra de estado
                Surface(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                    ListaEdificios()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaEdificios(viewModel: EdificiosViewModel = viewModel()) {
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
                    FilaEdificio(edificio)
                }
            }
        }
    }
}

// Ejercicio 2: fila con ícono representativo según la categoría
@Composable
fun FilaEdificio(edificio: Edificio) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icono = when (edificio.categoria) {
                "Académico" -> Icons.Default.School
                "Investigación" -> Icons.Default.Build
                "Deportes" -> Icons.Default.SportsSoccer
                "Servicios" -> Icons.Default.MenuBook
                else -> Icons.Default.School
            }

            Icon(
                imageVector = icono,
                contentDescription = edificio.categoria,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 12.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = edificio.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = edificio.categoria.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = edificio.descripcion,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListaEdificiosPreview() {
    ListaFiltrable_ComposeTheme {
        ListaEdificios()
    }
}
