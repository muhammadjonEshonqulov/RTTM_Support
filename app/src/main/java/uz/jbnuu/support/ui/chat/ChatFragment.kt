package uz.jbnuu.support.ui.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import uz.jbnuu.support.R
import uz.jbnuu.support.adapter.ChatAdapter
import uz.jbnuu.support.databinding.ChatFragmentsBinding
import uz.jbnuu.support.models.body.LoginBody
import uz.jbnuu.support.models.chat.ChatData
import uz.jbnuu.support.models.chat.CreateChatBody
import uz.jbnuu.support.models.chat.CreateChatResponse
import uz.jbnuu.support.models.login.Bolim
import uz.jbnuu.support.models.login.User
import uz.jbnuu.support.models.message.NotificationsData
import uz.jbnuu.support.models.message.PushNotification
import uz.jbnuu.support.ui.base.BaseFragment
import uz.jbnuu.support.ui.base.BottomSheetDialogPhoto
import uz.jbnuu.support.ui.base.CloseAndRatingDialog
import uz.jbnuu.support.ui.base.ProgressDialog
import uz.jbnuu.support.utils.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class ChatFragment : BaseFragment<ChatFragmentsBinding>(ChatFragmentsBinding::inflate),
    View.OnClickListener,
    ChatAdapter.OnItemClickListener {

    @Inject
    lateinit var prefs: Prefs
    lateinit var chatData: ChatData
    var progressDialog: ProgressDialog? = null
    private val vm: ChatViewModel by viewModels()
    private val chatAdapter: ChatAdapter by lazy { ChatAdapter(this) }
    private val chatDataList = ArrayList<ChatData>()

    var data_updated_at = ""
    var data_text = ""
    var name = ""
    var user_name = ""
    var fam = ""
    var role = ""
    var file = ""
    var lavozim = ""
    var phone = ""
    var photo = ""
    var bolim_name = ""
    var message_id = ""

    private var filePhoto: File? = null
    private val PERMISSION_CODE = 1001
    private val IMAGE_CHOOSE = 1000
    private var image_uri = ""
    private val REQUEST_CODE = 13

    override fun onCreate(view: View) {
        if (prefs.get(prefs.role, "") == prefs.user) {
            binding.closeAndRating.visibility = View.VISIBLE
        } else {
            binding.closeAndRating.visibility = View.GONE
        }
        binding.sendChat.setOnClickListener(this)
        binding.backBtn.setOnClickListener(this)
        binding.addChat.setOnClickListener(this)
        binding.closeAndRating.setOnClickListener(this)
        binding.cancelChat.setOnClickListener(this)
        binding.uploadImage.setOnClickListener(this)
        setupRecycle(arguments)
        arguments?.getString("message_id")?.let {
            getChat(it.toInt())
        }
    }

    private fun setupRecycle(arguments: Bundle?) {

        arguments?.apply {
            getString("data_updated_at")?.let {
                data_updated_at = it
            }
            getString("data_text")?.let {
                data_text = it
            }
            getString("file")?.let {
                file = it
            }
            getString("name")?.let {
                name = it
            }
            getString("user_name")?.let {
                user_name = it
            }
            getString("fam")?.let {
                fam = it
            }
            getString("lavozim")?.let {
                lavozim = it
            }
            getString("phone")?.let {
                phone = it
            }
            getString("photo")?.let {
                photo = it
            }
            getString("bolim_name")?.let {
                bolim_name = it
            }
            getString("role")?.let {
                role = it
            }
            getString("message_id")?.let {
                message_id = it
            }
            chatData = ChatData(
                0,
                data_text,
                null,
                0,
                message_id.toInt(),
                null,
                Gson().fromJson(data_updated_at, Date::class.java),
                User(
                    prefs.get(prefs.userId, 0),
                    name,
                    fam,
                    role,
                    user_name + "@jbnuu.uz",
                    phone,
                    photo,
                    lavozim,
                    0,
                    Bolim(0, bolim_name, 0, null, null)
                )
            )
            chatDataList.add(chatData)
        }

        binding.listChat.apply {
            val linearLayoutManager = LinearLayoutManager(binding.root.context)
//            linearLayoutManager.reverseLayout = true
//            linearLayoutManager.stackFromEnd = true
            layoutManager = linearLayoutManager
            adapter = chatAdapter
        }
        chatAdapter.setData(chatDataList)
    }

    private fun getChat(message_id: Int) {
        vm.getChat(message_id)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.getChatResponse.collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            vm.messageActive(message_id)
                            binding.charProgressbar.visibility = View.GONE
                            it.data?.let {
                                chatDataList.clear()
                                chatDataList.add(chatData)
                                chatDataList.addAll(it)
                                chatAdapter.setData(chatDataList)
                                binding.listChat.scrollToPosition(chatAdapter.itemCount - 1)
                            }
                        }
                        is NetworkResult.Loading -> {
                            binding.charProgressbar.visibility = View.VISIBLE

                        }
                        is NetworkResult.Error -> {
                            if (it.code == 401) {
                                login(message_id)
                            } else {
                                binding.charProgressbar.visibility = View.GONE
                                snackBar(it.message.toString())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun login(message_id: Int) {
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
                            getChat(message_id)
                        }

                        is NetworkResult.Error -> {
                            if (findNavControllerSafely()?.currentDestination?.id == R.id.chatFragment) {
                                findNavControllerSafely()?.navigate(R.id.action_chatFragment_to_all_loginFragment)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onItemClick(data: ChatData) {
        var imUrl = Constants.BASE_URL_IMG + data.file
        val dialogPhoto = BottomSheetDialogPhoto(imUrl)
        dialogPhoto.show(parentFragmentManager, "dialog")
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.closeAndRating -> {
                val dialog = CloseAndRatingDialog(binding.root.context)
                dialog.show()
                dialog.setOnCancelClick {
                    dialog.dismiss()
                }
                dialog.setOnSubmitClick {

                }
            }
            binding.sendChat -> {
                hideKeyBoard()
                val imageUri: Uri = Uri.parse(image_uri)
                if (binding.chatMessage.text.toString().isNotEmpty()) {
                    val stringType = "text/plain".toMediaTypeOrNull()

                    val imageFile: File = FileUtils.getFile(requireContext(), imageUri)
                    val image = saveBitmapToFile(imageFile)

                    sendMessage(
                        CreateChatBody(
                            binding.chatMessage.text.toString().toRequestBody(stringType),
                            message_id.toRequestBody(stringType),
                            if (image?.exists() == true) MultipartBody.Part.createFormData(
                                "photo",
                                image.name,
                                RequestBody.create("multipart/form-data".toMediaTypeOrNull(), image)
                            ) else null
                        )
                    )
                } else {
                    snackBar("Bildirishnoma mazmunini kiriting")
                }
            }
            binding.backBtn -> {
                finish()
            }
            binding.uploadImage -> {
                popupCamera(binding.uploadImage)
            }
            binding.cancelChat -> {
                binding.chatMessage.text.clear()
                hideKeyBoard()
                binding.answerLay.visibility = View.GONE
                binding.addChat.setImageResource(R.drawable.ic_baseline_add_circle_24)
            }
            binding.addChat -> {
                if (binding.answerLay.visibility == View.GONE) {
                    binding.chatMessage.showKeyboard()
                    binding.answerLay.visibility = View.VISIBLE
                    binding.addChat.setImageResource(R.drawable.ic_baseline_remove_circle_24)

                } else if (binding.answerLay.visibility == View.VISIBLE) {
                    hideKeyBoard()
                    binding.answerLay.visibility = View.GONE
                    binding.addChat.setImageResource(R.drawable.ic_baseline_add_circle_24)
                }
            }
        }
    }

    private fun sendMessage(body: CreateChatBody) {
        vm.chatCreate(body)
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.chatCreateResponse.collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            sendNotification(
                                it.data,
                                PushNotification(
                                    NotificationsData(
                                        bodyToString(body.message_id),
                                        data_text,
                                        bodyToString(body.text),
                                        file,
                                        Gson().toJson(data_updated_at),
                                        prefs.get(prefs.fam, ""),
                                        fam,
                                        prefs.get(prefs.name, ""),
                                        name,
                                        prefs.get(prefs.lavozim, ""),
                                        prefs.get(prefs.role, ""),
                                        prefs.get(prefs.bolim_name, ""),
                                        bolim_name,
                                        prefs.get(prefs.userNameTopicInFireBase, "")
                                    ), "/topics/" + user_name
                                )
                            )
                        }
                        is NetworkResult.Loading -> {
                            binding.charProgressbar.visibility = View.VISIBLE
                        }
                        is NetworkResult.Error -> {
                            binding.charProgressbar.visibility = View.GONE
                            snackBar(it.message.toString())
//                            }
                        }
                    }
                }
            }
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

    private fun sendNotification(response: CreateChatResponse?, notification: PushNotification) {
        try {
            vm.postNotify(notification) // api(requireContext()).postNotification(notification)
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    vm.notificationResponse.collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                closeLoader()
                                binding.charProgressbar.visibility = View.GONE
                                finish()
                                hideKeyBoard()

                                snackBar("Habaringiz yuborildi.")
                            }
                            is NetworkResult.Error -> {
                                closeLoader()
                                binding.charProgressbar.visibility = View.GONE
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

    private fun showLoader() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(binding.root.context, "Iltimos kuting...")
        }
        progressDialog?.show()
    }

    private fun closeLoader() {
        progressDialog?.dismiss()
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
            selectedBitmap = Bitmap.createBitmap(
                selectedBitmap!!,
                0,
                0,
                selectedBitmap.width,
                selectedBitmap.height,
                matrix,
                true
            )
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
                                "uz.jbnuu.support.fileprovider",
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
            "uz.jbnuu.support.fileprovider",
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
}