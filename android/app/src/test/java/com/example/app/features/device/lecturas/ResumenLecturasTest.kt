package com.example.app.features.device.lecturas

import com.example.app.model.Lectura
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumenLecturasTest {

    private fun lecturas(vararg temperaturas: Double) =
        temperaturas.mapIndexed { i, t -> Lectura(fecha = i.toLong(), temperatura = t) }

    @Test
    fun sinLecturas_devuelveNull() {
        assertNull(resumirLecturas(emptyList(), 2.0, 6.0))
    }

    @Test
    fun calculaMinimaMaximaYPromedio() {
        val resumen = resumirLecturas(lecturas(3.0, 5.0, 4.0), 2.0, 6.0)!!

        assertEquals(3.0, resumen.minima, 0.001)
        assertEquals(5.0, resumen.maxima, 0.001)
        assertEquals(4.0, resumen.promedio, 0.001)
        assertEquals(0, resumen.fueraDeRango)
    }

    @Test
    fun cuentaLasLecturasFueraDeRango() {
        val resumen = resumirLecturas(lecturas(1.5, 4.0, 6.5, 7.0), 2.0, 6.0)!!

        assertEquals(3, resumen.fueraDeRango)
    }

    @Test
    fun losLimitesCuentanComoDentroDelRango() {
        assertFalse(Lectura(0, 2.0).estaFueraDeRango(2.0, 6.0))
        assertFalse(Lectura(0, 6.0).estaFueraDeRango(2.0, 6.0))
        assertTrue(Lectura(0, 6.1).estaFueraDeRango(2.0, 6.0))
    }

    @Test
    fun funcionaConTemperaturasNegativasDeFreezer() {
        val resumen = resumirLecturas(lecturas(-16.5, -19.0), -20.0, -18.0)!!

        assertEquals(-19.0, resumen.minima, 0.001)
        assertEquals(1, resumen.fueraDeRango)
    }
}
