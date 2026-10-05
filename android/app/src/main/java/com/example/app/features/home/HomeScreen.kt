package com.example.app.features.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.app.features.auth.LoginScreen
import com.example.app.features.device.add.AddDeviceScreen
import com.example.app.features.device.detail.DetailScreen
import com.example.app.model.Heladera
import com.example.app.model.HeladeraStatus
import com.example.app.model.calcularEstado
import com.example.app.ui.components.BottomNavDestination
import com.example.app.ui.components.CommonHeader
import com.example.app.ui.components.ConfirmDeleteDialog
import com.example.app.ui.components.ErrorMessage
import com.example.app.ui.components.NavigationBar
import com.example.app.ui.components.NextButton
import com.example.app.ui.components.Spinner
import com.example.app.utils.formatTemperature

/*
 * ================================================================
 * HOME SCREEN
 * ================================================================
 */
class HomeScreen : Screen {

    val viewModel = HomeViewModel()

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current

        HomeContent(
            viewModel = viewModel,
            onAddDevice = { navigator?.push(AddDeviceScreen()) },
            onHeladeraClick = { id -> navigator?.push(DetailScreen(id)) },
            // Vuelvo al login y borro el historial, así "atrás" no regresa al Home
            onLogout = { navigator?.replaceAll(LoginScreen()) },
            onNavigate = { destination ->
                when (destination) {
                    BottomNavDestination.Home -> Unit
                    BottomNavDestination.Stats -> {
                        // TODO: navigator?.replace(StatsScreen())
                    }
                    BottomNavDestination.Notifications -> {
                        // TODO: navigator?.replace(NotificationsScreen())
                    }
                }
            }
        )
    }
}

/*
 * ================================================================
 * HOME CONTENT
 * ================================================================
 */
@Composable
fun HomeContent(
    viewModel: HomeViewModel,
    onAddDevice: () -> Unit,
    onHeladeraClick: (Long) -> Unit,
    onLogout: () -> Unit,
    onNavigate: (BottomNavDestination) -> Unit
) {

    // Estados
    val isLoading by viewModel.isLoading.collectAsState(false)
    val isError   by viewModel.isError.collectAsState(false)
    val heladeras by viewModel.heladeras.collectAsState()

    // Diálogo de cerrar sesión
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Carga de datos
    LaunchedEffect(Unit) {
        viewModel.loadHeladeras()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            // Encabezado
            CommonHeader(
                title = "Mis heladeras",
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )

            // Botón para agregar un nuevo dispositivo
            Box(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                NextButton(onClick = { onAddDevice() }, text = "Agregar dispositivo")
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Spinner()
                }
            } else {
                // Lista de heladeras
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = heladeras, key = { it.id }) { heladera ->
                        HeladeraCard(
                            heladera = heladera,
                            onClick = { onHeladeraClick(heladera.id) }
                        )
                    }
                }
            }

            NavigationBar(
                selectedDestination = BottomNavDestination.Home,
                onDestinationSelected = onNavigate
            )
        }

        // Confirmación para cerrar sesión
        if (showLogoutDialog) {
            ConfirmDeleteDialog(
                title = "¿Cerrar sesión?",
                message = "Vas a volver a la pantalla de inicio de sesión.",
                confirmText = "Cerrar sesión",
                onConfirm = {
                    showLogoutDialog = false
                    onLogout()
                },
                onDismiss = { showLogoutDialog = false }
            )
        }

        // Mensaje si hubo error
        if (isError) {
            ErrorMessage(
                errorText = "No se pudieron cargar las heladeras. Revisá tu conexión e intentá de nuevo.",
                onClick = { viewModel.loadHeladeras() }
            )
        }
    }
}

@Composable
private fun HeladeraCard(
    heladera: Heladera,
    onClick: () -> Unit
) {

    // Calculo el estado de la heladera con la temperatura actual y los límites
    val heladeraStatus = heladera.calcularEstado()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(1.dp, heladeraStatus.cardBorder),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E6FE0).copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Kitchen,
                contentDescription = null,
                tint = Color(0xFF1E6FE0),
                modifier = Modifier.fillMaxSize(0.6f)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = heladera.nombre,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1F36),
                maxLines = 1
            )

            StatusChip(status = heladeraStatus)

            Text(
                text = formatTemperature(heladera.temperaturaActual),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1F36)
            )
        }

        Icon(
            imageVector = heladeraStatus.signalIcon,
            contentDescription = heladeraStatus.label,
            tint = heladeraStatus.signalColor,
            modifier = Modifier
                .align(Alignment.Top)
                .size(26.dp)
        )
    }
}

/**
 * Etiqueta pequeña con un punto de color y el texto del estado.
 *
 * @param status Estado que se quiere mostrar.
 */
@Composable
private fun StatusChip(
    status: HeladeraStatus
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(status.chipBackground)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Punto de color
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(status.color)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Texto del estado
        Text(
            text = status.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = status.color
        )
    }
}