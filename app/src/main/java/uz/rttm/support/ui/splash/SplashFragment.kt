package uz.rttm.support.ui.splash

import android.content.pm.ActivityInfo
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uz.intalim.ui.splash.SplashVIewModel
import uz.rttm.support.R
import uz.rttm.support.databinding.FragmentSplashBinding
import uz.rttm.support.models.body.LoginBody
import uz.rttm.support.ui.base.BaseFragment
import uz.rttm.support.utils.NetworkResult
import uz.rttm.support.utils.Prefs
import uz.rttm.support.utils.findNavControllerSafely
import uz.rttm.support.utils.hasInternetConnection
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


        activity?.application?.let {
            if (hasInternetConnection(it)) {
                vm.getMe()
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        vm.getMeResponse.collect {
                            when (it) {
                                is NetworkResult.Success -> {
                                    it.data?.app_version?.apply {
                                        version?.let {
                                            prefs.save(prefs.versionCode, it)
                                        }
                                        name?.let {
                                            prefs.save(prefs.versionName, it)
                                        }
                                        type?.let {
                                            prefs.save(prefs.versionType, it)
                                        }
                                    }
                                    startDestination()
                                }
//                                is NetworkResult.Loading -> {
//                                    startDestination()
//                                }
                                is NetworkResult.Error -> {
                                    if (it.code == 401) {
                                        login()
                                    } else {
                                        startDestination()
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(500).also {
                        startDestination()
                    }
                }
            }
        }

    }

    private fun login() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.login(LoginBody(prefs.get(prefs.email, ""), prefs.get(prefs.password, "")))
                vm.loginResponse.collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.token?.let {
                                prefs.save(prefs.token, it)
                                startDestination()
                            }
                        }
                        is NetworkResult.Error -> {
                            if (findNavControllerSafely()?.currentDestination?.id == R.id.splashFragment) {
                                findNavControllerSafely()?.navigate(R.id.action_splashFragment_to_loginFragment)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreateTheme(theme: Theme) {
        super.onCreateTheme(theme)
//        val primaryColor = ContextCompat.getColor(requireContext(), theme.colorPrimary)
//        val textColor = ContextCompat.getColor(requireContext(), theme.textColor)
//        val defTextColor = ContextCompat.getColor(requireContext(), theme.defTextColor)
//        val textColorPrimary = ContextCompat.getColor(requireContext(), theme.textColorPrimary)
        val background = ContextCompat.getColor(requireContext(), theme.backgroundColor)

        binding.splashFragment.setBackgroundColor(background)
    }

    private fun showError(s: String) {
        snackBar(s)
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