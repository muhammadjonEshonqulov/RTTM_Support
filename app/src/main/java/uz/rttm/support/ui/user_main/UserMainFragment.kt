package uz.rttm.support.ui.user_main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import uz.rttm.support.BuildConfig
import uz.rttm.support.R
import uz.rttm.support.databinding.UserMainFragmentBinding
import uz.rttm.support.models.body.CreateMessageBody
import uz.rttm.support.models.body.LoginBody
import uz.rttm.support.models.message.NotificationsData
import uz.rttm.support.models.message.PushNotification
import uz.rttm.support.ui.base.BaseFragment
import uz.rttm.support.ui.base.LogoutDialog
import uz.rttm.support.ui.base.PageAdapter
import uz.rttm.support.ui.base.ProgressDialog
import uz.rttm.support.ui.news.NewsFragment
import uz.rttm.support.utils.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class UserMainFragment : BaseFragment<UserMainFragmentBinding>(UserMainFragmentBinding::inflate),
    ViewPager.OnPageChangeListener, View.OnClickListener {

    @Inject
    lateinit var prefs: Prefs
    private val vm: UserMainViewModel by viewModels()
    private var fragments: ArrayList<NewsFragment>? = null
    lateinit var pageAdapter: PageAdapter
    var progressDialog: ProgressDialog? = null


    private var filePhoto: File? = null
    private val PERMISSION_CODE = 1001
    private val IMAGE_CHOOSE = 1000
    private var image_uri = ""
    private val REQUEST_CODE = 13

    override fun onCreate(view: View) {
        fragmentsToViewPager()
        binding.closed.setOnClickListener(this)
        binding.unClosed.setOnClickListener(this)
        binding.actionBarAnswerBtn.setOnClickListener(this)
        binding.newBtn.setOnClickListener(this)
        binding.mainTopUser.setOnClickListener(this)
        binding.cancelMessageBtn.setOnClickListener(this)
        binding.selectImage.setOnClickListener(this)
        binding.sendMessageBtn.setOnClickListener(this)
        binding.viewPager.addOnPageChangeListener(this)

        val notificationManager = activity?.applicationContext?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!BuildConfig.isDebug)
            notificationManager.cancelAll()
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
                        if (findNavControllerSafely()?.currentDestination?.id == R.id.userMainFragment) {
                            findNavControllerSafely()?.navigate(R.id.action_userMainFragment_to_send_profileFragment)
                        }
                    }
                    R.id.help -> {
                        if (findNavControllerSafely()?.currentDestination?.id == R.id.userMainFragment) {
                            findNavControllerSafely()?.navigate(R.id.action_userMainFragment_to_send_managersFragment)
                        }
                    }
                    R.id.logout -> {
                        val logoutDialog = LogoutDialog(binding.root.context)
                        logoutDialog.show()
                        logoutDialog.setOnSubmitClick {
                            activity?.application?.let {
                                if (hasInternetConnection(it)) {
                                    showLoader()
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic(prefs.get(prefs.userNameTopicInFireBase, "")).addOnSuccessListener {
                                        closeLoader()
                                        prefs.clear()
                                        logoutDialog.dismiss()
                                        if (findNavControllerSafely()?.currentDestination?.id == R.id.userMainFragment) {
                                            findNavControllerSafely()?.navigate(R.id.action_userMainFragment_to_loginFragment)
                                        }
                                    }
                                } else {
                                    snackBar(getString(R.string.connection_error_message))

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
        p0.blockClickable(3000)
        when (p0) {
            binding.closed -> {
                binding.viewPager.setCurrentItem(2, true)
                binding.tabUnderView.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.closed_tab_color))
                binding.ticketsActionbar.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.closed_tab_color))
            }
            binding.selectImage -> {
                popupCamera(binding.selectImage)
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
            binding.actionBarAnswerBtn -> {
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
                binding.chatTitle.text.clear()
                binding.imageName.text = "Fayl nomi"
                hideKeyBoard()
                binding.answerLay.visibility = View.GONE
                binding.addMessage.setImageResource(R.drawable.ic_baseline_add_circle_24)
            }
            binding.sendMessageBtn -> {
                hideKeyBoard()
                if (binding.chatMessage.text.toString().isNotEmpty() && binding.chatTitle.text.toString().isNotEmpty()) {
                    var message: String? = binding.chatMessage.text.toString()
                    var title: String? = binding.chatTitle.text.toString()

                    val stringType = "text/plain".toMediaTypeOrNull()
                    val imageUri: Uri = Uri.parse(image_uri)

                    val imageFile: File = FileUtils.getFile(requireContext(), imageUri)
                    val image: File? = saveBitmapToFile(imageFile)

                    title?.toRequestBody(stringType)?.let {
                        message?.toRequestBody(stringType)?.let { it1 ->
                            CreateMessageBody(
                                it, it1,
                                if (image?.exists() == true) MultipartBody.Part.createFormData("photo", image.name, RequestBody.create("multipart/form-data".toMediaTypeOrNull(), image)) else null
                            )
                        }
                    }?.let {
                        sendMessage(it)
                    }
                    message = null
                    title = null
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
            vm.notificationResponse.collectLatestLA(lifecycleScope) {
                when (it) {
                    is NetworkResult.Success -> {
                        closeLoader()
                        binding.chatTitle.text.clear()
                        binding.chatMessage.text.clear()
                        hideKeyBoard()
                        binding.answerLay.visibility = View.GONE
                        binding.addMessage.setImageResource(R.drawable.ic_baseline_add_circle_24)
                        binding.viewPager.setCurrentItem(0, true)
                        fragments?.get(0)?.getMessages()
                        snackBar("Bildirishnomangiz qabul qilindi. Tez orada sizga xizmat ko'rsatiladi.")
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

    private fun bodyToString(request: RequestBody?): String? {
        return try {
            val buffer = Buffer()
            request?.writeTo(buffer)
            buffer.readUtf8()
        } catch (e: IOException) {
            "did not work"
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

    private fun login(body: CreateMessageBody) {

        vm.login(LoginBody(prefs.get(prefs.email, ""), prefs.get(prefs.password, "")))
        vm.loginResponse.collectLatestLA(lifecycleScope) {
            when (it) {
                is NetworkResult.Success -> {
                    it.data?.token?.let {
                        prefs.save(prefs.token, it)
                    }
                    sendMessage(body)
                }
                is NetworkResult.Error -> {
                    if (findNavControllerSafely()?.currentDestination?.id == R.id.userMainFragment) {
                        findNavControllerSafely()?.navigate(R.id.action_userMainFragment_to_loginFragment)
                    }
                }
                is NetworkResult.Loading ->{}
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


    private fun saveBitmapToFile(file: File?): File? {
        try {
            // BitmapFactory options to downsize the image
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            o.inSampleSize = 6
            // factor of downsizing the image
            var inputStream = FileInputStream(file)
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o)
            inputStream.close()

            // The new size we want to scale to
            val REQUIRED_SIZE = 100

            // Find the correct scale value. It should be the power of 2.
            var scale = 1
            while (o.outHeight / scale / 2 >= REQUIRED_SIZE && o.outWidth / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2
            }
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            inputStream = FileInputStream(file)
            var selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2)
            val matrix = Matrix()
            matrix.postRotate(90F)
            selectedBitmap = Bitmap.createBitmap(selectedBitmap!!, 0, 0, selectedBitmap.width, selectedBitmap.height, matrix, true)
            inputStream.close()


            // here i override the original image file
            file?.createNewFile()
            val outputStream = FileOutputStream(file)

            selectedBitmap!!.compress(Bitmap.CompressFormat.PNG, 50, outputStream)

            return file
        } catch (e: Exception) {
            return file
        }
    }

    @SuppressLint("RestrictedApi", "ResourceType")
    fun popupCamera(view: View) {
        val menuBuilder = MenuBuilder(requireContext())
        val inflater = MenuInflater(requireContext())
        inflater.inflate(R.menu.menu_edit_imag, menuBuilder)
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
                filePhoto = getPhotoFile("image")
                when (item.itemId) {
                    R.id.open_camera -> {
                        if (checkPermission()) {
                            val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            val providerFile = FileProvider.getUriForFile(
                                requireContext(),
                                "uz.rttm.support.fileprovider",
                                filePhoto!!
                            )
                            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)

                            if (activity?.packageManager?.let { takePhotoIntent.resolveActivity(it) } != null) {
                                startActivityForResult(takePhotoIntent, REQUEST_CODE)
                            } else {
                                snackBar("Camera could not open")
                            }
                        } else {
                            requestPermission()
                        }
                    }
                    R.id.open_galeriya -> {

                        if (PermissionChecker.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_DENIED
                        ) {
                            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                            requestPermissions(permissions, PERMISSION_CODE)
                        } else {
                            chooseImageGallery();
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

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.FROYO)
    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = activity?.getExternalFilesDir(
            Environment.DIRECTORY_PICTURES
        )
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    private fun chooseImageGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val providerFile = FileProvider.getUriForFile(
            requireContext(),
            "uz.rttm.support.fileprovider",
            filePhoto!!
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        startActivityForResult(intent, IMAGE_CHOOSE)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                Manifest.permission.CAMERA
            ),
            PERMISSION_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            image_uri = filePhoto?.absolutePath?.toUri().toString()

            binding.imageName.text = filePhoto?.name
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        if (requestCode == IMAGE_CHOOSE && resultCode == Activity.RESULT_OK) {
            image_uri = data?.data.toString()
            binding.imageName.text = filePhoto?.name
//            Glide
//                .with(this)
//                .load(image_uri)
//                .into(binding.image)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager.adapter = null
    }
}