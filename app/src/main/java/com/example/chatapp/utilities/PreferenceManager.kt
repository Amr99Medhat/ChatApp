package com.example.chatapp.utilities

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private var sharedPreferences: SharedPreferences? = null


    init {
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    fun putBoolean(key: String, value: Boolean) {
        val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
        editor?.putBoolean(key, value)
        editor?.apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreferences!!.getBoolean(key, false)
    }

    fun putString(key: String, value: String) {
        val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
        editor?.putString(key, value)
        editor?.apply()
    }

    fun getString(key: String): String? {
        return sharedPreferences!!.getString(key, null)
    }

    fun clear() {
        val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
        editor?.clear()
        editor?.apply()
    }
}