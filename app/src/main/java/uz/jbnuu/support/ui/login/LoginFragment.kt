package uz.jbnuu.support.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uz.jbnuu.support.databinding.LoginFragmentBinding
import uz.jbnuu.support.models.body.LoginBody
import uz.jbnuu.support.utils.Constants.Companion.CUSTOMER_PASS
import uz.jbnuu.support.utils.Constants.Companion.USER_ABROR
import uz.jbnuu.support.utils.Constants.Companion.USER_Asilbek
import uz.jbnuu.support.utils.Constants.Companion.USER_Boss
import uz.jbnuu.support.utils.Constants.Companion.USER_JAMSHID
import uz.jbnuu.support.utils.Constants.Companion.USER_Muhammad
import uz.jbnuu.support.utils.NetworkResult
import uz.jbnuu.support.utils.Prefs
import uz.jbnuu.support.utils.snack
import uz.jbnuu.support.viewmodels.LoginViewModel
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment(), View.OnClickListener {


    lateinit var binding: LoginFragmentBinding

//    @Inject
//    lateinit var prefs: Prefs

    private val vm: LoginViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = LoginFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginBtn.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.loginBtn -> {
                val userName = binding.loginAuth.text.toString()
                val password = binding.passwordAuth.text.toString()
                if (userName.isNotEmpty() && password.isNotEmpty()) {
                    if (userName.length and password.length < 6) {
                        vm.login(LoginBody(userName, password))
                        vm.loginResponse.onEach {
                            when(it){
                                is NetworkResult.Loading ->{
                                    snack(requireView(),"Loading ... " )
                                }
                                is NetworkResult.Error -> {
                                    snack(requireView(), "error ->"+it)
//                                    Log.d("TTT", ""+it.message)

                                }
                                is NetworkResult.Success -> {
                                    snack(requireView(), "Response -> "+it.data)
//                                    Log.i("TTT", "zo'r "+it.data)

                                }
                            }
                        }.launchIn(viewLifecycleOwner.lifecycleScope)
//                        if (password == CUSTOMER_PASS) {
//                            val role = ""
//                            when (userName) {
//                                USER_Boss -> {
//                                    prefs.save(prefs.role, prefs.user)
//                                    prefs.save(prefs.userLogin, USER_Boss)
//                                }
//                                USER_ABROR -> {
//                                    prefs.save(prefs.role, prefs.customer)
//                                    prefs.save(prefs.userLogin, USER_ABROR)
//                                }
//                                USER_JAMSHID -> {
//                                    prefs.save(prefs.role, prefs.user)
//                                    prefs.save(prefs.userLogin, USER_JAMSHID)
//                                }
//                                USER_Muhammad -> {
//                                    prefs.save(prefs.role, prefs.user)
//                                    prefs.save(prefs.userLogin, USER_Muhammad)
//                                }
//                                USER_Asilbek -> {
//                                    prefs.save(prefs.role, prefs.customer)
//                                    prefs.save(prefs.userLogin, USER_Asilbek)
//                                }
////                                    prefs.save(prefs.role, prefs.customer)
////                                    if (findNavController().currentDestination?.id == R.id.loginFragment){
////                                        findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
////                                    }
////
////                                    prefs.save(prefs.role, prefs.user)
////                                    if (findNavController().currentDestination?.id == R.id.loginFragment){
////                                        findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
////                                    }
//
//                            }
//                        } else {
//                            snack(requireView(), "Login yoki parol xato")
//                        }
                    } else {
                        snack(requireView(), "Login va parol 6 xonadan kam bo'lmasligi kerak!")
                    }

                } else {
                    if (userName.isEmpty() && password.isEmpty()) {
                        snack(requireView(), "Login va parolni kiriting")
                    } else if (userName.isEmpty()) {
                        snack(requireView(), "Loginni kiriting")
                    } else if (password.isEmpty()) {
                        snack(requireView(), "Parolni kiriting")
                    }
                }
            }
            binding.passwordShow -> {

            }
        }
    }
}