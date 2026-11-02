package ar.edu.utn.coldsense.notifications

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Avisa a las pantallas abiertas que llegó una alerta, para que vuelvan a pedir los datos
 * y no sigan mostrando una temperatura vieja. Si no hay ninguna pantalla escuchando, el aviso se pierde
 * (no importa: al volver a la app las pantallas se refrescan solas).
 */
object CambiosHeladeras {

    // Id de la heladera que cambió (null si la alerta no lo trae)
    private val _cambios = MutableSharedFlow<Long?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val cambios: SharedFlow<Long?> = _cambios.asSharedFlow()

    fun avisar(heladeraId: Long?) {
        _cambios.tryEmit(heladeraId)
    }
}
