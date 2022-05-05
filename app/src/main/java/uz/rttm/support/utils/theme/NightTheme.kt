package uz.rttm.support.utils.theme

import uz.rttm.support.R


class NightTheme : Theme() {
    override val id: Long
        get() = NIGHT_THEME
    override val style: Int
        get() = R.style.NightTheme
    override val name: Int
        get() = R.string.night_mode
    override val colorPrimary: Int
        get() = R.color.n_color_primary
    override val colorPrimaryDark: Int
        get() = R.color.n_color_primary
    override val colorAccent: Int
        get() = R.color.n_color_accent
    override val navigationBarColor: Int
        get() = R.color.n_color_primary
    override val backgroundColor: Int
        get() = R.color.n_background
    override val textColorPrimary: Int
        get() = R.color.white
    override val textColor: Int
        get() = R.color.n_text_color
    override val defTextColor: Int
        get() = R.color.n_def_text_color


}