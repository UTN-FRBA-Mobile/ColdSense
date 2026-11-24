package ar.edu.utn.coldsense.data.preferencias

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModoTemaTest {

    @Test
    fun sistema_sigueAlCelular() {
        assertTrue(ModoTema.Sistema.esOscuro(sistemaOscuro = true))
        assertFalse(ModoTema.Sistema.esOscuro(sistemaOscuro = false))
    }

    @Test
    fun claroYOscuro_ignoranAlCelular() {
        assertFalse(ModoTema.Claro.esOscuro(sistemaOscuro = true))
        assertTrue(ModoTema.Oscuro.esOscuro(sistemaOscuro = false))
    }

    @Test
    fun desdeGuardado_leeElValorGuardado() {
        assertEquals(ModoTema.Oscuro, ModoTema.desdeGuardado("Oscuro"))
        assertEquals(ModoTema.Claro, ModoTema.desdeGuardado("Claro"))
    }

    @Test
    fun desdeGuardado_sinValorOInvalido_sigueAlSistema() {
        assertEquals(ModoTema.Sistema, ModoTema.desdeGuardado(null))
        assertEquals(ModoTema.Sistema, ModoTema.desdeGuardado("cualquier cosa"))
    }
}
