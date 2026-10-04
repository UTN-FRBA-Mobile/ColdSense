package com.example.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Componente reutilizable que muestra un mensaje de error
 * centrado sobre la pantalla, junto con un botón para
 * ejecutar una acción.
 *
 * @param errorText Texto que describe el error.
 * @param buttonText Texto que se mostrará en el botón.
 * @param onClick Acción que se ejecutará al presionar el botón.
 */
@Composable
fun ErrorMessage(
    errorText: String = "Ocurrió un error inesperado. Por favor, inténtelo de nuevo.",
    buttonText: String = "Reintentar",
    onClick: () -> Unit
) {
    val errorColor = Color(0xFFE57373)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = 0.45f)
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp)
                )
                .background(
                    color = Color(0xFFFFDADA),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(
                    horizontal = 24.dp,
                    vertical = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Ícono de error
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = errorColor,
                modifier = Modifier.size(56.dp)
            )

            // Título
            Text(
                text = "Algo salió mal",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B1A1A),
                textAlign = TextAlign.Center
            )

            // Mensaje del error
            Text(
                text = errorText,
                fontSize = 15.sp,
                color = Color(0xFF6D3030),
                textAlign = TextAlign.Center
            )

            // Botón
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = errorColor,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}