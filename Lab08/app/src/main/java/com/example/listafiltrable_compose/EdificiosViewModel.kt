package com.example.listafiltrable_compose

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class EdificiosViewModel : ViewModel() {
    var listaCompleta by mutableStateOf<List<Edificio>>(emptyList())
        private set

    var textoBusqueda by mutableStateOf("")

    var categoriaSeleccionada by mutableStateOf("Todas")

    // Ejercicio 1: lee assets/edificios.txt con formato id;nombre;categoria;descripcion
    fun cargarDatosDesdeArchivo(context: Context) {
        if (listaCompleta.isNotEmpty()) return

        try {
            val lineas = context.assets.open("edificios.txt").bufferedReader().readLines()
            listaCompleta = lineas.filter { it.isNotBlank() }.map { linea ->
                val partes = linea.split(";")
                Edificio(
                    id = partes[0].trim().toInt(),
                    nombre = partes[1].trim(),
                    categoria = partes[2].trim(),
                    descripcion = partes[3].trim()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Ejercicio 3: combina el filtro por texto con el filtro por categoría
    val edificiosFiltrados: List<Edificio>
        get() = listaCompleta.filter { edificio ->
            val coincideTexto = edificio.nombre.contains(textoBusqueda, ignoreCase = true) ||
                    edificio.descripcion.contains(textoBusqueda, ignoreCase = true)
            val coincideCategoria = categoriaSeleccionada == "Todas" || edificio.categoria == categoriaSeleccionada
            coincideTexto && coincideCategoria
        }
}
