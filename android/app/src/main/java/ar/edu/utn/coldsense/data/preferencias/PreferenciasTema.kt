package ar.edu.utn.coldsense.data.preferencias

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Cómo elige la app entre el tema claro y el oscuro. */
enum class ModoTema {
    Sistema,
    Claro,
    Oscuro;

    /** Si hay que usar el tema oscuro, sabiendo cómo está configurado el celular. */
    fun esOscuro(sistemaOscuro: Boolean): Boolean = when (this) {
        Sistema -> sistemaOscuro
        Claro -> false
        Oscuro -> true
    }

    companion object {
        /** Lo guardado en el celular; si no hay nada (o es inválido), sigue al sistema. */
        fun desdeGuardado(valor: String?): ModoTema =
            entries.firstOrNull { it.name == valor } ?: Sistema
    }
}

/**
 * Guarda el tema elegido en el celular. La app lo escucha con [modo]
 * y cambia de tema en el momento, sin reiniciar.
 */
object PreferenciasTema {

    private const val ARCHIVO = "preferencias"
    private const val CLAVE_MODO = "modo_tema"

    private var prefs: SharedPreferences? = null

    private val _modo = MutableStateFlow(ModoTema.Sistema)
    val modo: StateFlow<ModoTema> = _modo.asStateFlow()

    /** Se llama al abrir la app, antes de dibujar la primera pantalla. */
    fun init(context: Context) {
        val preferencias = context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
        prefs = preferencias
        _modo.value = ModoTema.desdeGuardado(preferencias.getString(CLAVE_MODO, null))
    }

    fun cambiar(modo: ModoTema) {
        prefs?.edit { putString(CLAVE_MODO, modo.name) }
        _modo.value = modo
    }
}
