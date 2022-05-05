package uz.rttm.support.ui.login

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import uz.rttm.support.R
import uz.rttm.support.databinding.LoginFragmentBinding
import uz.rttm.support.models.body.LoginBody
import uz.rttm.support.ui.base.BaseFragment
import uz.rttm.support.ui.base.ProgressDialog
import uz.rttm.support.utils.*
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : BaseFragment<LoginFragmentBinding>(LoginFragmentBinding::inflate), View.OnClickListener {

    @Inject
    lateinit var prefs: Prefs

    private val vm: LoginViewModel by viewModels()

    var progressDialog: ProgressDialog? = null


    override fun onCreate(view: View) {
        binding.loginBtn.setOnClickListener(this)
        binding.loginRegistration.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.loginBtn -> {
                hideKeyBoard()
                val userName = binding.loginAuth.text.toString().lowercase()
                val password = binding.passwordAuth.text.toString().lowercase()
                if (userName.isNotEmpty() && password.isNotEmpty()) {
                    if (userName.endsWith("@jbnuu.uz") && userName.split("@jbnuu.uz").first().isNotEmpty()){
                        vm.login(LoginBody(userName, password))
                        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                            vm.loginResponse.collect {
                                when (it) {
                                    is NetworkResult.Loading -> {
                                        showLoader()
                                    }
                                    is NetworkResult.Error -> {
                                        closeLoader()
                                        snack(requireView(), it.message.toString())
                                    }
                                    is NetworkResult.Success -> {
                                        closeLoader()
                                        it.data?.apply {
                                            token?.let {
                                                prefs.save(prefs.token, it)
                                            }
                                            user?.apply {
                                                bolim?.apply {
                                                    id?.let {
                                                        prefs.save(prefs.sub_bolim_id, it)
                                                    }
                                                    bolim_id?.let {
                                                        prefs.save(prefs.bolim_id, it)
                                                    }
                                                }
                                                id?.let {
                                                    prefs.save(prefs.userId, it)
                                                }
                                                fam?.let {
                                                    prefs.save(prefs.fam, it)
                                                }
                                                phone?.let {
                                                    prefs.save(prefs.phone, it)
                                                }
                                                photo?.let {
                                                    prefs.save(prefs.photo, it)
                                                }
                                                role?.let {
                                                    prefs.save(prefs.role, it)
                                                }
                                                name?.let {
                                                    prefs.save(prefs.name, it)
                                                }
                                                lavozim?.let {
                                                    prefs.save(prefs.lavozim, it)
                                                }
                                                bolim?.name?.let {
                                                    prefs.save(prefs.bolim_name, it)
                                                }
                                            }
                                        }
                                        when (prefs.get(prefs.role, "")){
                                            prefs.manager -> {
                                                prefs.save(prefs.email, userName)
                                                prefs.save(prefs.password, password)
                                                val userNameTopicInFireBase = userName.split("@jbnuu.uz").first().toString()
                                                prefs.save(prefs.userNameTopicInFireBase, userNameTopicInFireBase)
                                                FirebaseMessaging.getInstance().subscribeToTopic(""+userNameTopicInFireBase)
                                                FirebaseMessaging.getInstance().subscribeToTopic("support")
                                                if(findNavControllerSafely()?.currentDestination?.id == R.id.loginFragment){
                                                    findNavControllerSafely()?.navigate(R.id.action_loginFragment_to_manager_mainFragment)
                                                }
                                            }
                                            prefs.user -> {
                                                prefs.save(prefs.email, userName)
                                                prefs.save(prefs.password, password)
                                                val userNameTopicInFireBase = userName.split("@jbnuu.uz").first().toString()
                                                prefs.save(prefs.userNameTopicInFireBase, userNameTopicInFireBase)
                                                FirebaseMessaging.getInstance().subscribeToTopic(""+userNameTopicInFireBase)
                                                if(findNavControllerSafely()?.currentDestination?.id == R.id.loginFragment){
                                                    findNavControllerSafely()?.navigate(R.id.action_loginFragment_to_user_mainFragment)
                                                }
                                            }
                                            prefs.admin -> {
                                                prefs.save(prefs.email, userName)
                                                prefs.save(prefs.password, password)
                                                val userNameTopicInFireBase = userName.split("@jbnuu.uz").first().toString()
                                                prefs.save(prefs.userNameTopicInFireBase, userNameTopicInFireBase)
                                                FirebaseMessaging.getInstance().subscribeToTopic(""+userNameTopicInFireBase)
                                                FirebaseMessaging.getInstance().subscribeToTopic("support")
                                                if(findNavControllerSafely()?.currentDestination?.id == R.id.loginFragment){
                                                    findNavControllerSafely()?.navigate(R.id.action_loginFragment_to_admin_mainFragment)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        snackBar("Ushbu email orqali mazkur ilovadan foydalana olmaysiz.")
                    }
                } else {
                    if (userName.isEmpty() && password.isEmpty()) {
                        snack(requireView(), "Email va parolni kiriting")
                    } else if (userName.isEmpty()) {
                        snack(requireView(), "Emailni kiriting")
                    } else if (password.isEmpty()) {
                        snack(requireView(), "Parolni kiriting")
                    }
                }
            }
            binding.loginRegistration -> {
                hideKeyBoard()
                if (findNavControllerSafely()?.currentDestination?.id == R.id.loginFragment){
                    findNavControllerSafely()?.navigate(R.id.action_loginFragment_to_registrationFragment)
                }
            }
        }
    }

    private fun showLoader() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(binding.root.context, "Iltimos kuting...")
        }
        progressDialog?.show()
    }

    private fun closeLoader() {
        progressDialog?.dismiss()
    }

}