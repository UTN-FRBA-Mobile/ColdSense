package com.example.app.features.device.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.app.ui.components.ErrorMessage
import com.example.app.ui.components.FormField
import com.example.app.ui.components.NextButton
import com.example.app.ui.components.Spinner

private val TextPrimary = Color(0xFF1A1F36)
private val TextSecondary = Color(0xFF6B7280)
private val DangerRed = Color(0xFFEF4444)

/*
 * ================================================================
 * EDIT DEVICE SCREEN
 * ================================================================
 */
class EditDeviceScreen(
    private val heladeraId: Long
) : Screen {

    private val viewModel = EditDeviceViewModel()

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current

        EditDeviceContent(
            heladeraId = heladeraId,
            viewModel = viewModel,
            onBack = { navigator?.pop() },
            // Al volver, el detalle recarga y muestra el rango nuevo
            onSaved = { navigator?.pop() }
        )
    }
}

/*
 * ================================================================
 * EDIT DEVICE CONTENT
 * ================================================================
 */
@Composable
fun EditDeviceContent(
    heladeraId: Long,
    viewModel: EditDeviceViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {

    // Estados
    val heladera by viewModel.heladera.collectAsState()
    val temperaturaMinima by viewModel.temperaturaMinima.collectAsState()
    val temperaturaMaxima by viewModel.temperaturaMaxima.collectAsState()
    val intervalo by viewModel.intervalo.collectAsState()
    val validationError by viewModel.validationError.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    // Carga de los valores actuales
    LaunchedEffect(heladeraId) {
        viewModel.loadHeladera(heladeraId)
    }

    // Cuando se guarda, vuelvo al detalle
    LaunchedEffect(isSaved) {
        if (isSaved) onSaved()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {

            EditDeviceHeader(onBack = onBack)

            val heladeraActual = heladera
            if (heladeraActual != null) {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text = heladeraActual.nombre,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "Si la temperatura sale de este rango, la heladera pasa a estar en alerta.",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormField(
                            label = "Temp. mínima (°C)",
                            value = temperaturaMinima,
                            onValueChange = viewModel::onTemperaturaMinimaChange,
                            placeholder = "Ej: 2",
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                        FormField(
                            label = "Temp. máxima (°C)",
                            value = temperaturaMaxima,
                            onValueChange = viewModel::onTemperaturaMaximaChange,
                            placeholder = "Ej: 6",
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    FormField(
                        label = "Intervalo de lectura (minutos)",
                        value = intervalo,
                        onValueChange = viewModel::onIntervaloChange,
                        placeholder = "Ej: 10",
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )

                    // Error de validación del formulario
                    validationError?.let { mensaje ->
                        Text(
                            text = mensaje,
                            fontSize = 13.sp,
                            color = DangerRed
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    NextButton(
                        onClick = { viewModel.guardar(heladeraId) },
                        enabled = !isSaving,
                        text = "Guardar cambios",
                        isLoading = isSaving
                    )
                }

            } else if (errorMessage == null) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Spinner()
                }
            }
        }

        // Overlay de error
        errorMessage?.let { mensaje ->
            ErrorMessage(
                errorText = mensaje,
                // Si nunca cargó, reintenta. Si ya hay datos (ej: falló el guardado), solo cierra.
                buttonText = if (heladera == null) "Reintentar" else "Aceptar",
                onClick = {
                    if (heladera == null) viewModel.loadHeladera(heladeraId)
                    else viewModel.dismissError()
                }
            )
        }
    }
}

/*
 * ================================================================
 * HEADER
 * ================================================================
 */
@Composable
private fun EditDeviceHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = TextPrimary
            )
        }

        Text(
            text = "Editar parámetros",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        // Espacio del mismo ancho que el botón de volver, para centrar el título
        Spacer(modifier = Modifier.size(48.dp))
    }
}
