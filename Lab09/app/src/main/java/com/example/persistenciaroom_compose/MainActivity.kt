package com.example.persistenciaroom_compose

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import com.example.persistenciaroom_compose.ui.theme.PersistenciaRoom_ComposeTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Entity
data class EdificioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val categoria: String
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = EdificioEntity::class,
            parentColumns = ["id"],
            childColumns = ["edificioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("edificioId")]
)
data class AmbienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val edificioId: Int,
    val nombre: String
)

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = AmbienteEntity::class,
            parentColumns = ["id"],
            childColumns = ["ambienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ambienteId")]
)
data class NotaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ambienteId: Int,
    val texto: String
)

data class EdificioConAmbientes(
    @Embedded val edificio: EdificioEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "edificioId"
    )
    val ambientes: List<AmbienteEntity>
)

data class AmbienteConNotas(
    @Embedded val ambiente: AmbienteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "ambienteId"
    )
    val notas: List<NotaEntity>
)

@Dao
interface EdificioDao {
    @Insert
    suspend fun insertarEdificio(edificio: EdificioEntity): Long

    @Update
    suspend fun actualizarEdificio(edificio: EdificioEntity)

    @Delete
    suspend fun eliminarEdificio(edificio: EdificioEntity)

    @Query("SELECT * FROM EdificioEntity")
    suspend fun obtenerTodosLosEdificios(): List<EdificioEntity>

    @Insert
    suspend fun insertarAmbiente(ambiente: AmbienteEntity): Long

    @Update
    suspend fun actualizarAmbiente(ambiente: AmbienteEntity)

    @Delete
    suspend fun eliminarAmbiente(ambiente: AmbienteEntity)

    @Insert
    suspend fun insertarNota(nota: NotaEntity): Long

    @Update
    suspend fun actualizarNota(nota: NotaEntity)

    @Delete
    suspend fun eliminarNota(nota: NotaEntity)

    @Transaction
    @Query("SELECT * FROM EdificioEntity")
    suspend fun obtenerConAmbientes(): List<EdificioConAmbientes>

    @Transaction
    @Query("SELECT * FROM AmbienteEntity")
    suspend fun obtenerAmbientesConNotas(): List<AmbienteConNotas>
}

@Database(
    entities = [EdificioEntity::class, AmbienteEntity::class, NotaEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun edificioDao(): EdificioDao
}

fun crearBaseDeDatos(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app.db"
    ).fallbackToDestructiveMigration(dropAllTables = true).build()
}

class EdificiosViewModel(private val dao: EdificioDao) : ViewModel() {
    private val _edificios = MutableStateFlow<List<EdificioConAmbientes>>(emptyList())
    val edificios: StateFlow<List<EdificioConAmbientes>> = _edificios

    private val _ambientesConNotas = MutableStateFlow<List<AmbienteConNotas>>(emptyList())
    val ambientesConNotas: StateFlow<List<AmbienteConNotas>> = _ambientesConNotas

    init {
        viewModelScope.launch {
            if (dao.obtenerTodosLosEdificios().isEmpty()) {
                val idEdificio1 = dao.insertarEdificio(
                    EdificioEntity(nombre = "Pabellón Nicholson", categoria = "Académico")
                )
                val idEdificio2 = dao.insertarEdificio(
                    EdificioEntity(nombre = "CiTeSoft UNSA", categoria = "Investigación")
                )

                val idAmbiente1 = dao.insertarAmbiente(
                    AmbienteEntity(edificioId = idEdificio1.toInt(), nombre = "Aula Teórica 101")
                )
                val idAmbiente2 = dao.insertarAmbiente(
                    AmbienteEntity(edificioId = idEdificio1.toInt(), nombre = "Laboratorio de Estructuras")
                )
                val idAmbiente3 = dao.insertarAmbiente(
                    AmbienteEntity(edificioId = idEdificio2.toInt(), nombre = "Área de Desarrollo I+D")
                )

                dao.insertarNota(
                    NotaEntity(ambienteId = idAmbiente1.toInt(), texto = "Proyector multimedia operativo")
                )
                dao.insertarNota(
                    NotaEntity(ambienteId = idAmbiente1.toInt(), texto = "Capacidad máxima para 40 alumnos")
                )
                dao.insertarNota(
                    NotaEntity(ambienteId = idAmbiente2.toInt(), texto = "Requiere mantenimiento de prensas")
                )
                dao.insertarNota(
                    NotaEntity(ambienteId = idAmbiente3.toInt(), texto = "Acceso restringido con tarjeta RFID")
                )
            }

            recargarDatos()
        }
    }

    private suspend fun recargarDatos() {
        _edificios.value = dao.obtenerConAmbientes()
        _ambientesConNotas.value = dao.obtenerAmbientesConNotas()
    }

    // Ejercicio 4: cada operación escribe en Room y luego vuelve a leer ambas consultas relacionales
    fun actualizarEdificio(edificio: EdificioEntity) {
        viewModelScope.launch {
            dao.actualizarEdificio(edificio)
            recargarDatos()
        }
    }

    // Por el CASCADE se eliminan también sus ambientes y las notas de esos ambientes
    fun eliminarEdificio(edificio: EdificioEntity) {
        viewModelScope.launch {
            dao.eliminarEdificio(edificio)
            recargarDatos()
        }
    }

