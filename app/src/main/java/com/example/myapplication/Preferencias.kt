package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences

object Preferencias {
    private const val PREF_NOMBRE = "prefs"
    private const val PREF_PRIMERA_VEZ = "primera_vez"

    fun getPreferencias(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NOMBRE, Context.MODE_PRIVATE)
    }

    fun esPrimeraVez(context: Context): Boolean {
        val prefs = getPreferencias(context)
        return prefs.getBoolean(PREF_PRIMERA_VEZ, true)
    }

    fun setPrimeraVez(context: Context, primeraVez: Boolean) {
        val prefs = getPreferencias(context)
        prefs.edit().putBoolean(PREF_PRIMERA_VEZ, primeraVez).apply()
    }
}