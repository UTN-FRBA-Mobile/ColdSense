package ar.edu.utn.coldsense.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ar.edu.utn.coldsense.ui.theme.ColdSenseTheme

/**
 * Componente reutilizable que muestra un indicador de carga centrado.
 *
 * @param color Color del indicador de carga. Por defecto es el color principal del tema.
 * @param paddingVertical Espaciado vertical alrededor del indicador de carga. Por defecto es 16.dp.
 */
@Composable
fun Spinner(
    color: Color = ColdSenseTheme.colors.primary,
    paddingVertical: Dp = 16.dp
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = paddingVertical),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = color)
    }
}