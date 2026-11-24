package ar.edu.utn.coldsense.features.device

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.utn.coldsense.model.DispositivoEncontrado
import ar.edu.utn.coldsense.utils.formatTemperature
import ar.edu.utn.coldsense.ui.theme.ColdSenseTheme

/**
 * Tarjeta de un sensor encontrado en la búsqueda.
 * La usan "Agregar dispositivo" (resultados) y "Configurar dispositivo".
 *
 * @param dispositivo Sensor que se muestra.
 * @param onClick Si viene, la tarjeta se puede tocar y muestra una flecha.
 */
@Composable
fun DispositivoEncontradoCard(
    dispositivo: DispositivoEncontrado,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, ColdSenseTheme.colors.border), RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ColdSenseTheme.colors.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DeviceThermostat,
                contentDescription = null,
                tint = ColdSenseTheme.colors.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = dispositivo.modelo,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColdSenseTheme.colors.textPrimary,
                maxLines = 1
            )
            Text(
                text = "N° de serie: ${dispositivo.numeroSerie}",
                fontSize = 13.sp,
                color = ColdSenseTheme.colors.textSecondary
            )
            Text(
                text = "Lectura actual: ${formatTemperature(dispositivo.temperaturaActual)}",
                fontSize = 13.sp,
                color = ColdSenseTheme.colors.textSecondary
            )
        }

        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ColdSenseTheme.colors.textSecondary
            )
        }
    }
}
