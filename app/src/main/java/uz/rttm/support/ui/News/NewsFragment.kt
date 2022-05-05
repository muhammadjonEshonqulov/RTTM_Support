package uz.rttm.support.ui.News

import android.annotation.SuppressLint
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.rttm.support.R
import uz.rttm.support.adapter.NewsAdapter
import uz.rttm.support.databinding.AllNotificationsFragmentBinding
import uz.rttm.support.models.body.LoginBody
import uz.rttm.support.models.message.MessageResponse
import uz.rttm.support.ui.base.BaseFragment
import uz.rttm.support.ui.base.ProgressDialog
import uz.rttm.support.utils.NetworkResult
import uz.rttm.support.utils.Prefs
import uz.rttm.support.utils.findNavControllerSafely
import javax.inject.Inject

@AndroidEntryPoint
class NewsFragment(val status:Int) : BaseFragment<AllNotificationsFragmentBinding>(AllNotificationsFragmentBinding::inflate), NewsAdapter.OnItemClickListener {

    @Inject
    lateinit var prefs: Prefs
    private val vm: NewsViewModel by viewModels()
    private var progressDialog: ProgressDialog? = null
    private val newsAdapter: NewsAdapter by lazy {
        NewsAdapter(this)
    }

    override fun onResume() {
        super.onResume()
        getMessages()
    }
    override fun onCreate(view: View) {
        binding.backBtn.setOnClickListener {
            finish()
        }
        setupRecycler()
        //getMessages()
        binding.swipeRefreshLayoutTopics.setOnRefreshListener {
            getMessages()
        }
    }

    @SuppressLint("RepeatOnLifecycleWrongUsage")
    fun getMessages() {
        vm.getMessage(status)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.getMessageResponse.collect {
                    when (it) {
                        is NetworkResult.Loading -> {
                             showLoader()
                        }
                        is NetworkResult.Success -> {
                            closeLoader()
                            it.data?.let {
                                if (it.isNotEmpty()) {
                                    binding.listMessages.visibility = View.VISIBLE
                                    binding.notFoundMessage.visibility = View.GONE
                                    newsAdapter.setData(it.sortedBy { it.updated_at})
                                } else {
                                    binding.listMessages.visibility = View.GONE
                                    binding.notFoundMessage.visibility = View.VISIBLE
                                }
                            }
                        }
                        is NetworkResult.Error -> {
                            if (it.code == 401) {
                                login()
                            } else {
                                closeLoader()
                                snackBar(it.message.toString())
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("RepeatOnLifecycleWrongUsage")
    private fun login() {
        vm.login(LoginBody(prefs.get(prefs.email, ""), prefs.get(prefs.password, "")))
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.loginResponse.collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.token?.let {
                                prefs.save(prefs.token, it)
                            }
                            getMessages()
                        }

                        is NetworkResult.Error -> {
                            if (findNavControllerSafely()?.currentDestination?.id == R.id.userMainFragment) {
                                prefs.clear()
                                findNavControllerSafely()?.navigate(R.id.action_userMainFragment_to_all_loginFragment)
                            } else  if (findNavControllerSafely()?.currentDestination?.id == R.id.managerMainFragment) {
                                prefs.clear()
                                findNavControllerSafely()?.navigate(R.id.action_managerMainFragment_to_all_loginFragment)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupRecycler() {
        binding.listMessages.apply {
            val layManager = LinearLayoutManager(binding.root.context)
            layManager.reverseLayout = true
            layManager.stackFromEnd = true
            adapter = newsAdapter
            layoutManager = layManager
        }
    }

    private fun showLoader() {
//        if (progressDialog == null) {
//            progressDialog = ProgressDialog(binding.root.context, "Iltimos kuting ...")
//        }
        binding.swipeRefreshLayoutTopics.isRefreshing = true
//        progressDialog?.show()
    }

    private fun closeLoader() {
        binding.swipeRefreshLayoutTopics.isRefreshing = false
//        progressDialog?.dismiss()
    }

    override fun onItemClick(data: MessageResponse) {

        var bundle = bundleOf(
                "message_status" to status,
                "message_id" to data.id.toString(),
                "data_text" to data.text,
                "chat_count" to data.chat_count,
                "file" to data.img,
                "name" to data.user?.name,
                "fam" to data.user?.fam,
                "phone" to data.user?.phone,
                "photo" to data.user?.photo,
                "lavozim" to data.user?.lavozim,
                "role" to data.user?.role,
                "bolim_name" to data.user?.bolim?.name,
                "user_name" to  if (prefs.get(prefs.role, "") == prefs.user) data.worker?.email?.split("@jbnuu.uz")?.first() else data.user?.email?.split("@jbnuu.uz")?.first(),
                "data_updated_at" to Gson().toJson(data.updated_at)
            )

            if (findNavControllerSafely()?.currentDestination?.id == R.id.userMainFragment) {
                findNavControllerSafely()?.navigate(R.id.action_userMainFragment_to_all_chatFragment, bundle)
            } else if (findNavControllerSafely()?.currentDestination?.id == R.id.managerMainFragment) {
                findNavControllerSafely()?.navigate(R.id.action_managerMainFragment_to_all_chatFragment, bundle)
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding.listMessages.adapter = null
    }
//    viewLifecycleOwner.lifecycleScope.launch {
//        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//            vm.login()
//            vm.loginResponse.collect { response ->
//                when (response) {
//                    is NetworkResult.Success -> {
//                        response.data?.let {
//
//                        }
//                    }
//                    is NetworkResult.Error -> {
//                        showError(response.message.toString())
//
//                    }
//                    is NetworkResult.Loading -> {
//                        showLoader()
//                    }
//                }
//            }
//        }
//    }
}