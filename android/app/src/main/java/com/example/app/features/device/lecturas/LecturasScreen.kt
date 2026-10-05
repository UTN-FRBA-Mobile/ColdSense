package com.example.app.features.device.lecturas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.app.model.Heladera
import com.example.app.model.Lectura
import com.example.app.ui.components.ErrorMessage
import com.example.app.ui.components.Spinner
import com.example.app.utils.formatLimite
import com.example.app.utils.formatTemperature
import com.example.app.utils.formatUltimaLectura

private val TextPrimary = Color(0xFF1A1F36)
private val TextSecondary = Color(0xFF6B7280)
private val CardBorder = Color(0xFFE5E8EF)
private val SurfaceGray = Color(0xFFF5F6F8)
private val DangerRed = Color(0xFFEF4444)
private val AlertBackground = Color(0xFFFFF1F1)

/*
 * ================================================================
 * LECTURAS SCREEN
 * ================================================================
 */
class LecturasScreen(
    private val heladeraId: Long
) : Screen {

    private val viewModel = LecturasViewModel()

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current

        LecturasContent(
            heladeraId = heladeraId,
            viewModel = viewModel,
            onBack = { navigator?.pop() }
        )
    }
}

/*
 * ================================================================
 * LECTURAS CONTENT
 * ================================================================
 */
@Composable
fun LecturasContent(
    heladeraId: Long,
    viewModel: LecturasViewModel,
    onBack: () -> Unit
) {

    // Estados
    val heladera by viewModel.heladera.collectAsState()
    val lecturas by viewModel.lecturas.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Carga de datos
    LaunchedEffect(heladeraId) {
        viewModel.loadLecturas(heladeraId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            LecturasHeader(onBack = onBack)

            val heladeraActual = heladera
            if (heladeraActual != null) {
                LecturasBody(
                    heladera = heladeraActual,
                    lecturas = lecturas,
                    modifier = Modifier.weight(1f)
                )
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
                onClick = { viewModel.loadLecturas(heladeraId) }
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
private fun LecturasHeader(onBack: () -> Unit) {
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
            text = "Historial de lecturas",
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
 * BODY
 * ================================================================
 */
@Composable
private fun LecturasBody(
    heladera: Heladera,
    lecturas: List<Lectura>,
    modifier: Modifier = Modifier
) {
    val resumen = resumirLecturas(lecturas, heladera.temperaturaMinima, heladera.temperaturaMaxima)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        item {
            Column(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = heladera.nombre,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Rango configurado: ${formatLimite(heladera.temperaturaMinima)} a ${formatLimite(heladera.temperaturaMaxima)}",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }

        if (resumen == null) {
            item {
                Text(
                    text = "Todavía no hay lecturas registradas.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        } else {
            item {
                ResumenCard(resumen = resumen, cantidad = lecturas.size)
                Spacer(modifier = Modifier.size(20.dp))
            }

            itemsIndexed(items = lecturas) { index, lectura ->
                LecturaRow(
                    lectura = lectura,
                    fueraDeRango = lectura.estaFueraDeRango(heladera.temperaturaMinima, heladera.temperaturaMaxima),
                    esPrimera = index == 0,
                    esUltima = index == lecturas.lastIndex
                )
            }
        }

        item {
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun ResumenCard(resumen: ResumenLecturas, cantidad: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceGray)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Resumen de las últimas $cantidad lecturas",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            ResumenDato(label = "Mínima", valor = formatTemperature(resumen.minima), modifier = Modifier.weight(1f))
            ResumenDato(label = "Promedio", valor = formatTemperature(resumen.promedio), modifier = Modifier.weight(1f))
            ResumenDato(label = "Máxima", valor = formatTemperature(resumen.maxima), modifier = Modifier.weight(1f))
        }

        Text(
            text = if (resumen.fueraDeRango == 0) {
                "Todas las lecturas estuvieron dentro del rango."
            } else {
                "${resumen.fueraDeRango} lecturas fuera de rango."
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (resumen.fueraDeRango == 0) Color(0xFF2E9E5B) else DangerRed
        )
    }
}

@Composable
private fun ResumenDato(label: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(text = valor, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun LecturaRow(
    lectura: Lectura,
    fueraDeRango: Boolean,
    esPrimera: Boolean,
    esUltima: Boolean
) {
    // Redondeo solo las esquinas de arriba de la primera fila y las de abajo de la última
    val forma = RoundedCornerShape(
        topStart = if (esPrimera) 12.dp else 0.dp,
        topEnd = if (esPrimera) 12.dp else 0.dp,
        bottomStart = if (esUltima) 12.dp else 0.dp,
        bottomEnd = if (esUltima) 12.dp else 0.dp
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(if (fueraDeRango) AlertBackground else SurfaceGray)
    ) {
        if (!esPrimera) {
            HorizontalDivider(color = CardBorder)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatUltimaLectura(lectura.fecha),
                fontSize = 14.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )

            if (fueraDeRango) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Fuera de rango",
                    tint = DangerRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = formatTemperature(lectura.temperatura),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (fueraDeRango) DangerRed else TextPrimary
            )
        }
    }
}
