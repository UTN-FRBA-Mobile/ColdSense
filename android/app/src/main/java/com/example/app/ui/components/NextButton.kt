package com.example.app.ui.components

import android.widget.Spinner
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

/**
 * Componente reutilizable para avanzar a la siguiente acción.
 *
 * @param onClick Acción que se ejecutará al presionar el botón.
 * @param enabled Indica si el botón está habilitado. Por defecto es true.
 * @param text Texto que se mostrará en el botón. Por defecto es "Continuar".
 */
@Composable
fun NextButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    text: String = "Continuar",
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF69B3D5))
    ) {
        if (isLoading) {
            Spinner(color = Color.White)
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
