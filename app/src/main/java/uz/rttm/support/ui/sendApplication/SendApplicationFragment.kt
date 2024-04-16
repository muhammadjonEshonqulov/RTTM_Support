package uz.rttm.support.ui.sendApplication

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import uz.rttm.support.R
import uz.rttm.support.adapter.AdditionalAdapter
import uz.rttm.support.databinding.SendApplicationBinding
import uz.rttm.support.models.body.CreateMessageBody
import uz.rttm.support.models.body.LoginBody
import uz.rttm.support.models.message.NotificationsData
import uz.rttm.support.models.message.PushNotification
import uz.rttm.support.ui.base.BaseFragment
import uz.rttm.support.ui.base.ProgressDialog
import uz.rttm.support.utils.NetworkResult
import uz.rttm.support.utils.Prefs
import uz.rttm.support.utils.collectLA
import uz.rttm.support.utils.collectLatestLA
import uz.rttm.support.utils.findNavControllerSafely
import uz.rttm.support.utils.getNowDate
import uz.rttm.support.utils.lg
import javax.inject.Inject

@AndroidEntryPoint
class SendApplicationFragment : BaseFragment<SendApplicationBinding>(SendApplicationBinding::inflate) {
    private val vm: SendMessageViewModel by viewModels()

    @Inject
    lateinit var prefs: Prefs
    var progressDialog: ProgressDialog? = null

    override fun onCreate(view: View) {
        arguments?.getString("MyKeyTech")?.let { arg ->
            try {
                val myKeyTech = arg.split("#")
                binding.textPaste.text = myKeyTech[1]
                binding.stateOfTech.text = myKeyTech[2]
                binding.building.text = myKeyTech[3]
                binding.room.text = myKeyTech[4]
                lg("additional -> 12 ${myKeyTech[5]}")
                val data = parseJsonToList(myKeyTech[5])
                lg("additional ->${data.size} -> ${data}")

                val adapter = AdditionalAdapter(object : AdditionalAdapter.OnItemClickListener {
                    override fun onItemClick(data: TechItem, type: Int) {

                    }
                })
                binding.listAdditional.adapter = adapter
                binding.listAdditional.layoutManager = LinearLayoutManager(binding.room.context)
                adapter.setData(data)

                binding.cancelChat.setOnClickListener {
                    finish()
                }
                binding.sendChat.setOnClickListener { _ ->
                    val stringType = "text/plain".toMediaTypeOrNull()

                    "Qo'shimcha qurilmalar ${getNowDate()}".toRequestBody(stringType).let {
                        CreateMessageBody(
                            it, (arg + "#" + binding.chatMessage.text.toString()).toRequestBody(stringType),
                            null
                        )
                    }.let {
                        showLoader()
                        sendMessage(it)
                    }
                }
            } catch (e: Exception) {
                lg("additional error -> ${e.message}")
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

    private fun sendNotification(notification: PushNotification) {
        try {
            vm.postNotify(notification)
            vm.notificationResponse.collectLatestLA(lifecycleScope) {
                when (it) {
                    is NetworkResult.Success -> {
                        closeLoader()
                        binding.chatMessage.text.clear()
                        hideKeyBoard()
                        binding.answerLay.visibility = View.GONE
                        snackBar("Bildirishnomangiz qabul qilindi.")
                        finish()
                    }

                    is NetworkResult.Error -> {
                        closeLoader()
                        snackBar(it.message.toString())
                    }

                    is NetworkResult.Loading -> {
                        showLoader()
                    }
                }

            }
        } catch (e: Exception) {
            snackBar("Error message->  : ${e.message}")
        }
    }

    private fun sendMessage(body: CreateMessageBody) {
        vm.sendMessage(body)
        vm.sendMessageResponse.collectLatestLA(lifecycleScope) {
            when (it) {
                is NetworkResult.Success -> {
                    sendNotification(
                        PushNotification(
                            NotificationsData(
                                it.data?.id.toString(),
                                it.data?.text,
                                it.data?.title,
                                it.data?.img,
                                it.data?.updated_at,
                                prefs.get(prefs.fam, ""),
                                prefs.get(prefs.fam, ""),
                                prefs.get(prefs.name, ""),
                                prefs.get(prefs.name, ""),
                                prefs.get(prefs.lavozim, ""),
                                prefs.get(prefs.role, ""),
                                prefs.get(prefs.phone, ""),
                                prefs.get(prefs.bolim_name, ""),
                                prefs.get(prefs.bolim_name, ""),
                                prefs.get(prefs.userNameTopicInFireBase, ""),
                                code = 101 // 101 code bu qabul qildim tugmasini chiqarish uchun kerak.
                            ), "/topics/support"
                        )
                    )
                }

                is NetworkResult.Loading -> {
                    showLoader()
                }

                is NetworkResult.Error -> {
                    if (it.code == 401) {
                        login(body)
                    } else {
                        closeLoader()
                        snackBar(it.message.toString())
                    }
                }

            }
        }
    }

    private fun login(body: CreateMessageBody? = null) {
        vm.login(LoginBody(prefs.get(prefs.email, ""), prefs.get(prefs.password, "")))
        vm.loginResponse.collectLA(lifecycleScope) {
            when (it) {
                is NetworkResult.Success -> {
                    it.data?.token?.let {
                        prefs.save(prefs.token, it)
                    }
                    body?.let {
                        sendMessage(it)
                    }
                }

                is NetworkResult.Error -> {
                    if (findNavControllerSafely()?.currentDestination?.id == R.id.chatFragment) {
                        findNavControllerSafely()?.navigate(R.id.action_chatFragment_to_all_loginFragment)
                    }
                }

                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun parseJsonToList(jsonString: String): List<TechItem> {

        val gson = Gson()
        val listType = object : TypeToken<List<TechItem>>() {}.type

        return gson.fromJson(jsonString, listType)
//        val jsonArray = JSONArray(jsonString)
//        val techItems = mutableListOf<TechItem>()
//
//        for (i in 0 until jsonArray.length()) {
//            val jsonObject = jsonArray.getJSONObject(i)
//            val techItem = TechItem(
//                _id = jsonObject.getString("_id"),
//                tip = TechItem.Tip(
//                    _id = jsonObject.getJSONObject("tip").getString("_id"),
//                    text = jsonObject.getJSONObject("tip").getString("text"),
//                    __v = jsonObject.getJSONObject("tip").getInt("__v")
//                ),
//                condition = jsonObject.getInt("condition"),
//                status = jsonObject.getInt("status")
//            )
//            techItems.add(techItem)
//        }
//
//        return techItems
    }
}

data class TechItem(
    val _id: String,
    val tip: Tip,
    val condition: Int,
    val status: Int
) {
    data class Tip(
        val _id: String,
        val text: String,
        val __v: Int
    )
}
