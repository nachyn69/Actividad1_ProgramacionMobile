package cl.actividad.recordatorios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Modelos inmutables: las propiedades son val y las categorías no se duplican.
enum class Prioridad { ALTA, MEDIA, BAJA }
sealed class Estado {
    data object Pendiente : Estado()
    data object Completado : Estado()
    data class Pospuesto(val motivo: String) : Estado() {
        init { require(motivo.isNotBlank()) { "El motivo es obligatorio" } }
    }
}
data class Categoria(val nombre: String) {
    init { require(nombre.isNotBlank()) { "La categoría es obligatoria" } }
}
data class Recordatorio(
    val id: Int,
    val titulo: String,
    val categoria: Categoria,
    val prioridad: Prioridad,
    val estado: Estado,
    val ubicacion: String? = null,
    val aviso: String? = null
) {
    init {
        require(id > 0) { "El identificador debe ser positivo" }
        require(titulo.isNotBlank()) { "El título es obligatorio" }
        require(ubicacion == null || ubicacion.isNotBlank()) { "La ubicación no puede estar vacía" }
        require(aviso == null || aviso.isNotBlank()) { "El aviso no puede estar vacío" }
    }
}

object DatosSimulados {
    val personal = Categoria("Personal")
    val trabajo = Categoria("Trabajo")
    val estudio = Categoria("Estudio")
    val recordatorios: List<Recordatorio> = listOf(
        Recordatorio(1, "Preparar presentación", trabajo, Prioridad.ALTA, Estado.Pendiente, aviso = "Mañana 09:00"),
        Recordatorio(2, "Estudiar Kotlin", estudio, Prioridad.MEDIA, Estado.Completado),
        Recordatorio(3, "Comprar medicamentos", personal, Prioridad.ALTA, Estado.Pospuesto("Esperar disponibilidad"), ubicacion = "Farmacia"),
        Recordatorio(4, "Ordenar documentos", trabajo, Prioridad.BAJA, Estado.Pendiente)
    )
    val categorias: Set<Categoria> = recordatorios.map { it.categoria }.toSet()
    val coloresPorCategoria: Map<Categoria, Color> = mapOf(
        personal to Color(0xFF7B1FA2), trabajo to Color(0xFF1565C0), estudio to Color(0xFF2E7D32)
    )
    fun porPrioridad(prioridad: Prioridad): List<Recordatorio> = recordatorios.filter { it.prioridad == prioridad }
    fun pendientes(): List<Recordatorio> = recordatorios.filter { it.estado is Estado.Pendiente }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { PantallaRecordatorios() } }
    }
}

@Composable
fun PantallaRecordatorios() {
    Scaffold(topBar = { Surface(color = MaterialTheme.colorScheme.primary) {
        Text("Mis recordatorios", modifier = Modifier.fillMaxWidth().padding(20.dp), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleLarge)
    } }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Agenda de ejemplo · ${DatosSimulados.recordatorios.size} tareas", style = MaterialTheme.typography.titleMedium) }
            items(DatosSimulados.recordatorios, key = { it.id }) { recordatorio -> TarjetaRecordatorio(recordatorio) }
        }
    }
}

@Composable
fun TarjetaRecordatorio(item: Recordatorio) {
    val estadoTexto = when (val e = item.estado) {
        Estado.Pendiente -> "Pendiente"
        Estado.Completado -> "Completado"
        is Estado.Pospuesto -> "Pospuesto: ${e.motivo}"
    }
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(item.titulo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Categoría: ${item.categoria.nombre}", color = DatosSimulados.coloresPorCategoria[item.categoria] ?: Color.DarkGray)
            Text("Prioridad: ${item.prioridad.name.lowercase().replaceFirstChar { it.uppercase() }}")
            Text("Estado: $estadoTexto")
            item.ubicacion?.let { Text("Ubicación: $it") }
            item.aviso?.let { Text("Aviso: $it") }
        }
    }
}
