package uz.rttm.support.ui.login

import android.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import uz.rttm.support.R
import uz.rttm.support.databinding.DialogEmailVerificationBinding
import uz.rttm.support.databinding.DialogEmailVerificationEnterPasswordBinding
import uz.rttm.support.databinding.LoginFragmentBinding
import uz.rttm.support.models.body.LoginBody
import uz.rttm.support.models.repeat.RepeatBody
import uz.rttm.support.ui.base.BaseFragment
import uz.rttm.support.ui.base.ProgressDialog
import uz.rttm.support.utils.*
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : BaseFragment<LoginFragmentBinding>(LoginFragmentBinding::inflate),
    View.OnClickListener {

    @Inject
    lateinit var prefs: Prefs

    private val vm: LoginViewModel by viewModels()

    var progressDialog: ProgressDialog? = null

    override fun onCreate(view: View) {
        binding.loginBtn.setOnClickListener(this)
        binding.loginRegistration.setOnClickListener(this)
        binding.forgetPassword.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.loginBtn -> {
                hideKeyBoard()
                val userName = binding.loginAuth.text.toString()
                val password = binding.passwordAuth.text.toString()
                if (userName.isNotEmpty() && password.isNotEmpty()) {
                    if (userName.endsWith("@jbnuu.uz") && userName.split("@jbnuu.uz").first()
                            .isNotEmpty()
                    ) {
                        vm.login(LoginBody(userName, password))
                        vm.loginResponse.collectLatestLA(lifecycleScope) {
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
                                    when (prefs.get(prefs.role, "")) {
                                        prefs.manager -> {
                                            prefs.save(prefs.email, userName)
                                            prefs.save(prefs.password, password)
                                            val userNameTopicInFireBase =
                                                userName.split("@jbnuu.uz").first()
                                                    .toString()
                                            prefs.save(prefs.userNameTopicInFireBase, userNameTopicInFireBase)
                                            FirebaseMessaging.getInstance().subscribeToTopic("" + userNameTopicInFireBase)
                                            FirebaseMessaging.getInstance().subscribeToTopic("support")
                                            if (findNavControllerSafely()?.currentDestination?.id == R.id.loginFragment) {
                                                findNavControllerSafely()?.navigate(R.id.action_loginFragment_to_manager_mainFragment)
                                            }
                                        }
                                        prefs.user -> {
                                            prefs.save(prefs.email, userName)
                                            prefs.save(prefs.password, password)
                                            val userNameTopicInFireBase =
                                                userName.split("@jbnuu.uz").first()
                                                    .toString()
                                            prefs.save(
                                                prefs.userNameTopicInFireBase,
                                                userNameTopicInFireBase
                                            )
                                            FirebaseMessaging.getInstance().subscribeToTopic("" + userNameTopicInFireBase)
                                            if (findNavControllerSafely()?.currentDestination?.id == R.id.loginFragment) {
                                                hideKeyBoard()
                                                findNavControllerSafely()?.navigate(R.id.action_loginFragment_to_user_mainFragment)
                                            }
                                        }
                                        prefs.admin -> {
                                            prefs.save(prefs.email, userName)
                                            prefs.save(prefs.password, password)
                                            val userNameTopicInFireBase =
                                                userName.split("@jbnuu.uz").first()
                                                    .toString()
                                            prefs.save(
                                                prefs.userNameTopicInFireBase,
                                                userNameTopicInFireBase
                                            )
                                            FirebaseMessaging.getInstance()
                                                .subscribeToTopic("" + userNameTopicInFireBase)
                                            FirebaseMessaging.getInstance()
                                                .subscribeToTopic("support")
                                            if (findNavControllerSafely()?.currentDestination?.id == R.id.loginFragment) {
                                                hideKeyBoard()
                                                findNavControllerSafely()?.navigate(R.id.action_loginFragment_to_admin_mainFragment)
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
                if (findNavControllerSafely()?.currentDestination?.id == R.id.loginFragment) {
                    findNavControllerSafely()?.navigate(R.id.action_loginFragment_to_registrationFragment)
                }
            }
            binding.forgetPassword -> {
                val dialog = AlertDialog.Builder(binding.root.context).create()
                val dialogView = LayoutInflater.from(binding.root.context)
                    .inflate(R.layout.dialog_email_verification, null, false)
                dialog.setView(dialogView)
                dialog.show()
                dialog.setCancelable(false)
                val dialogBinding = DialogEmailVerificationBinding.bind(dialogView)
                dialogBinding.cancelBtn.setOnClickListener {
                    dialog.dismiss()
                }
                dialogBinding.sendBtn.setOnClickListener {
                    hideKeyBoard()
                    val email = dialogBinding.email.text.toString()
                    if (email.isNotEmpty()) {
                        if (email.endsWith("@jbnuu.uz")) {
                            vm.forget(email)
                            vm.forgetResponse.collectLatestLA(lifecycleScope) {
                                when (it) {
                                    is NetworkResult.Error -> {
                                        closeLoader()
                                        snackBar(it.message.toString())
                                    }
                                    is NetworkResult.Loading -> {
                                        showLoader()
                                    }
                                    is NetworkResult.Success -> {
                                        closeLoader()
                                        dialog.dismiss()
                                        if (it.data?.response == "error") {
                                            snackBar("Bu pochta orqali ro'yxatdan o'tilmagan")
                                        } else {

                                            val dialogVerification =
                                                AlertDialog.Builder(binding.root.context)
                                                    .create()
                                            val dialogVerificationView =
                                                LayoutInflater.from(binding.root.context)
                                                    .inflate(
                                                        R.layout.dialog_email_verification_enter_password,
                                                        null,
                                                        false
                                                    )
                                            dialogVerification.setView(
                                                dialogVerificationView
                                            )
                                            dialogVerification.show()
                                            dialogVerification.setCancelable(false)
                                            val dialogVerificationBinding =
                                                DialogEmailVerificationEnterPasswordBinding.bind(
                                                    dialogVerificationView
                                                )
                                            var password = ""
                                            var repassword = ""
                                            dialogVerificationBinding.newPassword.addTextChangedListener(
                                                object : TextWatcher {
                                                    override fun beforeTextChanged(
                                                        s: CharSequence?,
                                                        start: Int,
                                                        count: Int,
                                                        after: Int
                                                    ) {

                                                    }

                                                    override fun onTextChanged(
                                                        s: CharSequence?,
                                                        start: Int,
                                                        before: Int,
                                                        count: Int
                                                    ) {
                                                        password =
                                                            dialogVerificationBinding.newPassword.text.toString()
                                                        if (s!!.length < 6) {
                                                            dialogVerificationBinding.newPasswordMes.visibility =
                                                                View.VISIBLE
                                                            dialogVerificationBinding.newPasswordMes.text =
                                                                "Parol kamida 6ta belgidan iborat bo'lishi kerak"
                                                        } else {
                                                            dialogVerificationBinding.newPasswordMes.visibility =
                                                                View.GONE
                                                        }
                                                    }

                                                    override fun afterTextChanged(s: Editable?) {

                                                    }

                                                })
                                            dialogVerificationBinding.newRePassword.addTextChangedListener(
                                                object : TextWatcher {
                                                    override fun beforeTextChanged(
                                                        s: CharSequence?,
                                                        start: Int,
                                                        count: Int,
                                                        after: Int
                                                    ) {

                                                    }

                                                    override fun onTextChanged(
                                                        s: CharSequence?,
                                                        start: Int,
                                                        before: Int,
                                                        count: Int
                                                    ) {
                                                        repassword =
                                                            dialogVerificationBinding.newRePassword.text.toString()
                                                        if (s!!.length < 6) {
                                                            dialogVerificationBinding.newRePasswordMes.visibility =
                                                                View.VISIBLE
                                                            dialogVerificationBinding.newRePasswordMes.text =
                                                                "Parol kamida 6ta belgidan iborat bo'lishi kerak"
                                                        } else {

                                                            if (password == s.toString()) {
                                                                dialogVerificationBinding.newRePasswordMes.visibility =
                                                                    View.GONE
                                                            } else {
                                                                dialogVerificationBinding.newRePasswordMes.visibility =
                                                                    View.VISIBLE
                                                                dialogVerificationBinding.newRePasswordMes.text =
                                                                    "Parollar mos emas"
                                                            }
                                                        }
                                                    }

                                                    override fun afterTextChanged(s: Editable?) {

                                                    }

                                                })

                                            dialogVerificationBinding.email.setText(email)
                                            dialogVerificationBinding.cancelBtn.setOnClickListener {
                                                dialogVerification.dismiss()
                                            }
                                            dialogVerificationBinding.sendBtn.setOnClickListener {

                                                val verifyCode =
                                                    dialogVerificationBinding.emailVerCode.text.toString()
                                                val newPassword =
                                                    dialogVerificationBinding.newPassword.text.toString()
                                                val newRePassword =
                                                    dialogVerificationBinding.newRePassword.text.toString()
                                                if (verifyCode.isNotEmpty() && newPassword.isNotEmpty() && newRePassword.isNotEmpty()) {
                                                    if (password == repassword) {
                                                        dialogVerificationBinding.newRePasswordMes.visibility = View.GONE

                                                        vm.repeat(RepeatBody(email, newPassword, verifyCode))

                                                        vm.repeatResponse.collectLatestLA(lifecycleScope) {
                                                            when (it) {
                                                                is NetworkResult.Error -> {
                                                                    closeLoader()
                                                                    snackBar(it.message.toString())
                                                                }
                                                                is NetworkResult.Loading -> {
                                                                    showLoader()
                                                                }
                                                                is NetworkResult.Success -> {
                                                                    dialogVerification.dismiss()
                                                                    closeLoader()
                                                                    if (it.data == 1) {
                                                                        snackBar("Parolingiz muvaffaqiyatli o'zgartirildi")
                                                                    } else if (it.data == 0) {
                                                                        snackBar("Emailingizga kelgan parolni noto'g'ri kiritdinging")
                                                                    }
                                                                }
                                                            }

                                                        }
                                                    } else {
                                                        dialogVerificationBinding.newRePasswordMes.visibility =
                                                            View.VISIBLE
                                                    }
                                                } else {
                                                    if (verifyCode.isEmpty()) {
                                                        dialogVerificationBinding.emailVerCodeMes.visibility =
                                                            View.VISIBLE
                                                    } else {
                                                        dialogVerificationBinding.emailVerCodeMes.visibility =
                                                            View.GONE
                                                    }
                                                    if (newPassword.isEmpty()) {
                                                        dialogVerificationBinding.newPasswordMes.visibility =
                                                            View.VISIBLE
                                                    } else {
                                                        dialogVerificationBinding.newPasswordMes.visibility =
                                                            View.GONE
                                                    }
                                                    if (newRePassword.isEmpty()) {
                                                        dialogVerificationBinding.newRePasswordMes.visibility =
                                                            View.VISIBLE
                                                    } else {
                                                        dialogVerificationBinding.newRePasswordMes.visibility =
                                                            View.GONE
                                                    }

                                                }
                                            }

                                        }
                                    }

                                }
                            }

                        } else {
                            dialogBinding.email.error = "@jbnuu.uz formatdagi email kiriting"
                        }
                    } else {
                        dialogBinding.email.error = "Emailingizni kiriting."
                    }

                }


//                vm.forget()
//                viewLifecycleOwner.lifecycleScope.launch {
//                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
//
//                        vm.forgetResponse.collect {
//                            when (it) {
//                                is NetworkResult.Loading -> {
//                                    showLoader()
//                                }
//                                is NetworkResult.Error -> {
//                                    closeLoader()
//                                    snack(requireView(), it.message.toString())
//                                }
//                                is NetworkResult.Success -> {
//                                    closeLoader()
//                                    it.data?.apply {
//                                        token?.let {
//                                            prefs.save(prefs.token, it)
//                                        }
//                                        user?.apply {
//                                            bolim?.apply {
//                                                id?.let {
//                                                    prefs.save(prefs.sub_bolim_id, it)
//                                                }
//                                                bolim_id?.let {
//                                                    prefs.save(prefs.bolim_id, it)
//                                                }
//                                            }
//                                            id?.let {
//                                                prefs.save(prefs.userId, it)
//                                            }
//                                            fam?.let {
//                                                prefs.save(prefs.fam, it)
//                                            }
//                                            phone?.let {
//                                                prefs.save(prefs.phone, it)
//                                            }
//                                            photo?.let {
//                                                prefs.save(prefs.photo, it)
//                                            }
//                                            role?.let {
//                                                prefs.save(prefs.role, it)
//                                            }
//                                            name?.let {
//                                                prefs.save(prefs.name, it)
//                                            }
//                                            lavozim?.let {
//                                                prefs.save(prefs.lavozim, it)
//                                            }
//                                            bolim?.name?.let {
//                                                prefs.save(prefs.bolim_name, it)
//                                            }
//                                        }
//                                    }
//                                    when (prefs.get(prefs.role, "")) {
//                                        prefs.manager -> {
//                                            prefs.save(prefs.email, userName)
//                                            prefs.save(prefs.password, password)
//                                            val userNameTopicInFireBase =
//                                                userName.split("@jbnuu.uz").first().toString()
//                                            prefs.save(
//                                                prefs.userNameTopicInFireBase,
//                                                userNameTopicInFireBase
//                                            )
//                                            FirebaseMessaging.getInstance()
//                                                .subscribeToTopic("" + userNameTopicInFireBase)
//                                            FirebaseMessaging.getInstance()
//                                                .subscribeToTopic("support")
//                                            if (findNavControllerSafely()?.currentDestination?.id == R.id.loginFragment) {
//                                                findNavControllerSafely()?.navigate(R.id.action_loginFragment_to_manager_mainFragment)
//                                            }
//                                        }
//                                        prefs.user -> {
//                                            prefs.save(prefs.email, userName)
//                                            prefs.save(prefs.password, password)
//                                            val userNameTopicInFireBase =
//                                                userName.split("@jbnuu.uz").first().toString()
//                                            prefs.save(
//                                                prefs.userNameTopicInFireBase,
//                                                userNameTopicInFireBase
//                                            )
//                                            FirebaseMessaging.getInstance()
//                                                .subscribeToTopic("" + userNameTopicInFireBase)
//                                            if (findNavControllerSafely()?.currentDestination?.id == R.id.loginFragment) {
//                                                findNavControllerSafely()?.navigate(R.id.action_loginFragment_to_user_mainFragment)
//                                            }
//                                        }
//                                        prefs.admin -> {
//                                            prefs.save(prefs.email, userName)
//                                            prefs.save(prefs.password, password)
//                                            val userNameTopicInFireBase =
//                                                userName.split("@jbnuu.uz").first().toString()
//                                            prefs.save(
//                                                prefs.userNameTopicInFireBase,
//                                                userNameTopicInFireBase
//                                            )
//                                            FirebaseMessaging.getInstance()
//                                                .subscribeToTopic("" + userNameTopicInFireBase)
//                                            FirebaseMessaging.getInstance()
//                                                .subscribeToTopic("support")
//                                            if (findNavControllerSafely()?.currentDestination?.id == R.id.loginFragment) {
//                                                findNavControllerSafely()?.navigate(R.id.action_loginFragment_to_admin_mainFragment)
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
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