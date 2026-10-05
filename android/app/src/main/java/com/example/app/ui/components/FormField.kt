package com.example.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Componente reutilizable para los campos de texto de los formularios.
 *
 * @param label Texto que describe el campo.
 * @param value Valor actual del campo.
 * @param onValueChange Acción que se ejecuta cuando el usuario escribe.
 * @param placeholder Texto de ejemplo que se muestra cuando el campo está vacío.
 * @param keyboardType Tipo de teclado (texto, número, decimal).
 * @param modifier Modificador opcional para personalizar el campo.
 * @param imeAction Acción del botón "enter" del teclado. Por defecto pasa al siguiente campo.
 */
@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        modifier = modifier.fillMaxWidth()
    )
}
