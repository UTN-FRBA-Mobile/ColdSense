package ar.edu.utn.coldsense.data.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsuarioDemoTest {

    @Test
    fun emailYContrasenaDelDemo_esUsuarioDemo() {
        assertTrue(esUsuarioDemo(AuthCredentials("demo@coldsense.com", "demo1234")))
    }

    @Test
    fun emailSinDistinguirMayusculas_esUsuarioDemo() {
        assertTrue(esUsuarioDemo(AuthCredentials("Demo@ColdSense.com", "demo1234")))
    }

    @Test
    fun contrasenaIncorrecta_noEsUsuarioDemo() {
        assertFalse(esUsuarioDemo(AuthCredentials("demo@coldsense.com", "otra")))
        assertFalse(esUsuarioDemo(AuthCredentials("demo@coldsense.com", "DEMO1234")))
    }

    @Test
    fun otroEmail_noEsUsuarioDemo() {
        assertFalse(esUsuarioDemo(AuthCredentials("alguien@coldsense.com", "demo1234")))
    }
}
