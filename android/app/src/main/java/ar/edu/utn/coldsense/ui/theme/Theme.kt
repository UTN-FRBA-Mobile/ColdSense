package ar.edu.utn.coldsense.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalColdSenseColors = staticCompositionLocalOf { LightColors }

/**
 * Acceso a los colores propios de la app desde cualquier pantalla:
 * `ColdSenseTheme.colors.textPrimary`.
 */
object ColdSenseTheme {
    val colors: ColdSenseColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColdSenseColors.current
}

/**
 * Esquema de Material armado con nuestra paleta, así los componentes de
 * Material (campos de texto, botones, diálogos) usan los mismos colores.
 */
private fun ColdSenseColors.toColorScheme(darkTheme: Boolean): ColorScheme {
    val base = if (darkTheme) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        background = background,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceMuted,
        onSurfaceVariant = textSecondary,
        outline = textSecondary,
        outlineVariant = border,
        error = danger
    )
}

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Sin colores dinámicos (Material You): la app mantiene su paleta en todos los celulares
    val colors = if (darkTheme) DarkColors else LightColors

    CompositionLocalProvider(LocalColdSenseColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(darkTheme),
            typography = Typography,
            content = content
        )
    }
}
