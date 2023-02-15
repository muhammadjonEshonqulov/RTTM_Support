package uz.rttm.support.ui.managers

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import uz.rttm.support.adapter.ManagersAdapter
import uz.rttm.support.databinding.FragmentManagersBinding
import uz.rttm.support.ui.base.BaseFragment
import uz.rttm.support.utils.NetworkResult
import uz.rttm.support.utils.collectLatestLA


@AndroidEntryPoint
class ManagersFragment : BaseFragment<FragmentManagersBinding>(FragmentManagersBinding::inflate), ManagersAdapter.OnItemClickListener {

    private val managersAdapter: ManagersAdapter by lazy { ManagersAdapter(this) }
    private val vm: ManagersViewModel by viewModels()

    override fun onCreate(view: View) {
        getMessages()
        setupRecycler()

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            getMessages()
        }
    }

    private fun setupRecycler() {
        binding.listManagers.apply {
            adapter = managersAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    private fun getMessages() {
        vm.getManagers()
        vm.managers.collectLatestLA(lifecycleScope) {
            when (it) {
                is NetworkResult.Loading -> {
                    showLoader()
                }
                is NetworkResult.Success -> {
                    closeLoader()
                    it.data?.let {
                        if (it.isNotEmpty()) {
                            binding.listManagers.visibility = View.VISIBLE
                            binding.notFound.visibility = View.GONE
                            managersAdapter.setData(it)
                        } else {
                            binding.listManagers.visibility = View.GONE
                            binding.notFound.visibility = View.VISIBLE
                        }
                    }
                }
                is NetworkResult.Error -> {
                    closeLoader()
                    snackBar(it.message.toString())

                }
            }

        }
    }

    private fun showLoader() {
        binding.swipeRefreshLayout.isRefreshing = true
    }

    private fun closeLoader() {
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onItemClick(data: Map<String, String>, type: Int) {
        when (type) {
            1 -> {
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:${data["phone"]}")
                startActivity(callIntent)
            }
            2 -> {
                val tgintent = Intent(Intent.ACTION_VIEW, Uri.parse("${data["telegram"]}"))
                tgintent.setPackage("org.telegram.messenger")
                startActivity(tgintent)
            }
        }
    }
}