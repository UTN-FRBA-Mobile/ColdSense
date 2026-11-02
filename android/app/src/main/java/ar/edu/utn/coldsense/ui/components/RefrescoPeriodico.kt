package ar.edu.utn.coldsense.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay

/**
 * Llama a [onRefrescar] apenas se muestra la pantalla y después cada [intervaloMs] mientras se ve.
 * Se frena cuando la app pasa a segundo plano o se sale de la pantalla, y arranca de nuevo al volver.
 *
 * @param intervaloMs Cada cuánto se vuelve a pedir los datos. Por defecto 3 segundos.
 * @param onRefrescar Pide los datos de nuevo.
 */
@Composable
fun RefrescoPeriodico(
    intervaloMs: Long = 3_000L,
    onRefrescar: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val refrescar by rememberUpdatedState(onRefrescar)

    LaunchedEffect(lifecycleOwner, intervaloMs) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                refrescar()
                delay(intervaloMs)
            }
        }
    }
}
