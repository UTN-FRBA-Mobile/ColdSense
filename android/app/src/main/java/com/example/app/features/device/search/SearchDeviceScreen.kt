package com.example.app.features.device.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SearchOff
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.app.features.device.DispositivoEncontradoCard
import com.example.app.features.device.add.AddDeviceScreen
import com.example.app.model.DispositivoEncontrado
import com.example.app.ui.components.ErrorMessage
import com.example.app.ui.components.NextButton
import com.example.app.ui.components.Spinner

private val TextPrimary = Color(0xFF1A1F36)
private val TextSecondary = Color(0xFF6B7280)

/*
 * ================================================================
 * SEARCH DEVICE SCREEN
 * ================================================================
 */
class SearchDeviceScreen : Screen {

    private val viewModel = SearchDeviceViewModel()

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current

        SearchDeviceContent(
            viewModel = viewModel,
            onBack = { navigator?.pop() },
            onDispositivoClick = { dispositivo -> navigator?.push(AddDeviceScreen(dispositivo)) }
        )
    }
}

/*
 * ================================================================
 * SEARCH DEVICE CONTENT
 * ================================================================
 */
@Composable
fun SearchDeviceContent(
    viewModel: SearchDeviceViewModel,
    onBack: () -> Unit,
    onDispositivoClick: (DispositivoEncontrado) -> Unit
) {

    // Estados
    val dispositivos by viewModel.dispositivos.collectAsState()
    val isError by viewModel.isError.collectAsState()

    // Arranca la búsqueda al abrir la pantalla
    LaunchedEffect(Unit) {
        viewModel.iniciarBusqueda()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            SearchDeviceHeader(onBack = onBack)

            val encontrados = dispositivos
            when {
                encontrados == null -> {
                    if (!isError) Buscando(modifier = Modifier.weight(1f))
                }
                encontrados.isEmpty() -> SinResultados(
                    onBuscarDeNuevo = viewModel::buscar,
                    modifier = Modifier.weight(1f)
                )
                else -> Resultados(
                    dispositivos = encontrados,
                    onDispositivoClick = onDispositivoClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Mensaje si hubo error
        if (isError) {
            ErrorMessage(
                errorText = "No se pudo buscar dispositivos. Revisá tu conexión e intentá de nuevo.",
                onClick = viewModel::buscar
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
private fun SearchDeviceHeader(onBack: () -> Unit) {
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
            text = "Agregar dispositivo",
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

/*
 * ================================================================
 * ESTADOS
 * ================================================================
 */
@Composable
private fun Buscando(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spinner()

        Text(
            text = "Buscando dispositivos…",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Asegurate de que el sensor esté encendido y cerca de tu celular.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Resultados(
    dispositivos: List<DispositivoEncontrado>,
    onDispositivoClick: (DispositivoEncontrado) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (dispositivos.size == 1) {
                    "Encontramos 1 dispositivo nuevo"
                } else {
                    "Encontramos ${dispositivos.size} dispositivos nuevos"
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "Tocá el dispositivo para ponerle nombre y configurarlo.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        dispositivos.forEach { dispositivo ->
            DispositivoEncontradoCard(
                dispositivo = dispositivo,
                onClick = { onDispositivoClick(dispositivo) }
            )
        }
    }
}

@Composable
private fun SinResultados(
    onBuscarDeNuevo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No encontramos dispositivos nuevos",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Revisá que el sensor esté encendido y volvé a buscar.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        NextButton(onClick = onBuscarDeNuevo, text = "Buscar de nuevo")
    }
}
