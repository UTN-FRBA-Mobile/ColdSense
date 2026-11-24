package ar.edu.utn.coldsense.data.preferencias

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.core.content.edit
import java.util.Locale

/** Idioma de la app. [tag] es el código del idioma (null = el del celular). */
enum class Idioma(val tag: String?) {
    Sistema(null),
    Espanol("es"),
    Ingles("en");

    companion object {
        /** Lo guardado o lo que eligió el usuario en Android; si no es un idioma de la app, sigue al celular. */
        fun desdeTag(tag: String?): Idioma =
            entries.firstOrNull { it.tag != null && it.tag == tag } ?: Sistema
    }
}

/**
 * Guarda el idioma elegido para la app, aparte del idioma del celular.
 *
 * En Android 13+ lo guarda el sistema, así que también se puede cambiar desde
 * los ajustes de Android. En versiones anteriores lo guardo en el celular y lo
 * aplico cada vez que se crea la Activity ([aplicar]).
 */
object PreferenciasIdioma {

    private const val ARCHIVO = "preferencias"
    private const val CLAVE_IDIOMA = "idioma"

    fun actual(context: Context): Idioma =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
            Idioma.desdeTag(if (locales.isEmpty) null else locales[0].language)
        } else {
            Idioma.desdeTag(prefs(context).getString(CLAVE_IDIOMA, null))
        }

    /** Cambia el idioma. La Activity se vuelve a crear para mostrar los textos nuevos. */
    fun cambiar(activity: Activity, idioma: Idioma) {
        if (idioma == actual(activity)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android recrea la Activity solo
            activity.getSystemService(LocaleManager::class.java).applicationLocales =
                idioma.tag?.let { LocaleList.forLanguageTags(it) } ?: LocaleList.getEmptyLocaleList()
        } else {
            prefs(activity).edit(commit = true) { putString(CLAVE_IDIOMA, idioma.tag) }
            activity.recreate()
        }
    }

    /** Android 12 o anterior: el contexto con el idioma elegido, para usar en attachBaseContext. */
    fun aplicar(base: Context): Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return base
        val tag = prefs(base).getString(CLAVE_IDIOMA, null) ?: return base

        val configuracion = Configuration(base.resources.configuration)
        configuracion.setLocale(Locale.forLanguageTag(tag))
        return base.createConfigurationContext(configuracion)
    }

    private fun prefs(context: Context) = context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
}
