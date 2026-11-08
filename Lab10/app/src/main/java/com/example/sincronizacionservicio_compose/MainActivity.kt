package com.example.sincronizacionservicio_compose

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.room.*
import com.example.sincronizacionservicio_compose.ui.theme.SincronizacionServicio_ComposeTheme
import kotlinx.coroutines.*
import kotlin.random.Random

// Acciones de los broadcasts que emite el Service
const val ACCION_SYNC_LISTA = "com.example.sincronizacionservicio_compose.ACCION_SYNC_LISTA"
const val ACCION_SYNC_ERROR = "com.example.sincronizacionservicio_compose.ACCION_SYNC_ERROR"

// Extras
const val EXTRA_EDIFICIOS = "edificios_actualizados"
const val EXTRA_MENSAJE_ERROR = "mensaje_error"
const val EXTRA_FORZAR_ERROR = "forzar_error"

// Ejercicio 3: probabilidad de que la sincronización falle (30 %)
const val PROBABILIDAD_ERROR = 0.30

@Entity
data class EdificioSync(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val requiereSync: Boolean
)

@Dao
interface EdificioSyncDao {
    @Query("SELECT * FROM EdificioSync WHERE requiereSync = 1")
    fun obtenerPendientesDeSync(): List<EdificioSync>

    @Insert
    fun insertar(edificio: EdificioSync)
}

@Database(entities = [EdificioSync::class], version = 1)
abstract class SyncDatabase : RoomDatabase() {
    abstract fun dao(): EdificioSyncDao
}

class SincronizacionService : Service() {
    private var job: Job? = null
    private lateinit var db: SyncDatabase

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(applicationContext, SyncDatabase::class.java, "sync.db").build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val forzarError = intent?.getBooleanExtra(EXTRA_FORZAR_ERROR, false) ?: false

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("SYNC", "1. Servicio iniciando sincronización...")
                insertarDatosInicialesSiFaltan()
                delay(4000)

                // Ejercicio 3: simulamos una falla (servidor caído) con probabilidad aleatoria
                if (forzarError || Random.nextDouble() < PROBABILIDAD_ERROR) {
                    throw IllegalStateException("No se pudo conectar con el servidor (timeout simulado)")
                }

                val pendientes = db.dao().obtenerPendientesDeSync()
                val cantidadReal = pendientes.size

                val resultado = Intent(ACCION_SYNC_LISTA)
                resultado.setPackage(packageName)
                resultado.putExtra(EXTRA_EDIFICIOS, cantidadReal)
                sendBroadcast(resultado)
                Log.d("SYNC", "2. Éxito: Sincronizados $cantidadReal registros de Room.")

            } catch (e: CancellationException) {
                // Debe ir antes de Exception: CancellationException también es una Exception
                Log.d("SYNC", "Operación cancelada por el usuario.")
            } catch (e: Exception) {
                val error = Intent(ACCION_SYNC_ERROR)
                error.setPackage(packageName)
                error.putExtra(EXTRA_MENSAJE_ERROR, e.message ?: "Error desconocido")
                sendBroadcast(error)
                Log.e("SYNC", "2. Error: ${e.message}")
            } finally {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    // Se ejecuta dentro de la misma corrutina para que los datos existan antes de contarlos
    private fun insertarDatosInicialesSiFaltan() {
        if (db.dao().obtenerPendientesDeSync().isEmpty()) {
            db.dao().insertar(EdificioSync(nombre = "Pabellón Nicholson", requiereSync = true))
            db.dao().insertar(EdificioSync(nombre = "INFOUNSA", requiereSync = true))
            db.dao().insertar(EdificioSync(nombre = "Facultad de Arquitectura", requiereSync = true))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        Log.d("SYNC", "Servicio destruido y corrutina cancelada.")
    }
}

@Composable
fun PantallaSincronizacion() {
    val context = LocalContext.current
    var resultado by remember { mutableStateOf<String?>(null) }
    var esError by remember { mutableStateOf(false) }
    var estaSincronizando by remember { mutableStateOf(false) }
    var forzarError by remember { mutableStateOf(false) }

    // Receiver 1: sincronización exitosa
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                Log.d("SYNC", "3. Aviso de éxito recibido en la pantalla")
                val cantidad = intent?.getIntExtra(EXTRA_EDIFICIOS, 0) ?: 0
                resultado = "Sincronización completa: $cantidad edificios desde Room"
                esError = false
                estaSincronizando = false
            }
        }

        val filtro = IntentFilter(ACCION_SYNC_LISTA)
        ContextCompat.registerReceiver(context, receiver, filtro, ContextCompat.RECEIVER_NOT_EXPORTED)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Receiver 2 (Ejercicio 3): escucha la acción de error
    DisposableEffect(Unit) {
        val receiverError = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                Log.d("SYNC", "3. Aviso de ERROR recibido en la pantalla")
                val mensaje = intent?.getStringExtra(EXTRA_MENSAJE_ERROR) ?: "Error desconocido"
                resultado = "Error en la sincronización: $mensaje. Intenta de nuevo."
                esError = true
                estaSincronizando = false
            }
        }

        val filtroError = IntentFilter(ACCION_SYNC_ERROR)
        ContextCompat.registerReceiver(context, receiverError, filtroError, ContextCompat.RECEIVER_NOT_EXPORTED)

        onDispose {
            context.unregisterReceiver(receiverError)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    resultado = null
                    esError = false
                    estaSincronizando = true
                    val intent = Intent(context, SincronizacionService::class.java)
                    intent.putExtra(EXTRA_FORZAR_ERROR, forzarError)
                    context.startService(intent)
                },
                enabled = !estaSincronizando
            ) {
                Text("Sincronizar")
            }

            Button(
                onClick = {
                    estaSincronizando = false
                    esError = false
                    resultado = "Sincronización cancelada manualmente."
                    context.stopService(Intent(context, SincronizacionService::class.java))
                },
                enabled = estaSincronizando,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Detener")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interruptor de prueba para forzar el caso de error sin depender del azar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = forzarError,
                onCheckedChange = { forzarError = it },
                enabled = !estaSincronizando
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Forzar error (prueba)")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (estaSincronizando) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Procesando datos locales...")
        } else {
            Text(
                text = resultado ?: "Presiona 'Sincronizar' para iniciar",
                color = if (esError) MaterialTheme.colorScheme.error else Color.Unspecified,
                fontWeight = if (esError) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SincronizacionServicio_ComposeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PantallaSincronizacion()
                }
            }
        }
    }
}
