package uz.rttm.support.utils.theme

import uz.rttm.support.R

class ClassicTheme : Theme() {
    override val id: Long
        get() = CLASSIC_THEME
    override val style: Int
        get() = R.style.ClassicTheme
    override val name: Int
        get() = R.string.day_mode
    override val colorPrimary: Int
        get() = R.color.cl_color_primary
    override val colorPrimaryDark: Int
        get() = R.color.cl_color_primary_dark
    override val colorAccent: Int
        get() = R.color.cl_color_accent
    override val navigationBarColor: Int
        get() = R.color.white
    override val backgroundColor: Int
        get() = R.color.white
    override val textColorPrimary: Int
        get() =  R.color.white
    override val textColor: Int
        get() = R.color.text_color
    override val defTextColor: Int
        get() = R.color.default_text_color
}
