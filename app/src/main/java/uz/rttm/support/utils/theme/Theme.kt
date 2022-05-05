package uz.rttm.support.utils.theme

abstract class Theme {
    abstract val id: Long
    abstract val style: Int
    abstract val name: Int
    abstract val colorPrimary: Int
    abstract val colorPrimaryDark: Int
    abstract val colorAccent: Int
    abstract val navigationBarColor: Int
    abstract val backgroundColor: Int
    abstract val textColorPrimary: Int
    abstract val textColor: Int
    abstract val defTextColor: Int

    companion object {
        const val CLASSIC_THEME = 0L
        const val NIGHT_THEME = 1L
    }

}