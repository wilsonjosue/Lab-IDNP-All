package com.example.helloworldcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.fillMaxWidth
import android.content.Context
import android.widget.Toast
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                BookRegisterScreen()
            }
        }
    }
}

@Composable
fun BookRegisterScreen() {

    var title by remember {
        mutableStateOf("")
    }
    var author by remember {
        mutableStateOf("")
    }
    var pagesRead by remember {
        mutableStateOf("")
    }
    var savedRecordText by remember {
        mutableStateOf("No se ha cargado ningún registro.")
    }

    val context = LocalContext.current

    val fileName = "libro_registro.txt"

    val tagLog = "REGISTRO_LIBRO"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Registro de Lectura",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = {
                Text("Título del libro")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = {
                Text("Autor")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = pagesRead,
            onValueChange = { pagesRead = it },
            label = {
                Text("Páginas leídas")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )

        if (
            title.isBlank() ||
            author.isBlank() ||
            pagesRead.isBlank()
        ) {

            Toast.makeText(
                context,
                "Por favor complete todos los campos",
                Toast.LENGTH_SHORT
            ).show()

            return@Button
        }

        Button(
            onClick = {

                val fileContent =
                    "Título: $title\n" +
                    "Autor: $author\n" +
                    "Páginas leídas: $pagesRead\n"

                context.openFileOutput(
                    fileName,
                    Context.MODE_PRIVATE
                ).use { outputStream ->

                    outputStream.write(
                        fileContent.toByteArray()
                    )
                }

                Toast.makeText(
                    context,
                    "¡Libro guardado exitosamente!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        ) {
            Text("Guardar")
        }
        

        OutlinedButton(
            onClick = {

                try {

                    context.openFileInput(fileName).use { inputStream ->

                        val reader =
                            BufferedReader(
                                InputStreamReader(inputStream)
                            )

                        val content = reader.readText()
                        Log.d(tagLog,"=== REGISTRO DEL LIBRO ===")
                        Log.d(tagLog,content)
                        savedRecordText = content
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                Text(
                                    text = "Contenido leído del archivo:",
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Text(
                                    text = savedRecordText,
                                    fontSize = 15.sp
                                )
                            }
                        }
                        Log.d(tagLog,"==========================")

                        Toast.makeText(
                            context,
                            "Registro cargado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (e: Exception) {

                    Toast.makeText(
                        context,
                        "No hay registros guardados",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        ) {
            Text("Ver registro")
        }
        

    }
}