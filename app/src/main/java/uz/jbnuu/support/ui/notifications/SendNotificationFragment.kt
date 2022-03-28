package uz.jbnuu.support.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uz.jbnuu.support.NotificationsData
import uz.jbnuu.support.PushNotification
import uz.jbnuu.support.RetrofitInstance
import uz.jbnuu.support.databinding.NotificationFragmentBinding
import uz.jbnuu.support.utils.NetworkResult
import uz.jbnuu.support.utils.Prefs
import uz.jbnuu.support.utils.lg
import uz.jbnuu.support.utils.snack
import uz.jbnuu.support.viewmodels.NotificationViewModel
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class SendNotificationFragment : Fragment() {
    private val ADMIN = "/topics/admins"
    private val USER = "/topics/users"
    private val TAG = "MainActivity"

    @Inject
    lateinit var prefs: Prefs

    private val vm:NotificationViewModel by viewModels()

    lateinit var binding: NotificationFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = NotificationFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            prefs.save(prefs.firebaseToken, it)
            lg("token --> NotificationsFragment -> "+it)
        }
        val role = prefs.get(prefs.role, "")
        if (role == prefs.customer) {
            FirebaseMessaging.getInstance().subscribeToTopic(ADMIN)
            FirebaseMessaging.getInstance().subscribeToTopic(prefs.get(prefs.userLogin, ""))
        } else if (role == prefs.user) {
            FirebaseMessaging.getInstance().subscribeToTopic(prefs.get(prefs.userLogin, ""))
        }
        binding.send.setOnClickListener {

            if (binding.titleEt.text.toString().isNotEmpty() && binding.messageEt.text.toString().isNotEmpty()) {

                val title = binding.titleEt.text.toString()
                val message = binding.messageEt.text.toString()
//                PushNotification(NotificationsData(title, message), ADMIN).also {
//                    sendNotification(it)
//                }
                if (role == prefs.customer) {
                    PushNotification(NotificationsData(title, message), USER).also {
                        sendNotification(it)
                    }
                } else if (role == prefs.user) {
                    PushNotification(NotificationsData(title, message), ADMIN).also {
                        sendNotification(it)
                    }
                }
//                snack(binding.root, "role -> "+role)
//                if (role == prefs.customer) {
//
//                } else if (role == prefs.user) {
//                    PushNotification(NotificationsData(title, message), prefs.get(prefs.firebaseToken, "tokens")).also {
//                        sendNotification(it)
//                    }
//                }

            } else {
                Snackbar.make(it, "input title va message", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
//
                val response = RetrofitInstance.api(requireContext()).postNotification(notification)
                if (response.isSuccessful) {
                    Snackbar.make(binding.root, "Response : ${response}", Snackbar.LENGTH_SHORT).show()
                    lg("Response : ${response}")
                } else {
                    lg("Error ->  : ${response}")
                }
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Error message->  : ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
//        vm.postNotify(notification)
//        vm.notificationResponse.onEach {
////            when(it){
////                is NetworkResult.Loading ->{
////                                snack(requireView(), "Loading ...")
////                            }
////                            is NetworkResult.Error -> {
////                                snack(requireView(), "Error -> "+it)
////                                Log.d("TTT", "Error -> "+it.message)
////
////                            }
////                            is NetworkResult.Success -> {
////                                snack(requireView(), "Response -> "+it)
////                                Log.i("TTT", "Response "+it.data)
////
////                            }
////            }
//
////
//        }.launchIn(viewLifecycleOwner.lifecycleScope)
//    }

}