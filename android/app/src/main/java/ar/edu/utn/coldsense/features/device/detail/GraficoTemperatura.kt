package ar.edu.utn.coldsense.features.device.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ar.edu.utn.coldsense.model.Lectura
import ar.edu.utn.coldsense.ui.components.Spinner
import ar.edu.utn.coldsense.utils.formatLimite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import ar.edu.utn.coldsense.ui.theme.ColdSenseTheme
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import ar.edu.utn.coldsense.R

/** Líneas que se pueden mostrar en el gráfico. */
enum class SerieGrafico(@StringRes val label: Int, val valor: (PuntoGrafico) -> Double) {
    Promedio(R.string.serie_promedio, { it.promedio }),
    Maxima(R.string.serie_maxima, { it.maxima }),
    Minima(R.string.serie_minima, { it.minima })
}

/** Color de cada línea según el tema (claro u oscuro). */
val SerieGrafico.color: Color
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        SerieGrafico.Promedio -> ColdSenseTheme.colors.primary
        SerieGrafico.Maxima -> ColdSenseTheme.colors.chartMax
        SerieGrafico.Minima -> ColdSenseTheme.colors.chartMin
    }

private const val UNA_HORA = 60 * 60 * 1000L
private const val UN_DIA = 24 * UNA_HORA

/**
 * Períodos que se pueden elegir para el gráfico.
 *
 * @param agrupacionMillis Tamaño del bloque que se promedia en un solo punto
 *                         (null = se muestra cada lectura).
 * @param leyenda          Texto que explica ese bloque debajo del gráfico.
 */
enum class PeriodoGrafico(
    @StringRes val label: Int,
    val duracionMillis: Long,
    val agrupacionMillis: Long?,
    @StringRes val leyenda: Int?
) {
    UnaHora(R.string.periodo_1_hora, UNA_HORA, null, null),
    VeinticuatroHoras(R.string.periodo_24_horas, UN_DIA, UNA_HORA, R.string.grafico_valores_por_hora),
    SieteDias(R.string.periodo_7_dias, 7 * UN_DIA, 6 * UNA_HORA, R.string.grafico_valores_por_6_horas),
    UnMes(R.string.periodo_1_mes, 30 * UN_DIA, UN_DIA, R.string.grafico_valores_por_dia)
}

/**
 * Un punto del gráfico: el promedio de un bloque de lecturas, y la mínima y máxima
 * que hubo en ese bloque (para las líneas de mínima y máxima, así no se esconden los picos).
 */
data class PuntoGrafico(
    val fecha: Long,
    val promedio: Double,
    val minima: Double,
    val maxima: Double
)

/**
 * Agrupa las lecturas por bloques (de 1 hora, 6 horas o 1 día según el período),
 * alineados a la hora local: 1 día va de 00:00 a 24:00, 6 horas de 00 a 06, 06 a 12, etc.
 * Cada bloque devuelve un punto ubicado en la fecha promedio de sus lecturas
 * (así un bloque incompleto, como el de hoy, no se corre).
 * En "1 hora" no se agrupa: cada lectura es un punto (mínima = máxima = promedio).
 */
fun agruparLecturas(lecturas: List<Lectura>, periodo: PeriodoGrafico): List<PuntoGrafico> {
    val bloque = periodo.agrupacionMillis
        ?: return lecturas.map { PuntoGrafico(it.fecha, it.temperatura, it.temperatura, it.temperatura) }
    val zona = TimeZone.getDefault()

    return lecturas
        .groupBy { lectura ->
            val local = lectura.fecha + zona.getOffset(lectura.fecha)
            Math.floorDiv(local, bloque)
        }
        .toSortedMap()
        .values
        .map { grupo ->
            val temperaturas = grupo.map { it.temperatura }
            PuntoGrafico(
                fecha = grupo.map { it.fecha }.average().toLong(),
                promedio = temperaturas.average(),
                minima = temperaturas.min(),
                maxima = temperaturas.max()
            )
        }
}

