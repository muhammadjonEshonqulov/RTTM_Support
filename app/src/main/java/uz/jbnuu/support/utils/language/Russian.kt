package uz.jbnuu.support.utils.language


class Russian: Language() {
    override val id: Int
        get() = Language.RU
    override val userName: String
        get() = "ru"
    override val name: String
        get() ="Русский"
}