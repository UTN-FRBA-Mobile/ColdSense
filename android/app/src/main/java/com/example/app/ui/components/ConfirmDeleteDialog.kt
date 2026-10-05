package com.example.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.window.Dialog

/**
 * Diálogo de confirmación para eliminar, con el mismo estilo que ErrorMessage.
 * Dialog ya oscurece el fondo y bloquea los toques de atrás.
 *
 * @param title Título del diálogo.
 * @param message Texto que explica la acción.
 * @param confirmText Texto del botón de confirmar. Por defecto es "Eliminar".
 * @param isLoading Mientras es true, muestra un spinner y no se puede cerrar.
 * @param onConfirm Acción al tocar el botón de confirmar.
 * @param onDismiss Acción al tocar "Cancelar" o fuera del diálogo.
 */
@Composable
fun ConfirmDeleteDialog(
    title: String = "¿Eliminar dispositivo?",
    message: String = "Esta acción no se puede deshacer. Se eliminará la heladera y todo su historial de lecturas asociado.",
    confirmText: String = "Eliminar",
    isLoading: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // Misma paleta que ErrorMessage
    val errorColor = Color(0xFFE57373)
    val cardBackground = Color(0xFFFFDADA)
    val titleColor = Color(0xFF8B1A1A)
    val messageColor = Color(0xFF6D3030)

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp)
                )
                .background(
                    color = cardBackground,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(
                    horizontal = 24.dp,
                    vertical = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Ícono "!"
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Advertencia",
                tint = errorColor,
                modifier = Modifier.size(56.dp)
            )

            // Título
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                textAlign = TextAlign.Center
            )

            // Mensaje
            Text(
                text = message,
                fontSize = 15.sp,
                color = messageColor,
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                // Botón eliminar
                Button(
                    onClick = onConfirm,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = errorColor,
                        contentColor = Color.White,
                        disabledContainerColor = errorColor.copy(alpha = 0.7f),
                        disabledContentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(text = confirmText, fontWeight = FontWeight.Bold)
                    }
                }

                // Botón cancelar
                Button(
                    onClick = onDismiss,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = titleColor,
                        disabledContainerColor = Color.White.copy(alpha = 0.6f),
                        disabledContentColor = titleColor.copy(alpha = 0.5f)
                    )
                ) {
                    Text(text = "Cancelar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}