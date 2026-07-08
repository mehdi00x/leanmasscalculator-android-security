package com.example.leanmasscalculator.security

import android.content.Context
import android.widget.Toast
import java.io.File

/**
 * MASVS-RESILIENCE-1 : Détection de root et contrôle de l'intégrité de l'environnement.
 * MASVS-AUTH-1       : Gestion du timeout de session.
 */
object SecurityUtils {

    // ── Timeout de session : 30 minutes d'inactivité ──────────────────────
    private const val SESSION_TIMEOUT_MS = 30 * 60 * 1000L   // 30 min
    private const val PREF_NAME          = "secure_session"
    private const val KEY_LAST_ACTIVE    = "last_active_ts"

    /** Enregistre le timestamp de la dernière activité. */
    fun recordActivity(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_ACTIVE, System.currentTimeMillis())
            .apply()
    }

    /** Retourne true si la session a expiré (inactivité > 30 min). */
    fun isSessionExpired(context: Context): Boolean {
        val prefs     = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastActive = prefs.getLong(KEY_LAST_ACTIVE, 0L)
        if (lastActive == 0L) return false   // Première connexion
        return (System.currentTimeMillis() - lastActive) > SESSION_TIMEOUT_MS
    }

    /** Efface le timestamp de session (à la déconnexion). */
    fun clearSession(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    // ── Détection de root ─────────────────────────────────────────────────
    private val ROOT_INDICATORS = arrayOf(
        "/system/app/Superuser.apk",
        "/system/app/SuperSU.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su"
    )

    /**
     * MASVS-RESILIENCE-1 : Vérifie la présence de binaires su ou d'APK de root connus.
     * Ne bloque pas l'app — avertit l'utilisateur et journalise l'anomalie.
     */
    fun isDeviceRooted(): Boolean {
        return ROOT_INDICATORS.any { File(it).exists() }
    }

    /**
     * Affiche un avertissement si le device est rooté.
     * À appeler dans onCreate() de chaque Activity sensible.
     */
    fun warnIfRooted(context: Context) {
        if (isDeviceRooted()) {
            Toast.makeText(
                context,
                "⚠️ Appareil rooté détecté. La sécurité de vos données peut être compromise.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
