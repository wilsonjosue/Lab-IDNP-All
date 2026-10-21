package com.example.listafiltrable_compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.listafiltrable_compose.ui.theme.ListaFiltrable_ComposeTheme

// ───────────────────────────────────────────────
// TarjetaLugar (Lab 06) adaptada para el Lab 08
// - Se agrega la línea de categoría (3 datos por fila)
// - Si el edificio no tiene foto, el círculo muestra
//   el ícono de su categoría (ejercicio 2)
// ───────────────────────────────────────────────
@Composable
fun TarjetaLugar(
    nombre: String,
    categoria: String,
    descripcion: String,
    imagen: Painter?,
    icono: ImageVector,
    colorFondo: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colorFondo,
        // Texto oscuro fijo: el fondo pastel no cambia en modo oscuro
        contentColor = Color(0xFF1C1B1F),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            if (imagen != null) {
                Image(
                    painter = imagen,
                    contentDescription = nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = categoria,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = categoria.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF49454F)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = descripcion,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TarjetaLugarPreview() {
    ListaFiltrable_ComposeTheme {
        TarjetaLugar(
            nombre = "Pabellón Nicholson",
            categoria = "Académico",
            descripcion = "Edificio histórico y emblemático de Ingeniería Civil.",
            imagen = null,
            icono = Icons.Default.School,
            colorFondo = Color(0xFFE8EAF6)
        )
    }
}
