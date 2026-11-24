package ar.edu.utn.coldsense.ui.components

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
import ar.edu.utn.coldsense.ui.theme.ColdSenseTheme
import androidx.compose.ui.res.stringResource
import ar.edu.utn.coldsense.R

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
    errorText: String = stringResource(R.string.error_inesperado),
    buttonText: String = stringResource(R.string.comun_reintentar),
    onClick: () -> Unit
) {
    val colors = ColdSenseTheme.colors
    val errorColor = colors.errorDialogAccent

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
                    color = colors.errorDialogBackground,
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
                contentDescription = stringResource(R.string.error_icono),
                tint = errorColor,
                modifier = Modifier.size(56.dp)
            )

            // Título
            Text(
                text = stringResource(R.string.error_titulo),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.errorDialogTitle,
                textAlign = TextAlign.Center
            )

            // Mensaje del error
            Text(
                text = errorText,
                fontSize = 15.sp,
                color = colors.errorDialogText,
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