    fun actualizarAmbiente(ambiente: AmbienteEntity) {
        viewModelScope.launch {
            dao.actualizarAmbiente(ambiente)
            recargarDatos()
        }
    }

    fun eliminarAmbiente(ambiente: AmbienteEntity) {
        viewModelScope.launch {
            dao.eliminarAmbiente(ambiente)
            recargarDatos()
        }
    }
}

class EdificiosViewModelFactory(private val dao: EdificioDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EdificiosViewModel(dao) as T
    }
}

@Composable
fun ListaPersistenciaScreen(viewModel: EdificiosViewModel, modifier: Modifier = Modifier) {
    val listaEdificios by viewModel.edificios.collectAsState()
    val listaAmbientesConNotas by viewModel.ambientesConNotas.collectAsState()

    // Elemento seleccionado para editar o eliminar; null = diálogo cerrado
    var edificioEditando by remember { mutableStateOf<EdificioEntity?>(null) }
    var edificioAEliminar by remember { mutableStateOf<EdificioEntity?>(null) }
    var ambienteEditando by remember { mutableStateOf<AmbienteEntity?>(null) }
    var ambienteAEliminar by remember { mutableStateOf<AmbienteEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "1. Edificios y sus Ambientes",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (listaEdificios.isEmpty()) {
            item {
                Text(text = "(No hay edificios registrados)", fontSize = 14.sp, color = Color.Gray)
            }
        }

        items(listaEdificios, key = { "edificio_${it.edificio.id}" }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${item.edificio.nombre} (${item.edificio.categoria})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Row {
                        TextButton(onClick = { edificioEditando = item.edificio }) {
                            Text("Editar edificio")
                        }
                        TextButton(onClick = { edificioAEliminar = item.edificio }) {
                            Text("Eliminar edificio", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    HorizontalDivider()
                    item.ambientes.forEach { ambiente ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = " • Ambiente: ${ambiente.nombre}",
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { ambienteEditando = ambiente }) {
                                Text("Editar", fontSize = 13.sp)
                            }
                            TextButton(onClick = { ambienteAEliminar = ambiente }) {
                                Text("Eliminar", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "2. Ambientes y sus Notas (@Relation)",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        items(listaAmbientesConNotas, key = { "ambiente_${it.ambiente.id}" }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Ambiente: ${item.ambiente.nombre}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (item.notas.isEmpty()) {
                        Text(text = "   (Sin notas registradas)", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        item.notas.forEach { nota ->
                            Text(
                                text = "   - Nota: \"${nota.texto}\"",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }

    edificioEditando?.let { edificio ->
        EditarEdificioDialog(
            edificio = edificio,
            onConfirmar = {
                viewModel.actualizarEdificio(it)
                edificioEditando = null
            },
            onCancelar = { edificioEditando = null }
        )
    }

    ambienteEditando?.let { ambiente ->
        EditarAmbienteDialog(
            ambiente = ambiente,
            onConfirmar = {
                viewModel.actualizarAmbiente(it)
                ambienteEditando = null
            },
            onCancelar = { ambienteEditando = null }
        )
    }

    edificioAEliminar?.let { edificio ->
        ConfirmarEliminacionDialog(
            mensaje = "¿Eliminar \"${edificio.nombre}\"? También se eliminarán sus ambientes y notas.",
            onConfirmar = {
                viewModel.eliminarEdificio(edificio)
                edificioAEliminar = null
            },
            onCancelar = { edificioAEliminar = null }
        )
    }

    ambienteAEliminar?.let { ambiente ->
        ConfirmarEliminacionDialog(
            mensaje = "¿Eliminar el ambiente \"${ambiente.nombre}\"? También se eliminarán sus notas.",
            onConfirmar = {
                viewModel.eliminarAmbiente(ambiente)
                ambienteAEliminar = null
            },
            onCancelar = { ambienteAEliminar = null }
        )
    }
}

@Composable
fun EditarEdificioDialog(
    edificio: EdificioEntity,
    onConfirmar: (EdificioEntity) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf(edificio.nombre) }
    var categoria by remember { mutableStateOf(edificio.categoria) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Editar edificio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = categoria, onValueChange = { categoria = it }, label = { Text("Categoría") })
            }
        },
        confirmButton = {
            // copy() conserva el id, así @Update sabe qué fila modificar
            TextButton(
                onClick = { onConfirmar(edificio.copy(nombre = nombre.trim(), categoria = categoria.trim())) },
                enabled = nombre.isNotBlank() && categoria.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
fun EditarAmbienteDialog(
    ambiente: AmbienteEntity,
    onConfirmar: (AmbienteEntity) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf(ambiente.nombre) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Editar ambiente") },
        text = {
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmar(ambiente.copy(nombre = nombre.trim())) },
                enabled = nombre.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
fun ConfirmarEliminacionDialog(
    mensaje: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Confirmar eliminación") },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = crearBaseDeDatos(applicationContext)
        val dao = database.edificioDao()
        val factory = EdificiosViewModelFactory(dao)

        setContent {
            PersistenciaRoom_ComposeTheme {
                val viewModel: EdificiosViewModel = viewModel(factory = factory)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ListaPersistenciaScreen(viewModel, Modifier.padding(innerPadding))
                }
            }
        }
    }
}
