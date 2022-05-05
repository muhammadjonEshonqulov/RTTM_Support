package uz.rttm.support.ui.admin_main

import android.view.View
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import uz.rttm.support.R
import uz.rttm.support.databinding.AdminMainFragmentBinding
import uz.rttm.support.databinding.ManagerMainFragmentBinding
import uz.rttm.support.ui.base.BaseFragment
import uz.rttm.support.ui.base.LogoutDialog
import uz.rttm.support.ui.base.ProgressDialog
import uz.rttm.support.utils.Prefs
import uz.rttm.support.utils.findNavControllerSafely
import uz.rttm.support.utils.lg
import uz.rttm.support.utils.snack
import javax.inject.Inject

@AndroidEntryPoint
class AdminMainFragment : BaseFragment<AdminMainFragmentBinding>(AdminMainFragmentBinding::inflate), View.OnClickListener {

    @Inject
    lateinit var prefs: Prefs
    var progressDialog: ProgressDialog? = null

    override fun onCreate(view: View) {
        binding.settingsNotification.setOnClickListener(this)
        binding.logout.setOnClickListener(this)
//        binding.historyNotification.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {

            binding.logout -> {
                val dialog =LogoutDialog(binding.root.context)
                dialog.show()
                dialog.setOnSubmitClick {
                    dialog.dismiss()
                }
                dialog.setOnCancelClick {
                    showLoader()
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("support").addOnSuccessListener {
                        closeLoader()
                        prefs.clear()
                        dialog.dismiss()
                        requireActivity().finish()
                    }
                }

            }
            binding.settingsNotification -> {
                snack(binding.root, "Settings")
            }
            binding.historyNotification -> {
                snack(binding.root, "History Notifications")
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