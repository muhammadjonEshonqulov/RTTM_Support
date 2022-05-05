package uz.rttm.support.ui.manager_main

import android.annotation.SuppressLint
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import uz.rttm.support.R
import uz.rttm.support.databinding.ManagerMainFragmentBinding
import uz.rttm.support.ui.News.NewsFragment
import uz.rttm.support.ui.base.BaseFragment
import uz.rttm.support.ui.base.LogoutDialog
import uz.rttm.support.ui.base.PageAdapter
import uz.rttm.support.ui.base.ProgressDialog
import uz.rttm.support.ui.user_main.UserMainViewModel
import uz.rttm.support.utils.Prefs
import uz.rttm.support.utils.blockClickable
import uz.rttm.support.utils.findNavControllerSafely
import uz.rttm.support.utils.hasInternetConnection
import javax.inject.Inject

@AndroidEntryPoint
class ManagerMainFragment :
    BaseFragment<ManagerMainFragmentBinding>(ManagerMainFragmentBinding::inflate),
    ViewPager.OnPageChangeListener, View.OnClickListener {

    @Inject
    lateinit var prefs: Prefs
    private val vm: UserMainViewModel by viewModels()
    private var fragments: ArrayList<NewsFragment>? = null
    lateinit var pageAdapter: PageAdapter
    var progressDialog: ProgressDialog? = null


    override fun onCreate(view: View) {
        fragmentsToViewPager()
        binding.closed.setOnClickListener(this)
        binding.unClosed.setOnClickListener(this)
        binding.newBtn.setOnClickListener(this)
        binding.topManager.setOnClickListener(this)
        binding.viewPager.addOnPageChangeListener(this)
    }

    private fun fragmentsToViewPager() {
        if (fragments == null) {
            fragments = ArrayList()
        }

        fragments?.clear()
        fragments?.add(NewsFragment(0))
        fragments?.add(NewsFragment(1))
        fragments?.add(NewsFragment(2))

        fragments?.let {
            pageAdapter = PageAdapter(childFragmentManager, it)
        }
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.adapter = pageAdapter
        binding.viewPager.setCurrentItem(1, true)
    }

    @SuppressLint("RestrictedApi")
    fun popupLogout(view: View) {
        val menuBuilder = MenuBuilder(requireContext())
        val inflater = MenuInflater(requireContext())
        inflater.inflate(R.menu.menu_main_top, menuBuilder)
        val optionsMenu = MenuPopupHelper(requireContext(), menuBuilder, view)
        optionsMenu.setForceShowIcon(true)
        if (optionsMenu.isShowing) {
            optionsMenu.dismiss()
            view.background
        }
        optionsMenu.show()
        menuBuilder.setCallback(object : MenuBuilder.Callback {
            @SuppressLint("WrongConstant")
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.profile -> {
                        if (findNavControllerSafely()?.currentDestination?.id == R.id.managerMainFragment){
                            findNavControllerSafely()?.navigate(R.id.action_managerMainFragment_to_send_profileFragment)
                        }
                    }
                    R.id.logout -> {
                        val logoutDialog = LogoutDialog(binding.root.context)
                        logoutDialog.show()
                        logoutDialog.setOnSubmitClick {
                            activity?.application?.let {
                                if (hasInternetConnection(it)) {
                                    showLoader()
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic("support").addOnSuccessListener {
                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(prefs.get(prefs.userNameTopicInFireBase, "")).addOnSuccessListener {
                                            closeLoader()
                                            prefs.clear()
                                            logoutDialog.dismiss()
                                            if (findNavControllerSafely()?.currentDestination?.id == R.id.managerMainFragment){
                                                findNavControllerSafely()?.navigate(R.id.action_managerMainFragment_to_loginFragment)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        logoutDialog.setOnCancelClick {
                            logoutDialog.dismiss()
                        }
                    }
                }
                notifyLanguageChanged()
                return true
            }

            override fun onMenuModeChange(menu: MenuBuilder) {
            }
        })
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
    override fun onClick(p0: View?) {
        p0.blockClickable()
        when (p0) {
            binding.closed -> {
                binding.viewPager.setCurrentItem(2, true)
                binding.tabUnderView.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.closed_tab_color
                    )
                )
            }
            binding.unClosed -> {
                binding.viewPager.setCurrentItem(1, true)
                binding.tabUnderView.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.un_closed_tab_color))
                binding.ticketsActionbar.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.un_closed_tab_color))

            }
            binding.newBtn -> {
                binding.viewPager.setCurrentItem(0, true)
                binding.tabUnderView.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.new_tab_color))
                binding.ticketsActionbar.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.new_tab_color))
            }
            binding.topManager->{
                popupLogout(binding.topManager)
            }

        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        when (position) {
            2 -> {
                binding.tabUnderView.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.closed_tab_color))
                binding.ticketsActionbar.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.closed_tab_color))
            }
            1 -> {
                binding.tabUnderView.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.un_closed_tab_color))
                binding.ticketsActionbar.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.un_closed_tab_color))
            }
            0 -> {
                binding.tabUnderView.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.new_tab_color))
                binding.ticketsActionbar.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.new_tab_color))
            }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager.adapter = null
    }
}