package ar.edu.utn.coldsense.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ar.edu.utn.coldsense.ui.theme.ColdSenseTheme

enum class HeladeraStatus(
    val label: String,
    val signalIcon: ImageVector
) {
    Normal(
        label = "Normal",
        signalIcon = Icons.Default.Wifi
    ),
    Alert(
        label = "En alerta",
        signalIcon = Icons.Default.Wifi
    ),
    Disconnected(
        label = "Sin conexión",
        signalIcon = Icons.Default.WifiOff
    )
}

// Los colores dependen del tema (claro u oscuro), por eso se leen desde la UI

val HeladeraStatus.color: Color
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        HeladeraStatus.Normal -> ColdSenseTheme.colors.success
        HeladeraStatus.Alert, HeladeraStatus.Disconnected -> ColdSenseTheme.colors.alert
    }

val HeladeraStatus.chipBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        HeladeraStatus.Normal -> ColdSenseTheme.colors.successBackground
        HeladeraStatus.Alert -> ColdSenseTheme.colors.alertBackground
        HeladeraStatus.Disconnected -> ColdSenseTheme.colors.disconnectedBackground
    }

val HeladeraStatus.cardBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = ColdSenseTheme.colors.border

val HeladeraStatus.signalColor: Color
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        HeladeraStatus.Normal -> ColdSenseTheme.colors.success
        HeladeraStatus.Alert -> ColdSenseTheme.colors.alert
        HeladeraStatus.Disconnected -> ColdSenseTheme.colors.disconnectedSignal
    }
