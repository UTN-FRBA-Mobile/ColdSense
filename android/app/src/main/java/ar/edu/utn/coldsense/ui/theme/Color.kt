package ar.edu.utn.coldsense.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colores propios de la app. Hay una versión para el tema claro y otra
 * para el oscuro; las pantallas los leen con [ColdSenseTheme.colors] y
 * así cambian solos al cambiar el tema.
 */
@Immutable
data class ColdSenseColors(
    // Fondos
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val border: Color,
    val grid: Color,

    // Textos
    val textPrimary: Color,
    val textSecondary: Color,

    // Marca y acciones
    val primary: Color,
    val onPrimary: Color,
    val actionButton: Color,
    val glassReflection: Color,

    // Estados
    val success: Color,
    val successBackground: Color,
    val danger: Color,
    val dangerBackground: Color,
    val alert: Color,
    val alertBackground: Color,
    val disconnectedBackground: Color,
    val disconnectedSignal: Color,

    // Series del gráfico
    val chartMax: Color,
    val chartMin: Color,

    // Diálogos de error y confirmación
    val errorDialogBackground: Color,
    val errorDialogAccent: Color,
    val errorDialogTitle: Color,
    val errorDialogText: Color,
    val errorDialogSecondaryButton: Color
)

val LightColors = ColdSenseColors(
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceMuted = Color(0xFFF5F6F8),
    border = Color(0xFFE5E8EF),
    grid = Color(0xFFEDEFF3),

    textPrimary = Color(0xFF1A1F36),
    textSecondary = Color(0xFF6B7280),

    primary = Color(0xFF1E6FE0),
    onPrimary = Color(0xFFFFFFFF),
    actionButton = Color(0xFF69B3D5),
    glassReflection = Color(0xCCFFFFFF),

    success = Color(0xFF2E9E5B),
    successBackground = Color(0xFFE3F5EA),
    danger = Color(0xFFEF4444),
    dangerBackground = Color(0xFFFFF1F1),
    alert = Color(0xFFD93025),
    alertBackground = Color(0xFFFFD6D6),
    disconnectedBackground = Color(0xFFFFE0E0),
    disconnectedSignal = Color(0xFF8A8F98),

    chartMax = Color(0xFFF59E0B),
    chartMin = Color(0xFF0EA5A4),

    errorDialogBackground = Color(0xFFFFDADA),
    errorDialogAccent = Color(0xFFE57373),
    errorDialogTitle = Color(0xFF8B1A1A),
    errorDialogText = Color(0xFF6D3030),
    errorDialogSecondaryButton = Color(0xFFFFFFFF)
)

val DarkColors = ColdSenseColors(
    background = Color(0xFF0F1218),
    surface = Color(0xFF171B22),
    surfaceMuted = Color(0xFF1C2129),
    border = Color(0xFF2C323D),
    grid = Color(0xFF262C36),

    textPrimary = Color(0xFFE6E8EF),
    textSecondary = Color(0xFF9CA3AF),

    primary = Color(0xFF5B9BFF),
    onPrimary = Color(0xFF0F1218),
    actionButton = Color(0xFF2F7FA8),
    glassReflection = Color(0x26FFFFFF),

    success = Color(0xFF4ADE80),
    successBackground = Color(0xFF16301F),
    danger = Color(0xFFF87171),
    dangerBackground = Color(0xFF3A1F22),
    alert = Color(0xFFFF6B6B),
    alertBackground = Color(0xFF4A2326),
    disconnectedBackground = Color(0xFF3D2427),
    disconnectedSignal = Color(0xFF8A8F98),

    chartMax = Color(0xFFFBBF24),
    chartMin = Color(0xFF2DD4BF),

    errorDialogBackground = Color(0xFF3B2124),
    errorDialogAccent = Color(0xFFE57373),
    errorDialogTitle = Color(0xFFFFB4AB),
    errorDialogText = Color(0xFFE8C4C4),
    errorDialogSecondaryButton = Color(0xFF4A2A2E)
)
