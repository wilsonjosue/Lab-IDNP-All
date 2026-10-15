package com.example.planointeractivo_compose

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PlanoAmbienteScreen(
    viewModel: PlanoViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.cargarMarcadoresDesdeAssets(context)
    }

    val marcadores = viewModel.marcadores
    val marcadorSeleccionado = viewModel.marcadorSeleccionado

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Plano del Pabellón de Sistemas",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF1A237E)
        )
        Text(
            text = "Toca un marcador circular para ver los detalles del ambiente",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF0F0F0))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(marcadores) {
                        detectTapGestures { offset ->
                            val tocado = marcadores.find { marcador ->
                                (offset - marcador.offset).getDistance() <= marcador.radio
                            }
                            viewModel.onMarcadorSeleccionado(tocado)
                        }
                    }
            ) {

                drawRect(color = Color(0xFFBBDEFB), topLeft = Offset(20f, 20f), size = Size(150f, 180f))
                drawRect(color = Color(0xFFFFE0B2), topLeft = Offset(190f, 20f), size = Size(150f, 180f))
                drawRect(color = Color(0xFFC8E6C9), topLeft = Offset(20f, 220f), size = Size(150f, 180f))
                drawRect(color = Color(0xFFE1BEE7), topLeft = Offset(190f, 220f), size = Size(150f, 180f))

                drawLine(
                    color = Color.DarkGray,
                    start = Offset(180f, 10f),
                    end = Offset(180f, 415f),
                    strokeWidth = 5f
                )
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(10f, 210f),
                    end = Offset(350f, 210f),
                    strokeWidth = 5f
                )

                val paintTexto = Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 24f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }

                drawContext.canvas.nativeCanvas.apply {
                    drawText("CiTeSoft", 95f, 60f, paintTexto)
                    drawText("Auditorio", 265f, 60f, paintTexto)
                    drawText("Taller Robótica", 95f, 260f, paintTexto)
                    drawText("Patio Central", 265f, 260f, paintTexto)
                }

                marcadores.forEach { marcador ->
                    drawCircle(
                        color = Color(0xFFE53935),
                        radius = marcador.radio,
                        center = marcador.offset
                    )
                    drawCircle(
                        color = Color.White,
                        radius = marcador.radio * 0.4f,
                        center = marcador.offset
                    )
                }
            }
        }
    }

    marcadorSeleccionado?.let { marcador ->
        AlertDialog(
            onDismissRequest = { viewModel.onMarcadorSeleccionado(null) },
            title = { Text(text = marcador.titulo, fontWeight = FontWeight.Bold) },
            text = { Text(text = marcador.descripcion) },
            confirmButton = {
                TextButton(onClick = { viewModel.onMarcadorSeleccionado(null) }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
