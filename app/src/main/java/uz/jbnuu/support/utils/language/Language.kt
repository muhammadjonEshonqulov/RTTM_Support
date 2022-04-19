package uz.jbnuu.support.utils.language

abstract class Language {
    abstract val id: Int
    abstract val userName: String
    abstract val name:String

    companion object{
        const val UZ = 1
        const val RU = 2
        const val KR = 3
        const val EN = 4
        const val QQ = 5

        fun  getNameByLanguage(uz:String?, ru:String?, en:String?, kr:String?, qq:String?, language: Language):String?{
            return when(language.id){
                UZ -> if(uz?.isNotEmpty() == true) uz else null
                RU -> if(ru?.isNotEmpty() == true) ru else if(uz?.isNotEmpty() == true) uz else null
                EN -> if(en?.isNotEmpty() == true) en else if(uz?.isNotEmpty() == true) uz else null
                QQ -> if(qq?.isNotEmpty() == true) qq else if(uz?.isNotEmpty() == true) uz else null
                KR -> if(kr?.isNotEmpty() == true) kr else if(uz?.isNotEmpty() == true) uz else null
                else -> null
            }
        }
    }
    
    
}