package com.example.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Componente reutilizable que muestra el encabezado común de las
 * pantallas, con el título principal.
 *
 * Respeta automáticamente las zonas del sistema (barra de estado,
 * cámara frontal y esquinas redondeadas), por lo que el contenido
 * nunca queda tapado.
 *
 * @param title Texto del título.
 * @param modifier Modificador opcional para personalizar el contenedor.
 * @param backgroundColor Color de fondo del encabezado (incluye la zona de la barra de estado).
 * @param actions Botones opcionales que se muestran a la derecha del título.
 */
@Composable
fun CommonHeader(
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            // El fondo va antes del padding de las zonas del sistema
            // para que también pinte detrás de la barra de estado
            .background(backgroundColor)
            // Evita la barra de estado, la cámara y los bordes redondeados
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Título
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Acciones opcionales (ej: cerrar sesión)
        actions()
    }
}