/** Estado del gráfico. Las lecturas van de la más vieja a la más nueva. */
data class GraficoUiState(
    val periodo: PeriodoGrafico = PeriodoGrafico.VeinticuatroHoras,
    val desde: Long = 0L,
    val hasta: Long = 0L,
    val lecturas: List<Lectura> = emptyList(),
    val series: Set<SerieGrafico> = setOf(SerieGrafico.Promedio),
    val isLoading: Boolean = false,
    val hasError: Boolean = false
)

/*
 * ================================================================
 * SECCIÓN: BOTONES + GRÁFICO
 * ================================================================
 */
@Composable
fun GraficoSection(
    state: GraficoUiState,
    temperaturaMinima: Double,
    temperaturaMaxima: Double,
    intervaloLecturaMinutos: Int,
    onPeriodoClick: (PeriodoGrafico) -> Unit,
    onSerieClick: (SerieGrafico) -> Unit,
    onReintentar: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        PeriodoSelector(seleccionado = state.periodo, onClick = onPeriodoClick)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, ColdSenseTheme.colors.border, RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> Spinner()

                state.hasError -> Text(
                    text = stringResource(R.string.grafico_error),
                    fontSize = 14.sp,
                    color = ColdSenseTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onReintentar)
                        .padding(8.dp)
                )

                else -> {
                    GraficoTemperatura(
                        lecturas = state.lecturas,
                        desde = state.desde,
                        hasta = state.hasta,
                        periodo = state.periodo,
                        series = state.series,
                        temperaturaMinima = temperaturaMinima,
                        temperaturaMaxima = temperaturaMaxima,
                        intervaloLecturaMinutos = intervaloLecturaMinutos,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (state.lecturas.isEmpty()) {
                        Text(
                            text = stringResource(R.string.grafico_sin_lecturas),
                            fontSize = 14.sp,
                            color = ColdSenseTheme.colors.textSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ColdSenseTheme.colors.surface)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Selector de líneas (solo cuando hay bloques; en "1 hora" cada punto es
        // una sola lectura, así que promedio, mínima y máxima serían la misma línea)
        val leyenda = state.periodo.leyenda
        if (leyenda != null) {
            SerieSelector(activas = state.series, onClick = onSerieClick)
            Text(
                text = stringResource(leyenda),
                fontSize = 12.sp,
                color = ColdSenseTheme.colors.textSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

/*
 * ================================================================
 * SELECTOR DE LÍNEAS (PROMEDIO / MÁXIMA / MÍNIMA)
 * ================================================================
 */
@Composable
private fun SerieSelector(
    activas: Set<SerieGrafico>,
    onClick: (SerieGrafico) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        SerieGrafico.entries.forEach { serie ->
            val activa = serie in activas
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (activa) serie.color.copy(alpha = 0.12f) else ColdSenseTheme.colors.surfaceMuted)
                    .border(
                        width = 1.dp,
                        color = if (activa) serie.color else ColdSenseTheme.colors.border,
                        shape = RoundedCornerShape(50)
                    )
                    .toggleable(
                        value = activa,
                        onValueChange = { onClick(serie) },
                        role = Role.Checkbox
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 14.dp, height = 3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (activa) serie.color else ColdSenseTheme.colors.textSecondary.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(serie.label),
                    fontSize = 13.sp,
                    fontWeight = if (activa) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (activa) ColdSenseTheme.colors.textPrimary else ColdSenseTheme.colors.textSecondary
                )
            }
        }
    }
}

/*
 * ================================================================
 * BOTONES DE PERÍODO
 * ================================================================
 */
@Composable
private fun PeriodoSelector(
    seleccionado: PeriodoGrafico,
    onClick: (PeriodoGrafico) -> Unit
) {
    // Misma altura para todos aunque alguno ocupe dos líneas
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PeriodoGrafico.entries.forEach { periodo ->
            val isSelected = periodo == seleccionado
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .heightIn(min = 40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) ColdSenseTheme.colors.primary else ColdSenseTheme.colors.surfaceMuted)
                    .selectable(
                        selected = isSelected,
                        onClick = { onClick(periodo) },
                        role = Role.Tab
                    )
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(periodo.label),
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) ColdSenseTheme.colors.onPrimary else ColdSenseTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

/*
 * ================================================================
 * GRÁFICO DE LÍNEA
 * ================================================================
 */
@Composable
private fun GraficoTemperatura(
    lecturas: List<Lectura>,
    desde: Long,
    hasta: Long,
    periodo: PeriodoGrafico,
    series: Set<SerieGrafico>,
    temperaturaMinima: Double,
    temperaturaMaxima: Double,
    intervaloLecturaMinutos: Int,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val colorGrilla = ColdSenseTheme.colors.grid
    val colorLimite = ColdSenseTheme.colors.danger
    val ejeStyle = TextStyle(fontSize = 10.sp, color = ColdSenseTheme.colors.textSecondary)
    val limiteStyle = TextStyle(fontSize = 10.sp, color = colorLimite, fontWeight = FontWeight.SemiBold)
    // Dentro del Canvas no se pueden leer recursos, así que armo los textos antes
    val textoMaximo = stringResource(R.string.grafico_limite_maximo, formatLimite(temperaturaMaxima))
    val textoMinimo = stringResource(R.string.grafico_limite_minimo, formatLimite(temperaturaMinima))

    // Puntos a dibujar: cada lectura en "1 hora", bloques en el resto
    val puntos = remember(lecturas, periodo) { agruparLecturas(lecturas, periodo) }

    // En "1 hora" no hay bloques: promedio, mínima y máxima son lo mismo, así que una sola línea
    val seriesVisibles = if (periodo.agrupacionMillis == null) {
        listOf(SerieGrafico.Promedio)
    } else {
        SerieGrafico.entries.filter { it in series }
    }
    val coloresSeries = SerieGrafico.entries.associateWith { it.color }

    Canvas(modifier = modifier) {
        if (hasta <= desde) return@Canvas

        // Rango vertical: incluye siempre los dos límites y las líneas visibles
        val valores = seriesVisibles.flatMap { serie -> puntos.map(serie.valor) } +
            listOf(temperaturaMinima, temperaturaMaxima)
        val dataMin = valores.min()
        val dataMax = valores.max()
        val margen = maxOf((dataMax - dataMin) * 0.15, 0.5)
        val yMin = dataMin - margen
        val yMax = dataMax + margen

        // Etiquetas del eje Y (4 valores)
        val ticksY = (0..3).map { yMin + (yMax - yMin) * it / 3.0 }
        val etiquetasY = ticksY.map { textMeasurer.measure(formatEjeY(it), ejeStyle) }
        val anchoEjeY = etiquetasY.maxOf { it.size.width } + 6.dp.toPx()
        val altoEjeX = textMeasurer.measure("00:00", ejeStyle).size.height + 6.dp.toPx()

        // Área donde se dibujan los datos
        val left = anchoEjeY
        val right = size.width
        val top = 4.dp.toPx()
        val bottom = size.height - altoEjeX
        if (right <= left || bottom <= top) return@Canvas

        fun xDe(fecha: Long): Float =
            left + (right - left) * ((fecha - desde).toFloat() / (hasta - desde).toFloat())

        fun yDe(temperatura: Double): Float =
            bottom - (bottom - top) * ((temperatura - yMin) / (yMax - yMin)).toFloat()

        // Grilla + etiquetas Y
        ticksY.forEachIndexed { i, valor ->
            val y = yDe(valor)
            drawLine(colorGrilla, Offset(left, y), Offset(right, y), strokeWidth = 1.dp.toPx())
            val etiqueta = etiquetasY[i]
            drawText(
                etiqueta,
                topLeft = Offset(
                    x = anchoEjeY - 6.dp.toPx() - etiqueta.size.width,
                    y = (y - etiqueta.size.height / 2f).coerceIn(0f, size.height - etiqueta.size.height)
                )
            )
        }

        // Etiquetas X: inicio, medio y fin del período
        val formatoX = SimpleDateFormat(
            if (periodo.duracionMillis <= UN_DIA) "HH:mm" else "dd/MM",
            Locale.getDefault()
        )
        listOf(desde, desde + (hasta - desde) / 2, hasta).forEachIndexed { i, fecha ->
            val etiqueta = textMeasurer.measure(formatoX.format(Date(fecha)), ejeStyle)
            val x = when (i) {
                0 -> left
                1 -> xDe(fecha) - etiqueta.size.width / 2f
                else -> right - etiqueta.size.width
            }
            drawText(etiqueta, topLeft = Offset(x, bottom + 6.dp.toPx()))
        }

        // Líneas de temperatura máxima y mínima configuradas
        dibujarLimite(textMeasurer, textoMaximo, yDe(temperaturaMaxima),
            left, right, colorLimite, limiteStyle, etiquetaArriba = true)
        dibujarLimite(textMeasurer, textoMinimo, yDe(temperaturaMinima),
            left, right, colorLimite, limiteStyle, etiquetaArriba = false)

        // Separo los puntos en tramos continuos. Si entre dos puntos pasó mucho más de lo
        // esperado (sensor desconectado), corto las líneas para no inventar datos.
        val saltoMaximo = maxOf(
            maxOf(intervaloLecturaMinutos, 1) * 60_000L * 3,
            (periodo.agrupacionMillis ?: 0L) * 3 / 2
        )
        val tramos = mutableListOf<MutableList<PuntoGrafico>>()
        puntos.forEach { punto ->
            val tramoActual = tramos.lastOrNull()
            if (tramoActual == null || punto.fecha - tramoActual.last().fecha > saltoMaximo) {
                tramos.add(mutableListOf(punto))
            } else {
                tramoActual.add(punto)
            }
        }

        // Una línea por cada serie activa, con un puntito en cada valor
        seriesVisibles.forEach { serie ->
            val path = Path()
            tramos.forEach { tramo ->
                tramo.forEachIndexed { i, punto ->
                    val x = xDe(punto.fecha)
                    val y = yDe(serie.valor(punto))
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
            }
            drawPath(
                path = path,
                color = coloresSeries.getValue(serie),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Si hubiera demasiados puntos, solo la línea
            if (puntos.size <= 40) {
                puntos.forEach { punto ->
                    drawCircle(
                        color = coloresSeries.getValue(serie),
                        radius = 3.dp.toPx(),
                        center = Offset(xDe(punto.fecha), yDe(serie.valor(punto)))
                    )
                }
            }
        }
    }
}

private fun DrawScope.dibujarLimite(
    textMeasurer: TextMeasurer,
    texto: String,
    y: Float,
    left: Float,
    right: Float,
    color: Color,
    style: TextStyle,
    etiquetaArriba: Boolean
) {
    drawLine(
        color = color,
        start = Offset(left, y),
        end = Offset(right, y),
        strokeWidth = 1.5.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 5.dp.toPx()))
    )
    val etiqueta = textMeasurer.measure(texto, style)
    val separacion = 2.dp.toPx()
    val yTexto = if (etiquetaArriba) y - etiqueta.size.height - separacion else y + separacion
    drawText(
        etiqueta,
        topLeft = Offset(
            x = right - etiqueta.size.width,
            y = yTexto.coerceIn(0f, size.height - etiqueta.size.height)
        )
    )
}

/** 4.25 -> "4.3°" */
private fun formatEjeY(valor: Double): String =
    "${String.format(Locale.US, "%.1f", valor)}°"
