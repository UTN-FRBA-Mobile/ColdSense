package com.example.app.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class HeladeraStatus(
    val label: String,
    val color: Color,
    val chipBackground: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val signalIcon: ImageVector,
    val signalColor: Color
) {
    Normal(
        label = "Normal",
        color = Color(0xFF2E9E5B),
        chipBackground = Color(0xFFE3F5EA),
        cardBackground = Color.White,
        cardBorder = Color(0xFFE5E8EF),
        signalIcon = Icons.Default.Wifi,
        signalColor = Color(0xFF2E9E5B)
    ),
    Alert(
        label = "En alerta",
        color = Color(0xFFD93025),
        chipBackground = Color(0xFFFFD6D6),
        cardBackground = Color.White,
        cardBorder = Color(0xFFE5E8EF),
        signalIcon = Icons.Default.Wifi,
        signalColor = Color(0xFFD93025)
    ),
    Disconnected(
        label = "Sin conexión",
        color = Color(0xFFD93025),
        chipBackground = Color(0xFFFFE0E0),
        cardBackground = Color.White,
        cardBorder = Color(0xFFE5E8EF),
        signalIcon = Icons.Default.WifiOff,
        signalColor = Color(0xFF8A8F98)
    )
}