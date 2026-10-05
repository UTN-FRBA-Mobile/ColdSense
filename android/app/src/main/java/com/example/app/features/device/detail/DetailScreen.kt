package com.example.app.features.device.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.app.features.device.edit.EditDeviceScreen
import com.example.app.features.device.lecturas.LecturasScreen
import com.example.app.model.Heladera
import com.example.app.model.HeladeraStatus
import com.example.app.model.Lectura
import com.example.app.model.calcularEstado
import com.example.app.ui.components.ConfirmDeleteDialog
import com.example.app.ui.components.ErrorMessage
import com.example.app.ui.components.Spinner
import com.example.app.utils.formatHora
import com.example.app.utils.formatLimite
import com.example.app.utils.formatTemperature
import com.example.app.utils.formatUltimaLectura

// Colores de la pantalla
private val TextPrimary = Color(0xFF1A1F36)
private val TextSecondary = Color(0xFF6B7280)
private val PrimaryBlue = Color(0xFF1E6FE0)
private val CardBorder = Color(0xFFE5E8EF)
private val SurfaceGray = Color(0xFFF5F6F8)
private val DangerRed = Color(0xFFEF4444)

/*
 * ================================================================
 * DEVICE DETAIL SCREEN
 * ================================================================
 */
class DetailScreen(
    private val heladeraId: Long
) : Screen {

    private val viewModel = DetailViewModel()

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current

        DeviceDetailContent(
            heladeraId = heladeraId,
            viewModel = viewModel,
            onBack = { navigator?.pop() },
            onEdit = { navigator?.push(EditDeviceScreen(heladeraId)) },
            onVerTodas = { navigator?.push(LecturasScreen(heladeraId)) },
            onDeleted = { navigator?.pop() }
        )
    }
}

/*
 * ================================================================
 * DEVICE DETAIL CONTENT
 * ================================================================
 */
@Composable
fun DeviceDetailContent(
    heladeraId: Long,
    viewModel: DetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onVerTodas: () -> Unit,
    onDeleted: () -> Unit
) {

    // Estados
    val heladera by viewModel.heladera.collectAsState()
    val lecturas by viewModel.lecturas.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val isDeleted by viewModel.isDeleted.collectAsState()

    // Carga de datos (se repite al volver a esta pantalla, así se refresca)
    LaunchedEffect(heladeraId) {
        viewModel.loadDetalle(heladeraId)
    }

    // Cuando se elimina, vuelvo al Home
    LaunchedEffect(isDeleted) {
        if (isDeleted) onDeleted()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            DetailHeader(
                title = heladera?.nombre.orEmpty(),
                onBack = onBack,
                onEdit = onEdit,
                editEnabled = heladera != null
            )

            val heladeraActual = heladera
            if (heladeraActual != null) {
                DetailBody(
                    heladera = heladeraActual,
                    lecturas = lecturas,
                    onVerTodas = onVerTodas,
                    onDeleteClick = viewModel::onDeleteClick,
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

        // Diálogo de confirmación
        if (showDeleteDialog) {
            ConfirmDeleteDialog(
                isLoading = isDeleting,
                onConfirm = { viewModel.confirmDelete(heladeraId) },
                onDismiss = viewModel::onDismissDelete
            )
        }

        // Overlay de error
        errorMessage?.let { mensaje ->
            ErrorMessage(
                errorText = mensaje,
                // Si nunca cargó, reintenta. Si ya hay datos (ej: falló el eliminar), solo cierra.
                buttonText = if (heladera == null) "Reintentar" else "Aceptar",
                onClick = {
                    if (heladera == null) viewModel.loadDetalle(heladeraId)
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
private fun DetailHeader(
    title: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    editEnabled: Boolean
) {
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
            text = title,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        IconButton(onClick = onEdit, enabled = editEnabled) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar parámetros",
                tint = if (editEnabled) PrimaryBlue else PrimaryBlue.copy(alpha = 0.3f)
            )
        }
    }
}

/*
 * ================================================================
 * BODY
 * ================================================================
 */
@Composable
private fun DetailBody(
    heladera: Heladera,
    lecturas: List<Lectura>,
    onVerTodas: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = heladera.calcularEstado()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        EstadoActualCard(status = status)

        TemperaturaActual(heladera = heladera)

        // Rango + sensor, misma altura
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RangoCard(
                heladera = heladera,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            SensorCard(
                heladera = heladera,
                status = status,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        LecturasSection(lecturas = lecturas, onVerTodas = onVerTodas)

        // Eliminar dispositivo
        TextButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = DangerRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Eliminar dispositivo",
                color = DangerRed,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EstadoActualCard(status: HeladeraStatus) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(status.chipBackground)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Estado actual",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = status.color
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = estadoIcon(status),
                    contentDescription = null,
                    tint = status.color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = status.label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }

        // TODO: reemplazar por la ilustración de la heladera (painterResource)
        Icon(
            imageVector = Icons.Default.Kitchen,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(56.dp)
        )
    }
}

@Composable
private fun TemperaturaActual(heladera: Heladera) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = formatTemperature(heladera.temperaturaActual),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Última lectura: ${formatUltimaLectura(heladera.ultimaLectura)}",
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun RangoCard(heladera: Heladera, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceGray)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CardTitle(text = "Rango configurado")
        LimiteRow(label = "Mín:", valor = heladera.temperaturaMinima)
        LimiteRow(label = "Máx:", valor = heladera.temperaturaMaxima)
    }
}

@Composable
private fun LimiteRow(label: String, valor: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 14.sp, color = TextPrimary)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = formatLimite(valor),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun SensorCard(heladera: Heladera, status: HeladeraStatus, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CardTitle(text = "Estado del sensor")

        IconTextRow(
            icon = status.signalIcon,
            iconColor = status.signalColor,
            text = if (status == HeladeraStatus.Disconnected) "Sin conexión" else "Conectado"
        )

        IconTextRow(
            icon = Icons.Default.BatteryStd,
            iconColor = TextSecondary,
            text = "Batería: ${heladera.bateria?.let { "$it%" } ?: "--"}"
        )
    }
}

@Composable
private fun LecturasSection(lecturas: List<Lectura>, onVerTodas: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Últimas lecturas",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "Ver todas",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryBlue,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onVerTodas)
                    .padding(4.dp)
            )
        }

        if (lecturas.isEmpty()) {
            Text(
                text = "Todavía no hay lecturas registradas.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceGray)
            ) {
                lecturas.forEachIndexed { index, lectura ->
                    if (index > 0) {
                        HorizontalDivider(color = CardBorder)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatHora(lectura.fecha),
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = formatTemperature(lectura.temperatura),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

/*
 * ================================================================
 * HELPERS
 * ================================================================
 */
@Composable
private fun CardTitle(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = TextSecondary
    )
}

@Composable
private fun IconTextRow(icon: ImageVector, iconColor: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 13.sp, color = TextPrimary)
    }
}

private fun estadoIcon(status: HeladeraStatus): ImageVector = when (status) {
    HeladeraStatus.Normal -> Icons.Default.CheckCircle
    HeladeraStatus.Alert -> Icons.Default.Warning
    HeladeraStatus.Disconnected -> Icons.Default.WifiOff
}