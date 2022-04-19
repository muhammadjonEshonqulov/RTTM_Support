package uz.jbnuu.support.ui.user_main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.provider.MediaStore
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager.widget.ViewPager
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.jbnuu.support.R
import uz.jbnuu.support.databinding.UserMainFragmentBinding
import uz.jbnuu.support.models.body.CreateMessageBody
import uz.jbnuu.support.models.body.LoginBody
import uz.jbnuu.support.models.message.NotificationsData
import uz.jbnuu.support.models.message.PushNotification
import uz.jbnuu.support.ui.News.NewsFragment
import uz.jbnuu.support.ui.base.*
import uz.jbnuu.support.utils.NetworkResult
import uz.jbnuu.support.utils.Prefs
import uz.jbnuu.support.utils.findNavControllerSafely
import uz.jbnuu.support.utils.lg
import javax.inject.Inject
import kotlin.math.log

@AndroidEntryPoint
class UserMainFragment : BaseFragment<UserMainFragmentBinding>(UserMainFragmentBinding::inflate),
    ViewPager.OnPageChangeListener, View.OnClickListener {

    @Inject
    lateinit var prefs: Prefs
    private val vm: UserMainViewModel by viewModels()
    private var fragments: ArrayList<Fragment>? = null
    lateinit var pageAdapter: PageAdapter
    var progressDialog: ProgressDialog? = null


    override fun onCreate(view: View) {
        fragmentsToViewPager()
        binding.closed.setOnClickListener(this)
        binding.unClosed.setOnClickListener(this)
        binding.addMessage.setOnClickListener(this)
        binding.newBtn.setOnClickListener(this)
        binding.mainTopUser.setOnClickListener(this)
        binding.cancelMessageBtn.setOnClickListener(this)
        binding.sendMessageBtn.setOnClickListener(this)
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
                        if (findNavControllerSafely()?.currentDestination?.id == R.id.userMainFragment){
                            findNavControllerSafely()?.navigate(R.id.action_userMainFragment_to_send_profileFragment)
                        }
                    }
                    R.id.logout -> {
                        val logoutDialog = LogoutDialog(binding.root.context)
                        logoutDialog.show()
                        logoutDialog.setOnSubmitClick {
                            showLoader()
                            FirebaseMessaging.getInstance().unsubscribeFromTopic(prefs.get(prefs.userNameTopicInFireBase, "")).addOnSuccessListener {
                                closeLoader()
                                prefs.clear()
                                logoutDialog.dismiss()
                                if (findNavControllerSafely()?.currentDestination?.id == R.id.userMainFragment){
                                    findNavControllerSafely()?.navigate(R.id.action_userMainFragment_to_loginFragment)
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

        override fun onClick(p0: View?) {
            when (p0) {
                binding.closed -> {
                    binding.viewPager.setCurrentItem(2, true)
                    binding.tabUnderView.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.closed_tab_color))
                    binding.ticketsActionbar.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.closed_tab_color))
                }
                binding.unClosed -> {
                    binding.viewPager.setCurrentItem(1, true)
                    binding.tabUnderView.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.un_closed_tab_color))
                    binding.ticketsActionbar.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.un_closed_tab_color))

                }
                binding.mainTopUser -> {
                    popupLogout(binding.mainTopUser)
                }
                binding.newBtn -> {
                    binding.viewPager.setCurrentItem(0, true)
                    binding.tabUnderView.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.new_tab_color))
                    binding.ticketsActionbar.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.new_tab_color))
                }
                binding.addMessage -> {
                    if (binding.answerLay.visibility == View.GONE) {
                        binding.chatTitle.showKeyboard()
                        binding.answerLay.visibility = View.VISIBLE
                        binding.addMessage.setImageResource(R.drawable.ic_baseline_remove_circle_24)

                    } else if (binding.answerLay.visibility == View.VISIBLE) {
                        hideKeyBoard()
                        binding.answerLay.visibility = View.GONE
                        binding.addMessage.setImageResource(R.drawable.ic_baseline_add_circle_24)
                    }
                }
                binding.cancelMessageBtn -> {
                    binding.chatMessage.text.clear()
                    hideKeyBoard()
                    binding.answerLay.visibility = View.GONE
                    binding.addMessage.setImageResource(R.drawable.ic_baseline_add_circle_24)
                }
                binding.sendMessageBtn -> {
                    hideKeyBoard()
                    if (binding.chatMessage.text.toString()
                            .isNotEmpty() && binding.chatTitle.text.toString().isNotEmpty()
                    ) {
                        val message = binding.chatMessage.text.toString()
                        val title = binding.chatTitle.text.toString()
                        sendMessage(title, message)
                    } else {
                        if (binding.chatTitle.text.toString().isEmpty()) {
                            snackBar("Bildirishnoma sarlavhasini kiriting")
                        } else if (binding.chatMessage.text.toString().isEmpty()) {
                            snackBar("Bildirishnoma mazmunini kiriting")
                        }
                    }
                }
            }
        }

    private fun sendNotification(notification: PushNotification) {
        try {
            vm.postNotify(notification) // api(requireContext()).postNotification(notification)
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    vm.notificationResponse.collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                closeLoader()
                                binding.chatTitle.text.clear()
                                binding.chatMessage.text.clear()
                                hideKeyBoard()
                                binding.answerLay.visibility = View.GONE
                                binding.addMessage.setImageResource(R.drawable.ic_baseline_add_circle_24)
                                pageAdapter.notifyDataSetChanged()
                                snackBar("Arizangiz qabul qilindi. Tez orada sizga xizmat ko'rsatiladi.")
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
                }
            }
        } catch (e: Exception) {
            snackBar("Error message->  : ${e.message}")
        }
    }

    private fun sendMessage(title: String, message: String) {
        vm.sendMessage(CreateMessageBody(title, message))
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.sendMessageResponse.collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            sendNotification(
                                PushNotification(
                                    NotificationsData(
                                        it.data?.id.toString(),
                                        message,
                                        message,
                                        file = null,
                                        Gson().toJson(it.data?.updated_at),
                                        prefs.get(prefs.fam, ""),
                                        prefs.get(prefs.fam, ""),
                                        prefs.get(prefs.name, ""),
                                        prefs.get(prefs.name, ""),
                                        prefs.get(prefs.lavozim, ""),
                                        prefs.get(prefs.role, ""),
                                        prefs.get(prefs.bolim_name, ""),
                                        prefs.get(prefs.bolim_name, ""),
                                        prefs.get(prefs.userNameTopicInFireBase, "")
                                    ), "/topics/support"
                                )
                            )
                        }
                        is NetworkResult.Loading -> {
                            showLoader()
                        }
                        is NetworkResult.Error -> {
                            if (it.code == 401) {
                                login(title, message)
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

    private fun login(title: String, message: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            prefs.save(prefs.password, "a")
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.login(LoginBody(prefs.get(prefs.email, ""), prefs.get(prefs.password, "")))
                vm.loginResponse.collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            it.data?.token?.let {
                                prefs.save(prefs.token, it)
                            }
                            sendMessage(title, message)
                        }

                        is NetworkResult.Error -> {
                            if (findNavControllerSafely()?.currentDestination?.id == R.id.userMainFragment) {
                                findNavControllerSafely()?.navigate(R.id.action_userMainFragment_to_loginFragment)
                            }
                        }
                    }
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

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        when (position) {
            2 -> {
                binding.tabUnderView.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.closed_tab_color))
                binding.ticketsActionbar.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.closed_tab_color))
            }
            1 -> {
                binding.tabUnderView.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.un_closed_tab_color))
                binding.ticketsActionbar.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.un_closed_tab_color))
            }
            0 -> {
                binding.tabUnderView.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.new_tab_color))
                binding.ticketsActionbar.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.new_tab_color))
            }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {

    }
}