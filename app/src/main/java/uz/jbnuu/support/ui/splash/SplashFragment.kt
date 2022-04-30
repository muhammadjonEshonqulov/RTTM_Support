package uz.jbnuu.support.ui.splash

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
import uz.jbnuu.support.R
import uz.jbnuu.support.databinding.FragmentSplashBinding
import uz.jbnuu.support.models.body.LoginBody
import uz.jbnuu.support.ui.base.BaseFragment
import uz.jbnuu.support.utils.NetworkResult
import uz.jbnuu.support.utils.Prefs
import uz.jbnuu.support.utils.findNavControllerSafely
import uz.jbnuu.support.utils.hasInternetConnection
import uz.jbnuu.support.utils.theme.Theme
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>(FragmentSplashBinding::inflate) {
    @Inject
    lateinit var prefss: Prefs
    private val vm: SplashVIewModel by viewModels()

    override fun onCreate(view: View) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onCreateTheme(themeManager.currentTheme)

        viewLifecycleOwner.lifecycleScope.launch {
            delay(500)
            startDestination()
        }

//        activity?.application?.let {
//            if (hasInternetConnection(it)){
//                viewLifecycleOwner.lifecycleScope.launch {
//                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
//                        vm.login(LoginBody(prefss.get(prefss.email, ""), prefss.get(prefss.password, "")))
//                        vm.loginResponse.collect {
//                            when(it){
//                                is NetworkResult.Success->{
//                                    it.data?.token?.let {
//                                        prefss.save(prefss.token, it)
//                                    }
//                                    startDestination()
//                                }
//                                is NetworkResult.Loading->{
//                                    startDestination()
//                                }
//                                is NetworkResult.Error->{
//                                    startDestination()                                }
//                            }
//                        }
//                    }
//                }
//            } else {
//                viewLifecycleOwner.lifecycleScope.launch {
//                    delay(500).also {
//                        startDestination()
//                    }
//                }
//            }
//        }

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
    
    
    private fun startDestination(){

        if (findNavControllerSafely()?.currentDestination?.id == R.id.splashFragment){
            if (prefss.get( prefss.token, "") == "" && prefss.get( prefss.userId, 0) == 0){
                findNavControllerSafely()?.navigate(R.id.action_splashFragment_to_loginFragment)
            } else {
                 when(prefss.get(prefss.role, "0")){
                     prefss.admin -> {
                         findNavControllerSafely()?.navigate(R.id.action_splashFragment_to_admin_mainFragment)
                     }
                     prefss.user -> {
                         findNavControllerSafely()?.navigate(R.id.action_splashFragment_to_user_mainFragment)
                     }
                     prefss.manager -> {
                         findNavControllerSafely()?.navigate(R.id.action_splashFragment_to_manager_mainFragment)
                     }
                 }
            }
        }
    }
    
}