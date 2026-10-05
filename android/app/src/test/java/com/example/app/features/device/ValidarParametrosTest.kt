package com.example.app.features.device

import com.example.app.model.ParametrosRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidarParametrosTest {

    @Test
    fun parametrosValidos_armaElRequest() {
        assertEquals(
            ValidacionParametros.Ok(ParametrosRequest(2.0, 8.0, 15)),
            validarParametros(" 2 ", "8", "15")
        )
    }

    @Test
    fun aceptaComaDecimalYNegativos() {
        assertEquals(
            ValidacionParametros.Ok(ParametrosRequest(-20.5, -18.0, 10)),
            validarParametros("-20,5", "-18", "10")
        )
    }

    @Test
    fun minimaIgualAMaxima_esValido() {
        assertTrue(validarParametros("4", "4", "10") is ValidacionParametros.Ok)
    }

    @Test
    fun minimaMayorQueMaxima_esError() {
        assertEquals(
            ValidacionParametros.Error("La temperatura mínima no puede ser mayor que la máxima."),
            validarParametros("8", "2", "10")
        )
    }

    @Test
    fun valoresNoNumericos_sonError() {
        assertTrue(validarParametros("", "6", "10") is ValidacionParametros.Error)
        assertTrue(validarParametros("2", "seis", "10") is ValidacionParametros.Error)
        assertTrue(validarParametros("2", "6", "0") is ValidacionParametros.Error)
    }

    @Test
    fun aTextoDeCampo_sacaElPuntoCeroSoloSiEsEntero() {
        assertEquals("2", 2.0.aTextoDeCampo())
        assertEquals("-18", (-18.0).aTextoDeCampo())
        assertEquals("-18.5", (-18.5).aTextoDeCampo())
    }
}
