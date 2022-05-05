package uz.rttm.support.utils.theme

import android.content.Context
import uz.rttm.support.utils.Prefs

class ThemeManager(val context: Context) {

    var themes: ArrayList<Theme> = ArrayList()
    private val prefs = Prefs(context)

    init {
        themes.add(ClassicTheme())
        themes.add(NightTheme())
    }

    var  currentTheme: Theme
        get() = findThemeById(prefs.get(prefs.theme, getDefaultTheme().id))
        set(value) {
            prefs.save( prefs.theme, value.id)
        }

    private fun findThemeById(id: Long): Theme {
        themes.forEach {
            if (it.id == id)
                return it
        }
        return getDefaultTheme()
    }

    private fun getDefaultTheme(): Theme {
        return themes[0]
    }

    fun getAllThemes() = themes
}