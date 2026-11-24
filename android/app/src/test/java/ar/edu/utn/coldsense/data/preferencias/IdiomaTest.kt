package ar.edu.utn.coldsense.data.preferencias

import org.junit.Assert.assertEquals
import org.junit.Test

class IdiomaTest {

    @Test
    fun desdeTag_leeLosIdiomasDeLaApp() {
        assertEquals(Idioma.Espanol, Idioma.desdeTag("es"))
        assertEquals(Idioma.Ingles, Idioma.desdeTag("en"))
    }

    @Test
    fun desdeTag_sinIdioma_sigueAlCelular() {
        assertEquals(Idioma.Sistema, Idioma.desdeTag(null))
    }

    @Test
    fun desdeTag_idiomaQueLaAppNoTiene_sigueAlCelular() {
        assertEquals(Idioma.Sistema, Idioma.desdeTag("fr"))
    }
}
