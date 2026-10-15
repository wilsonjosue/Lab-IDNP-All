package com.example.planointeractivo_compose

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Marcador(
    val offset: Offset,
    val radio: Float,
    val titulo: String,
    val descripcion: String
)

data class MarcadorDto(
    val id: Int,
    val x: Float,
    val y: Float,
    val radio: Float,
    val titulo: String,
    val descripcion: String
)

class PlanoViewModel : ViewModel() {
    var marcadores by mutableStateOf<List<Marcador>>(emptyList())
        private set

    var marcadorSeleccionado by mutableStateOf<Marcador?>(null)
        private set

    fun cargarMarcadoresDesdeAssets(context: Context) {
        if (marcadores.isNotEmpty()) return

        try {
            val jsonString = context.assets.open("ambientes.json").bufferedReader().use { it.readText() }
            val tipoLista = object : TypeToken<List<MarcadorDto>>() {}.type
            val listaDto: List<MarcadorDto> = Gson().fromJson(jsonString, tipoLista)

            marcadores = listaDto.map { dto ->
                Marcador(
                    offset = Offset(dto.x, dto.y),
                    radio = dto.radio,
                    titulo = dto.titulo,
                    descripcion = dto.descripcion
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onMarcadorSeleccionado(marcador: Marcador?) {
        marcadorSeleccionado = marcador
    }
}
