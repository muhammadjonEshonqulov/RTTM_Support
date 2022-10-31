package uz.rttm.support.ui.base

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import uz.rttm.support.R
import uz.rttm.support.app.App
import uz.rttm.support.utils.findNavControllerSafely
import uz.rttm.support.utils.language.Language
import uz.rttm.support.utils.language.LanguageManager
import uz.rttm.support.utils.theme.ClassicTheme
import uz.rttm.support.utils.theme.Theme
import uz.rttm.support.utils.theme.ThemeManager
import java.util.*


typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<VB : ViewBinding>(private val inflate: Inflate<VB>) : Fragment() {
    private var isUseBackPress = true
    lateinit var themeManager: ThemeManager
    lateinit var languageManager: LanguageManager

    private var _binding: VB? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        binding.root.context?.let {
            themeManager = ThemeManager(it)
            languageManager = LanguageManager(it)
        }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, e ->
            if (keyCode == KeyEvent.KEYCODE_BACK && e.action == KeyEvent.ACTION_DOWN) {
                isUseBackPress = true
                onBackPressed()
                return@setOnKeyListener isUseBackPress
            } else return@setOnKeyListener false
        }
        onCreate(view)
        notifyLanguageChanged()
        notifyThemeChanged()
    }

    abstract fun onCreate(view: View)

    open fun onBackPressed() {
        isUseBackPress = false
    }

    fun finish() {
        findNavControllerSafely()?.popBackStack()
    }

    fun hideKeyBoard() {
        val view = activity?.currentFocus ?: View(activity)
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun EditText.showKeyboard() {
        this.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)

    }
    fun snackBarAction(message: String) {
        try {
            binding.root.let {
                val snackbar = Snackbar.make(it, message, Snackbar.LENGTH_INDEFINITE)
                snackbar.setAction("Yopmoq") {
                    snackbar.dismiss()
                }
                val textView: TextView = snackbar.view.findViewById(com.google.android.material.R.id.snackbar_text)
                textView.maxLines = 6
                snackbar.show()
            }
        } catch (e: Exception) {

        }
    }
    fun snackBar(message: String) {
        try {
            binding.root.let {
                val themeId = themeManager.currentTheme.id
                val snackbar = Snackbar.make(it, message, Snackbar.LENGTH_SHORT)
                snackbar.view.setBackgroundColor(
                    ContextCompat.getColor(
                        it.context,
                        if (themeId == ClassicTheme().id) R.color.cl_color_primary else R.color.black
                    )
                )
                snackbar.setTextColor(ContextCompat.getColor(it.context, R.color.white))
                snackbar.show()
            }
        } catch (e: Exception) {
            Toast.makeText(App.context, message, Toast.LENGTH_SHORT).show()
        } catch (e:java.lang.Exception){
            Toast.makeText(App.context, message, Toast.LENGTH_SHORT).show()

        }
    }

    protected fun notifyThemeChanged() = onCreateTheme(themeManager.currentTheme)
    protected fun notifyLanguageChanged() = onCreateLanguage(languageManager.currentLanguage)

    open fun onCreateTheme(theme: Theme) {
        view?.context?.let {
            if (theme.id == Theme.CLASSIC_THEME) {
                val statusBarColor = ContextCompat.getColor(it, theme.colorPrimaryDark)
                val navigationBarColor = ContextCompat.getColor(it, theme.navigationBarColor)
                activity?.window?.statusBarColor = statusBarColor
                activity?.window?.navigationBarColor = navigationBarColor
                activity?.window?.decorView?.let { view ->
                    view.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            } else {
                val statusBarColor = ContextCompat.getColor(it, theme.colorPrimaryDark)
                val navigationBarColor = ContextCompat.getColor(it, theme.navigationBarColor)
                activity?.window?.statusBarColor = statusBarColor
                activity?.window?.navigationBarColor = navigationBarColor
                activity?.window?.decorView?.let { view ->
                    view.systemUiVisibility = 0
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.let {
                if (theme.id == Theme.CLASSIC_THEME) {
                    val navigationBarColor = ContextCompat.getColor(it, theme.navigationBarColor)
                    activity?.window?.navigationBarColor = navigationBarColor
                    activity?.window?.decorView?.let { view ->
                        view.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                } else {
                    val navigationBarColor = ContextCompat.getColor(it, theme.navigationBarColor)
                    activity?.window?.navigationBarColor = navigationBarColor
                    activity?.window?.decorView?.let { view ->
                        view.systemUiVisibility = 0
                    }
                }
            }
        }
    }

    open fun onCreateLanguage(language: Language) {
        binding.root.apply {
            val configuration = Configuration()
            configuration.setLocale(
                Locale(languageManager.currentLanguage.userName)
            )
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }
}

