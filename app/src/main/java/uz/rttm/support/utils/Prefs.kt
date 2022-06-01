package uz.rttm.support.utils

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject


class Prefs @Inject constructor(context: Context) {

    private val prefsName: String = "JbnuuPref"
    private var prefs: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    val token: String = "token"

    val versionCode = "versionCode"
    val versionName = "versionName"
    val versionType = "versionType"
    val countEnter = "countEnter"
    val fam: String = "fam"
    val phone: String = "phone"
    val photo: String = "photo"
    val role: String = "role"
    val email: String = "email"
    val bolim_id: String = "bolim_id"
    val sub_bolim_id: String = "sub_bolim_id"
    val bolim_name: String = "bolim_name"
    val lavozim: String = "lavozim"
    val name: String = "name"
    val manager: String = "2"
    val user: String = "1"
    val admin: String = "3"
    val language: String = "language"
    val userId: String = "user_id"
    val theme: String = "theme"
    val userNameTopicInFireBase: String = "userNameTopicInFireBase"
    val userLogin: String = "userLogin"
//    val username: String = "username"
    val password: String = "password"
    val firebaseToken: String = "FIREBASE_TOKEN"

    fun save(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun save(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun save(key: String, value: Float) {
        prefs.edit().putFloat(key, value).apply()
    }

    fun save(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun save(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    fun get(key: String, defValue: Int) = prefs.getInt(key, defValue)

    fun get(key: String, defValue: String) = prefs.getString(key, defValue) ?: ""

    fun get(key: String, defValue: Float) = prefs.getFloat(key, defValue)

    fun get(key: String, defValue: Boolean) = prefs.getBoolean(key, defValue)

    fun get(key: String, defValue: Long) = prefs.getLong(key, defValue)

    fun clear() {
        prefs.edit().clear().apply()
    }
}