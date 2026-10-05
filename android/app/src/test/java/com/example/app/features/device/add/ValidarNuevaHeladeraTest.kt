package com.example.app.features.device.add

import com.example.app.model.HeladeraRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidarNuevaHeladeraTest {

    @Test
    fun formularioValido_armaElRequest() {
        val resultado = validarNuevaHeladera("  Heladera Bar ", "2", "6", "15")

        assertEquals(
            ValidacionHeladera.Ok(HeladeraRequest("Heladera Bar", 2.0, 6.0, 15)),
            resultado
        )
    }

    @Test
    fun aceptaComaDecimalYNegativos() {
        val resultado = validarNuevaHeladera("Freezer", "-20,5", "-18", "10")

        assertEquals(
            ValidacionHeladera.Ok(HeladeraRequest("Freezer", -20.5, -18.0, 10)),
            resultado
        )
    }

    @Test
    fun nombreVacio_esError() {
        assertTrue(validarNuevaHeladera("   ", "2", "6", "10") is ValidacionHeladera.Error)
    }

    @Test
    fun temperaturaNoNumerica_esError() {
        assertTrue(validarNuevaHeladera("Heladera", "abc", "6", "10") is ValidacionHeladera.Error)
        assertTrue(validarNuevaHeladera("Heladera", "2", "", "10") is ValidacionHeladera.Error)
    }

    @Test
    fun minimaMayorQueMaxima_esError() {
        assertEquals(
            ValidacionHeladera.Error("La temperatura mínima no puede ser mayor que la máxima."),
            validarNuevaHeladera("Heladera", "8", "2", "10")
        )
    }

    @Test
    fun intervaloInvalido_esError() {
        assertTrue(validarNuevaHeladera("Heladera", "2", "6", "0") is ValidacionHeladera.Error)
        assertTrue(validarNuevaHeladera("Heladera", "2", "6", "diez") is ValidacionHeladera.Error)
    }
}
