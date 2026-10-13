package com.example.app.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Sesión del usuario guardada en el celular, para no pedir login cada vez que se abre
 * la app (por ejemplo, al tocar una notificación con la app cerrada). Cerrar sesión la borra.
 *
 * Se inicializa en MainActivity antes de mostrar la primera pantalla.
 */
object SesionStorage {

    private const val ARCHIVO = "sesion"
    private const val CLAVE_EMAIL = "email"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
    }

    val haySesion: Boolean
        get() = prefs.contains(CLAVE_EMAIL)

    fun guardar(email: String) {
        prefs.edit { putString(CLAVE_EMAIL, email) }
    }

    fun cerrar() {
        prefs.edit { remove(CLAVE_EMAIL) }
    }
}
