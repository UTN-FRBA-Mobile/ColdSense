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

// Colores del gráfico
private val TextPrimary = Color(0xFF1A1F36)
private val TextSecondary = Color(0xFF6B7280)
private val PrimaryBlue = Color(0xFF1E6FE0)
private val CardBorder = Color(0xFFE5E8EF)
private val SurfaceGray = Color(0xFFF5F6F8)
private val GridGray = Color(0xFFEDEFF3)
private val LimiteRed = Color(0xFFEF4444)

/** Líneas que se pueden mostrar en el gráfico, cada una con su color. */
enum class SerieGrafico(val label: String, val color: Color, val valor: (PuntoGrafico) -> Double) {
    Promedio("Promedio", Color(0xFF1E6FE0), { it.promedio }),
    Maxima("Máxima", Color(0xFFF59E0B), { it.maxima }),
    Minima("Mínima", Color(0xFF0EA5A4), { it.minima })
}

private const val UNA_HORA = 60 * 60 * 1000L
private const val UN_DIA = 24 * UNA_HORA

/**
 * Períodos que se pueden elegir para el gráfico.
 *
 * @param agrupacionMillis Tamaño del bloque que se promedia en un solo punto
 *                         (null = se muestra cada lectura).
 * @param nombreBloque     Cómo se nombra ese bloque en la leyenda.
 */
enum class PeriodoGrafico(
    val label: String,
    val duracionMillis: Long,
    val agrupacionMillis: Long?,
    val nombreBloque: String?
) {
    UnaHora("1 hora", UNA_HORA, null, null),
    VeinticuatroHoras("24 horas", UN_DIA, UNA_HORA, "hora"),
    SieteDias("7 días", 7 * UN_DIA, 6 * UNA_HORA, "6 horas"),
    UnMes("1 mes", 30 * UN_DIA, UN_DIA, "día")
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
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> Spinner()

                state.hasError -> Text(
                    text = "No se pudo cargar el gráfico.\nTocá para reintentar.",
                    fontSize = 14.sp,
                    color = TextSecondary,
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
                            text = "Sin lecturas en este período",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Selector de líneas (solo cuando hay bloques; en "1 hora" cada punto es
        // una sola lectura, así que promedio, mínima y máxima serían la misma línea)
        val bloque = state.periodo.nombreBloque
        if (bloque != null) {
            SerieSelector(activas = state.series, onClick = onSerieClick)
            Text(
                text = "Valores por $bloque",
                fontSize = 12.sp,
                color = TextSecondary,
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
                    .background(if (activa) serie.color.copy(alpha = 0.12f) else SurfaceGray)
                    .border(
                        width = 1.dp,
                        color = if (activa) serie.color else CardBorder,
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
                        .background(if (activa) serie.color else TextSecondary.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = serie.label,
                    fontSize = 13.sp,
                    fontWeight = if (activa) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (activa) TextPrimary else TextSecondary
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
                    .background(if (isSelected) PrimaryBlue else SurfaceGray)
                    .selectable(
                        selected = isSelected,
                        onClick = { onClick(periodo) },
                        role = Role.Tab
                    )
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = periodo.label,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) Color.White else TextPrimary,
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
    val ejeStyle = TextStyle(fontSize = 10.sp, color = TextSecondary)
    val limiteStyle = TextStyle(fontSize = 10.sp, color = LimiteRed, fontWeight = FontWeight.SemiBold)

    // Puntos a dibujar: cada lectura en "1 hora", bloques en el resto
    val puntos = remember(lecturas, periodo) { agruparLecturas(lecturas, periodo) }

    // En "1 hora" no hay bloques: promedio, mínima y máxima son lo mismo, así que una sola línea
    val seriesVisibles = if (periodo.agrupacionMillis == null) {
        listOf(SerieGrafico.Promedio)
    } else {
        SerieGrafico.entries.filter { it in series }
    }

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
            drawLine(GridGray, Offset(left, y), Offset(right, y), strokeWidth = 1.dp.toPx())
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
        dibujarLimite(textMeasurer, "Máx ${formatLimite(temperaturaMaxima)}", yDe(temperaturaMaxima),
            left, right, limiteStyle, etiquetaArriba = true)
        dibujarLimite(textMeasurer, "Mín ${formatLimite(temperaturaMinima)}", yDe(temperaturaMinima),
            left, right, limiteStyle, etiquetaArriba = false)

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
                color = serie.color,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Si hubiera demasiados puntos, solo la línea
            if (puntos.size <= 40) {
                puntos.forEach { punto ->
                    drawCircle(
                        color = serie.color,
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
    style: TextStyle,
    etiquetaArriba: Boolean
) {
    drawLine(
        color = LimiteRed,
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
