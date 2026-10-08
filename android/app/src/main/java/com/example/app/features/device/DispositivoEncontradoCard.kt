package com.example.app.features.device

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.model.DispositivoEncontrado
import com.example.app.utils.formatTemperature

private val TextPrimary = Color(0xFF1A1F36)
private val TextSecondary = Color(0xFF6B7280)
private val PrimaryBlue = Color(0xFF1E6FE0)
private val CardBorder = Color(0xFFE5E8EF)

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
            .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryBlue.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DeviceThermostat,
                contentDescription = null,
                tint = PrimaryBlue,
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
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = "N° de serie: ${dispositivo.numeroSerie}",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Text(
                text = "Lectura actual: ${formatTemperature(dispositivo.temperaturaActual)}",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}
