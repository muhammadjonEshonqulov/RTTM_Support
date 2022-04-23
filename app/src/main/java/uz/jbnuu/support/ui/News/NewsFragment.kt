package uz.jbnuu.support.ui.News

import android.annotation.SuppressLint
import android.os.Bundle
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
import uz.jbnuu.support.R
import uz.jbnuu.support.adapter.NewsAdapter
import uz.jbnuu.support.databinding.AllNotificationsFragmentBinding
import uz.jbnuu.support.models.body.LoginBody
import uz.jbnuu.support.models.message.MessageResponse
import uz.jbnuu.support.ui.base.BaseFragment
import uz.jbnuu.support.ui.base.ProgressDialog
import uz.jbnuu.support.utils.NetworkResult
import uz.jbnuu.support.utils.Prefs
import uz.jbnuu.support.utils.findNavControllerSafely
import uz.jbnuu.support.utils.hasInternetConnection
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
    private fun getMessages() {
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
                                    newsAdapter.setData(it)
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
                            if (findNavControllerSafely()?.currentDestination?.id == R.id.allNotificationsFragment) {
                                findNavControllerSafely()?.navigate(R.id.action_allNotificationsFragment_to_all_loginFragment)
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
        activity?.application?.let {
            if (hasInternetConnection(it)){
                if (data.status == 0){
                    data.id?.let {
                        vm.messageActive(it)
                    }
                }
            }
        }

        var bundle = bundleOf(
                "message_id" to data.id.toString(),
                "data_text" to data.text,
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
                findNavControllerSafely()?.navigate(R.id.action_allNotificationsFragment_to_all_chatFragment, bundle)
            } else if (findNavControllerSafely()?.currentDestination?.id == R.id.managerMainFragment) {
                findNavControllerSafely()?.navigate(R.id.action_managerMainFragment_to_all_chatFragment, bundle)
            }
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