package ar.edu.utn.coldsense.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import ar.edu.utn.coldsense.data.preferencias.ModoTema
import ar.edu.utn.coldsense.data.preferencias.PreferenciasTema
import ar.edu.utn.coldsense.ui.theme.ColdSenseTheme
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import ar.edu.utn.coldsense.R
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Language
import ar.edu.utn.coldsense.data.preferencias.Idioma
import ar.edu.utn.coldsense.data.preferencias.PreferenciasIdioma

/*
 * ================================================================
 * SETTINGS SCREEN
 * ================================================================
 */
class SettingsScreen : Screen {

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current
        val modoTema by PreferenciasTema.modo.collectAsState()
        val activity = LocalActivity.current
        val idioma = activity?.let { PreferenciasIdioma.actual(it) } ?: Idioma.Sistema

        SettingsContent(
            modoTema = modoTema,
            onModoTemaClick = PreferenciasTema::cambiar,
            idioma = idioma,
            onIdiomaClick = { nuevo -> activity?.let { PreferenciasIdioma.cambiar(it, nuevo) } },
            onBack = { navigator?.pop() }
        )
    }
}

/*
 * ================================================================
 * SETTINGS CONTENT
 * ================================================================
 */
@Composable
fun SettingsContent(
    modoTema: ModoTema,
    onModoTemaClick: (ModoTema) -> Unit,
    idioma: Idioma,
    onIdiomaClick: (Idioma) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        SettingsHeader(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            SeccionTitulo(texto = stringResource(R.string.configuracion_apariencia))

            // Opciones del tema: se aplica apenas se toca, sin botón de guardar
            GrupoOpciones {
                ModoTema.entries.forEachIndexed { index, modo ->
                    if (index > 0) {
                        HorizontalDivider(color = ColdSenseTheme.colors.border)
                    }
                    Opcion(
                        icon = modo.icono(),
                        texto = stringResource(modo.texto()),
                        seleccionada = modo == modoTema,
                        onClick = { onModoTemaClick(modo) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SeccionTitulo(texto = stringResource(R.string.configuracion_idioma))

            // Al elegir otro idioma la pantalla se vuelve a dibujar con los textos nuevos
            GrupoOpciones {
                Idioma.entries.forEachIndexed { index, opcion ->
                    if (index > 0) {
                        HorizontalDivider(color = ColdSenseTheme.colors.border)
                    }
                    Opcion(
                        icon = opcion.icono(),
                        texto = stringResource(opcion.texto()),
                        seleccionada = opcion == idioma,
                        onClick = { onIdiomaClick(opcion) }
                    )
                }
            }
        }
    }
}

/*
 * ================================================================
 * HEADER
 * ================================================================
 */
@Composable
private fun SettingsHeader(onBack: () -> Unit) {
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
                contentDescription = stringResource(R.string.comun_volver),
                tint = ColdSenseTheme.colors.textPrimary
            )
        }

        Text(
            text = stringResource(R.string.comun_configuracion),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = ColdSenseTheme.colors.textPrimary
        )

        // Espacio del mismo ancho que el botón de volver, para centrar el título
        Spacer(modifier = Modifier.size(48.dp))
    }
}

/*
 * ================================================================
 * HELPERS
 * ================================================================
 */
@Composable
private fun SeccionTitulo(texto: String) {
    Text(
        text = texto,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = ColdSenseTheme.colors.textPrimary
    )
}

/** Opciones de una sola elección, dentro de un recuadro. */
@Composable
private fun GrupoOpciones(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, ColdSenseTheme.colors.border, RoundedCornerShape(12.dp))
            .selectableGroup(),
        content = content
    )
}

@Composable
private fun Opcion(
    icon: ImageVector,
    texto: String,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = seleccionada, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ColdSenseTheme.colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = texto,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp,
            color = ColdSenseTheme.colors.textPrimary
        )
        // El click lo maneja la fila entera
        RadioButton(
            selected = seleccionada,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = ColdSenseTheme.colors.primary,
                unselectedColor = ColdSenseTheme.colors.textSecondary
            )
        )
    }
}

@StringRes
private fun ModoTema.texto(): Int = when (this) {
    ModoTema.Sistema -> R.string.tema_sistema
    ModoTema.Claro -> R.string.tema_claro
    ModoTema.Oscuro -> R.string.tema_oscuro
}

private fun ModoTema.icono(): ImageVector = when (this) {
    ModoTema.Sistema -> Icons.Default.PhoneAndroid
    ModoTema.Claro -> Icons.Default.LightMode
    ModoTema.Oscuro -> Icons.Default.DarkMode
}

@StringRes
private fun Idioma.texto(): Int = when (this) {
    Idioma.Sistema -> R.string.idioma_sistema
    Idioma.Espanol -> R.string.idioma_espanol
    Idioma.Ingles -> R.string.idioma_ingles
}

private fun Idioma.icono(): ImageVector = when (this) {
    Idioma.Sistema -> Icons.Default.PhoneAndroid
    Idioma.Espanol, Idioma.Ingles -> Icons.Default.Language
}
