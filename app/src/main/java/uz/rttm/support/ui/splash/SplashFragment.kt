package uz.rttm.support.ui.splash

import android.content.pm.ActivityInfo
import android.os.CountDownTimer
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import uz.rttm.support.R
import uz.rttm.support.databinding.FragmentSplashBinding
import uz.rttm.support.models.body.LoginBody
import uz.rttm.support.ui.base.BaseFragment
import uz.rttm.support.utils.NetworkResult
import uz.rttm.support.utils.Prefs
import uz.rttm.support.utils.collectLatestLA
import uz.rttm.support.utils.findNavControllerSafely
import uz.rttm.support.utils.theme.Theme
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {
    @Inject
    lateinit var prefs: Prefs
    private val vm: SplashVIewModel by viewModels()

    override fun onCreate(view: View) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onCreateTheme(themeManager.currentTheme)

        object : CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                startDestination()
            }
        }.start()
    }


    override fun onCreateTheme(theme: Theme) {
        super.onCreateTheme(theme)
        val background = ContextCompat.getColor(requireContext(), theme.backgroundColor)

        binding.splashFragment.setBackgroundColor(background)
    }


    private fun startDestination() {

        if (findNavControllerSafely()?.currentDestination?.id == R.id.splashFragment) {
            if (prefs.get(prefs.token, "") == "" && prefs.get(prefs.userId, 0) == 0) {
                findNavControllerSafely()?.navigate(R.id.action_splashFragment_to_loginFragment)
            } else {
                when (prefs.get(prefs.role, "0")) {
                    prefs.admin -> {
                        findNavControllerSafely()?.navigate(R.id.action_splashFragment_to_admin_mainFragment)
                    }

                    prefs.user -> {
                        findNavControllerSafely()?.navigate(R.id.action_splashFragment_to_user_mainFragment)
                    }

                    prefs.manager -> {
                        findNavControllerSafely()?.navigate(R.id.action_splashFragment_to_manager_mainFragment)
                    }
                }
            }
        }
    }

}