package uz.rttm.support.utils.language

import android.content.Context
import uz.rttm.support.utils.Prefs

class LanguageManager(val context: Context) {

    var languages: ArrayList<Language> = ArrayList()
    val prefs = Prefs(context)
    init {
        languages.add(Uzbek())
        languages.add(Russian())
        languages.add(English())
        languages.add(Krill())
        languages.add(Qoroqolpoq())
    }

    var currentLanguage : Language
    get() = findLanguageById(prefs.get(prefs.language,languages[0].id))
    set(value) = prefs.save(prefs.language,value.id)

    fun findLanguageById(id:Int): Language {
        languages.forEach {
            if (it.id == id)
                return it
        }
        return languages[0]
    }

}