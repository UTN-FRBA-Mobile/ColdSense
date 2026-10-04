package com.example.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Destinos disponibles en la barra de navegación inferior.
 *
 * @param label Texto que se muestra debajo del ícono.
 * @param icon Ícono que representa al destino.
 */
enum class BottomNavDestination(
    val label: String,
    val icon: ImageVector
) {
    Home("Inicio", Icons.Default.Home),
    Stats("Estadísticas", Icons.Default.BarChart),
    Notifications("Notificaciones", Icons.Default.Notifications)
}

/**
 * Componente reutilizable que muestra una barra de navegación
 * flotante en la parte inferior de la pantalla, con un estilo
 * "glass" (vidrio translúcido) similar al de iOS.
 *
 * Los colores se adaptan al tema (claro u oscuro) por defecto.
 *
 * @param selectedDestination Destino actualmente seleccionado.
 * @param onDestinationSelected Acción que se ejecutará al presionar un destino.
 * @param modifier Modificador opcional para personalizar el contenedor.
 * @param contentColor Color base de los íconos y textos.
 * @param glassColor Color base del vidrio translúcido.
 */
@Composable
fun NavigationBar(
    selectedDestination: BottomNavDestination,
    onDestinationSelected: (BottomNavDestination) -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    glassColor: Color = MaterialTheme.colorScheme.surface
) {
    // Forma de píldora (bordes 100% redondeados) para la barra completa
    val barShape = RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(barShape)
                // Fondo translúcido con degradado para simular el vidrio
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            glassColor.copy(alpha = 0.80f),
                            glassColor.copy(alpha = 0.60f)
                        )
                    )
                )
                // Borde: reflejo blanco arriba y borde sutil abajo para que se
                // distinga también sobre fondos claros
                .border(
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.8f),
                                contentColor.copy(alpha = 0.15f)
                            )
                        )
                    ),
                    shape = barShape
                )
                // Mismo padding en los 4 lados para que las curvas de los
                // ítems queden concéntricas con las de la barra
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Un ítem por cada destino
            BottomNavDestination.entries.forEach { destination ->
                NavigationItem(
                    modifier = Modifier.weight(1f),
                    destination = destination,
                    selected = destination == selectedDestination,
                    contentColor = contentColor,
                    onClick = { onDestinationSelected(destination) }
                )
            }
        }
    }
}

/**
 * Ítem individual de la barra de navegación. Muestra el ícono
 * y el texto del destino, resaltando el que está seleccionado.
 *
 * @param modifier Modificador del contenedor (recibe el weight desde el Row).
 * @param destination Destino que representa el ítem.
 * @param selected Indica si el ítem está seleccionado.
 * @param contentColor Color base de los íconos y textos.
 * @param onClick Acción que se ejecutará al presionar el ítem.
 */
@Composable
private fun NavigationItem(
    modifier: Modifier = Modifier,
    destination: BottomNavDestination,
    selected: Boolean,
    contentColor: Color,
    onClick: () -> Unit
) {
    // Animación suave de color entre estado seleccionado y no seleccionado
    val tint by animateColorAsState(
        targetValue = if (selected) contentColor else contentColor.copy(alpha = 0.55f),
        label = "navItemTint"
    )

    // Se anima hacia el mismo color con alpha 0 (y no hacia Color.Transparent)
    // para evitar que la transición pase por tonos grises
    val highlight by animateColorAsState(
        targetValue = contentColor.copy(alpha = if (selected) 0.12f else 0f),
        label = "navItemHighlight"
    )

    // Contenedor de ancho fijo (1/3 de la barra): el resaltado ocupa
    // todo el ancho para que los tres ítems se vean iguales
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(highlight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {

        // Contenido centrado dentro del ítem
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {

            // Ícono del destino
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.label,
                tint = tint,
                modifier = Modifier.size(26.dp)
            )

            // Texto del destino
            Text(
                text = destination.label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = tint,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}