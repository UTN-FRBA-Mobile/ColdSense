package com.example.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.app.ui.components.ErrorMessage
import com.example.app.ui.components.NextButton

@Composable
fun HomeScreen() {

    var showError by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        if (showError) {

            ErrorMessage(
                errorText = "No se pudo realizar la operación.",
                buttonText = "Reintentar",
                onClick = {
                    showError = false
                }
            )

        } else {

            Spacer(
                modifier = Modifier.weight(1f)
            )

            NextButton(
                onClick = {
                    showError = true
                }
            )
        }
    }
}
