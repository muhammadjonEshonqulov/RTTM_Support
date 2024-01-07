package uz.rttm.support.ui.managers

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import uz.rttm.support.adapter.ManagersAdapter
import uz.rttm.support.databinding.FragmentManagersBinding
import uz.rttm.support.ui.base.BaseFragment
import uz.rttm.support.utils.NetworkResult
import uz.rttm.support.utils.collectLatestLA
import uz.rttm.support.utils.lg
import uz.rttm.support.utils.snack


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
                if (checkPermission()) {
                    val callIntent = Intent(Intent.ACTION_CALL)
                    callIntent.data = Uri.parse("tel:${data["phone"]}")
                    startActivity(callIntent)
                } else {
                    requestPermission()
                }
            }
            2 -> {

                val appTelName = "org.telegram.messenger"
                val appPLusName = "org.telegram.plus"
                val appAkaName = "org.aka.messenger"
                val appGraphName = "ir.ilmili.telegraph"
                val isTelegramAppInstalled: Boolean = isAppAvailable(activity?.applicationContext, appTelName)
                val isPlusAppInstalled: Boolean = isAppAvailable(activity?.applicationContext, appPLusName)
                val isAkaAppInstalled: Boolean = isAppAvailable(activity?.applicationContext, appAkaName)
                val isGraphAppInstalled: Boolean = isAppAvailable(activity?.applicationContext, appGraphName)

                if (isTelegramAppInstalled) {
                    val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse("${data["telegram"]}"))
                    myIntent.setPackage(appTelName)
                    startActivity(myIntent)
                } else if (isPlusAppInstalled) {
                    val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse("${data["telegram"]}"))
                    myIntent.setPackage(appPLusName)
                    startActivity(myIntent)
                } else if (isAkaAppInstalled) {
                    val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse("${data["telegram"]}"))
                    myIntent.setPackage(appAkaName)
                    startActivity(myIntent)
                } else if (isGraphAppInstalled) {
                    val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse("${data["telegram"]}"))
                    myIntent.setPackage(appGraphName)
                    startActivity(myIntent)
                } else {
                    snack(binding.root, "Telegram not Installed")
                }
            }
        }
    }


    private fun isAppAvailable(context: Context?, appName: String): Boolean {
        val pm = context?.packageManager
        return try {
            pm?.getPackageInfo(appName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
//            lg("$appName->${e}")
            false
        }
    }

    private val PERMISSION_CODE = 10011

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                Manifest.permission.CALL_PHONE
            ),
            PERMISSION_CODE
        )
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }
}