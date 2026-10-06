package com.example.tarjetascampus_compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * ViewModel compartido para persistir la selección del edificio a lo largo
 * del ciclo de vida de la navegación. (Mismo patrón del Lab 05)
 */
class SeleccionViewModel : ViewModel() {

    // Estado reactivo observable por Compose
    var edificioSeleccionado by mutableStateOf("Ninguno")
        private set

    // Método para mutar el estado desde EdificiosScreen
    fun seleccionarEdificio(nuevoEdificio: String) {
        edificioSeleccionado = nuevoEdificio
    }
}
