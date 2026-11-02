package ar.edu.utn.coldsense.features.device.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.utn.coldsense.model.Heladera
import ar.edu.utn.coldsense.model.Lectura
import ar.edu.utn.coldsense.network.ApiClient
import ar.edu.utn.coldsense.network.HeladeraService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val heladeraService: HeladeraService = ApiClient.heladeraService
) : ViewModel() {

    // Heladera (null hasta que termina la primera carga)
    private val _heladera = MutableStateFlow<Heladera?>(null)
    val heladera = _heladera.asStateFlow()

    // Últimas lecturas (más nueva primero)
    private val _lecturas = MutableStateFlow<List<Lectura>>(emptyList())
    val lecturas = _lecturas.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Mensaje de error (null = sin error)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // Gráfico de temperatura del período elegido
    private val _grafico = MutableStateFlow(GraficoUiState())
    val grafico = _grafico.asStateFlow()
    private var graficoJob: Job? = null

    // Diálogo de eliminar
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog = _showDeleteDialog.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting = _isDeleting.asStateFlow()

    // Pasa a true cuando se eliminó, para que la pantalla vuelva atrás
    private val _isDeleted = MutableStateFlow(false)
    val isDeleted = _isDeleted.asStateFlow()

    fun loadDetalle(heladeraId: Long) {

        // Evito múltiples solicitudes simultáneas
        if (_isLoading.value)
            return
        _isLoading.value = true

        // Solo limpio el error si es el de la carga inicial; uno de otra acción
        // (ej: no se pudo eliminar) queda hasta que lo cierren
        val primeraCarga = _heladera.value == null
        if (primeraCarga)
            _errorMessage.value = null

        viewModelScope.launch {
            try {
                // Pido la heladera y sus lecturas en paralelo.
                // coroutineScope hace que si una falla, el error llegue al catch.
                coroutineScope {
                    val heladeraDeferred = async { heladeraService.getHeladera(heladeraId) }
                    val lecturasDeferred = async { heladeraService.getLecturas(heladeraId, CANTIDAD_LECTURAS) }

                    _heladera.value = heladeraDeferred.await()
                    _lecturas.value = lecturasDeferred.await()
                }

            } catch (e: Exception) {
                // Si falla un refresco sigo mostrando lo último que se cargó
                if (primeraCarga)
                    _errorMessage.value = "No se pudo cargar el detalle de la heladera. Revisá tu conexión e intentá de nuevo."
                Log.e(TAG, "Error al cargar el detalle: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carga las lecturas del período para el gráfico. Si se cambia de período
     * mientras carga, cancelo el pedido anterior.
     *
     * @param mostrarCarga false para los refrescos automáticos: el gráfico sigue mostrando
     * lo anterior mientras carga y, si falla, no muestra el error.
     */
    fun loadGrafico(
        heladeraId: Long,
        periodo: PeriodoGrafico = _grafico.value.periodo,
        mostrarCarga: Boolean = true
    ) {
        // Un refresco automático no pisa una carga que el usuario está esperando (ej: cambió de período)
        if (!mostrarCarga && _grafico.value.isLoading)
            return
        graficoJob?.cancel()

        val hasta = System.currentTimeMillis()
        val desde = hasta - periodo.duracionMillis
        if (mostrarCarga) {
            _grafico.value = _grafico.value.copy(
                periodo = periodo,
                desde = desde,
                hasta = hasta,
                isLoading = true,
                hasError = false
            )
        }

        graficoJob = viewModelScope.launch {
            try {
                val lecturas = heladeraService.getLecturasDesde(heladeraId, desde)
                _grafico.value = _grafico.value.copy(
                    periodo = periodo,
                    desde = desde,
                    hasta = hasta,
                    lecturas = lecturas.sortedBy { it.fecha },
                    isLoading = false,
                    hasError = false
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (mostrarCarga) {
                    _grafico.value = _grafico.value.copy(
                        lecturas = emptyList(),
                        isLoading = false,
                        hasError = true
                    )
                }
                Log.e(TAG, "Error al cargar el gráfico: ${e.message}", e)
            }
        }
    }

    /** Activa o desactiva una línea del gráfico. Siempre queda al menos una activa. */
    fun onSerieClick(serie: SerieGrafico) {
        val actuales = _grafico.value.series
        val nuevas = if (serie in actuales) actuales - serie else actuales + serie
        if (nuevas.isNotEmpty()) {
            _grafico.value = _grafico.value.copy(series = nuevas)
        }
    }

    fun onDeleteClick() {
        _showDeleteDialog.value = true
    }

    fun onDismissDelete() {
        if (!_isDeleting.value) {
            _showDeleteDialog.value = false
        }
    }

    fun confirmDelete(heladeraId: Long) {
        if (_isDeleting.value)
            return
        _isDeleting.value = true

        viewModelScope.launch {
            try {
                heladeraService.eliminarHeladera(heladeraId)
                _isDeleted.value = true

            } catch (e: Exception) {
                _errorMessage.value = "No se pudo eliminar el dispositivo. Intentá de nuevo."
                Log.e(TAG, "Error al eliminar: ${e.message}", e)
            } finally {
                _isDeleting.value = false
                _showDeleteDialog.value = false
            }
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    companion object {
        private const val TAG = "DetailViewModel"
        private const val CANTIDAD_LECTURAS = 5
    }
}