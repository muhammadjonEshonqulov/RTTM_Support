package uz.rttm.support.utils.language

import uz.rttm.support.utils.language.Language

class Uzbek : Language() {
    override val id: Int
        get() = Language.UZ
    override val userName: String
        get() = "uz"
    override val name: String
        get() = "O'zbekcha"
}