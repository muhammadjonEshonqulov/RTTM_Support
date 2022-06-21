package uz.rttm.support.ui.profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import uz.rttm.support.R
import uz.rttm.support.databinding.ProfileFragmentBinding
import uz.rttm.support.models.body.UpdateBody
import uz.rttm.support.ui.base.BaseFragment
import uz.rttm.support.ui.base.ProgressDialog
import uz.rttm.support.utils.*
import uz.rttm.support.utils.Constants.Companion.BASE_URL_IMG
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseFragment<ProfileFragmentBinding>(ProfileFragmentBinding::inflate), View.OnClickListener {

    @Inject
    lateinit var pref: Prefs
    private val vm: ProfileViewModel by viewModels()

    private var organization_id = -1
    private var organization_sub_id = -1
    private var progressDialog: ProgressDialog? = null

    private var filePhoto: File? = null
    private val PERMISSION_CODE = 1001
    private val IMAGE_CHOOSE = 1000
    private var image_uri = ""
    private val REQUEST_CODE = 13
    private var isPasswordChange = false

    override fun onCreate(view: View) {
        pref.get(pref.photo, "").let {
            Glide
                .with(binding.root)
                .load(BASE_URL_IMG + it)
        }
        binding.position.setText(pref.get(pref.lavozim, ""))
        binding.surname.setText(pref.get(pref.fam, ""))
        binding.name.setText(pref.get(pref.name, ""))
        binding.fullNameOfUser.setText(pref.get(pref.name, "")+" "+pref.get(pref.fam, ""))
        binding.phone.setText(pref.get(pref.phone, ""))
        binding.organizationUser.setText(pref.get(pref.bolim_name, ""))

        binding.imgUser.setOnClickListener(this)
        binding.backBtnProfile.setOnClickListener(this)
        binding.send.setOnClickListener(this)
        binding.changePasswordToggle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.passwordChangeLay.visibility = View.VISIBLE
                isPasswordChange = isChecked
            } else {
                binding.passwordChangeLay.visibility = View.GONE
                isPasswordChange = isChecked
            }
        }
        binding.currentPasswordRegistrationShow.setOnClickListener {
            if (binding.currentPasswordRegistration.transformationMethod == PasswordTransformationMethod.getInstance()) {
                binding.currentPasswordRegistrationShow.setImageResource(R.drawable.ic_eye)
                binding.currentPasswordRegistration.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                binding.currentPasswordRegistrationShow.setImageResource(R.drawable.ic_eyeslash)
                binding.currentPasswordRegistration.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            }
            binding.currentPasswordRegistration.setSelection(binding.currentPasswordRegistration.length())
        }
        binding.newPasswordRegistrationShow.setOnClickListener {
            if (binding.newPasswordRegistration.transformationMethod == PasswordTransformationMethod.getInstance()) {
                binding.newPasswordRegistrationShow.setImageResource(R.drawable.ic_eye)
                binding.newPasswordRegistration.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                binding.newPasswordRegistrationShow.setImageResource(R.drawable.ic_eyeslash)
                binding.newPasswordRegistration.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            }
            binding.newPasswordRegistration.setSelection(binding.newPasswordRegistration.length())
        }
        binding.rePasswordRegistrationShow.setOnClickListener {
            if (binding.rePasswordRegistration.transformationMethod == PasswordTransformationMethod.getInstance()) {
                binding.rePasswordRegistrationShow.setImageResource(R.drawable.ic_eye)
                binding.rePasswordRegistration.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                binding.rePasswordRegistrationShow.setImageResource(R.drawable.ic_eyeslash)
                binding.rePasswordRegistration.transformationMethod =
                    PasswordTransformationMethod.getInstance()
            }
            binding.rePasswordRegistration.setSelection(binding.rePasswordRegistration.length())
        }


        var arraySpinner = arrayOf("tanlang", "Tarkibiy bo'linma", "Fakultet", "Kafedra")
        val organizationAdapter = ArrayAdapter(binding.root.context, R.layout.simple_spinner_item, arraySpinner)
        organizationAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerOrganization.adapter = organizationAdapter
        binding.spinnerOrganization.setSelection(pref.get(pref.bolim_id, 0))
        binding.spinnerOrganization.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                organization_id = p2
                if (p2 > 0) {
                    binding.spinnerOrganizationMes.visibility = View.GONE
                }
                getBolim(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}

        }
    }

    private fun getBolim(id: Int) {
        val sub_bolim_id = pref.get(pref.sub_bolim_id, 0)

        vm.bolim(id)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.bolimResponse.collect {
                    var arraySpinner = ArrayList<String>()
                    arraySpinner.clear()
                    arraySpinner.add("tanlang")
                    when (it) {
                        is NetworkResult.Success -> {

                            it.data?.let {
                                it.forEachIndexed { index, bolim ->
                                    arraySpinner.add(bolim.name.toString())
                                }
                            }
                        }
                    }
                    val organizationAdapter = ArrayAdapter(binding.root.context, R.layout.simple_spinner_item, arraySpinner)
                    organizationAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                    binding.spinnerOrganizationName.adapter = organizationAdapter
                    delay(100)

                    if (sub_bolim_id < arraySpinner.size) {
                        binding.spinnerOrganizationName.setSelection(sub_bolim_id)
                    }
                    binding.spinnerOrganizationName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                            organization_sub_id = p2
                            pref.save(pref.sub_bolim_id, organization_sub_id)
                            pref.save(pref.bolim_name, arraySpinner.get(p2))
                            lg("name -<>"+arraySpinner.get(p2))
                            if (p2 > 0) {
                                binding.spinnerOrganizationNameMes.visibility = View.GONE
                            }
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {}

                    }
                }
            }
        }
    }

    override fun onClick(p0: View?) {
        p0.blockClickable()
        when (p0) {
            binding.backBtnProfile -> {
                hideKeyBoard()
                finish()
            }
            binding.imgUser -> {
                popupCamera(binding.imgUser)
            }
            binding.send -> {
                hideKeyBoard()
                if (isPasswordChange) {
                    if (organization_id > 0 && organization_sub_id > 0 && binding.currentPasswordRegistration.text.toString().isNotEmpty() && binding.surname.text.toString()
                            .isNotEmpty() && binding.name.text.toString().isNotEmpty() && binding.position.text.toString().isNotEmpty() && binding.phone.text.toString()
                            .isNotEmpty() && binding.newPasswordRegistration.text.toString().isNotEmpty() && binding.rePasswordRegistration.text.toString().isNotEmpty()
                    ) {
                        binding.currentPasswordRegistrationMes.visibility = View.GONE
                        if (binding.newPasswordRegistration.text.toString().length >= 6 && binding.rePasswordRegistration.text.toString().length >= 6 && binding.newPasswordRegistration.text.toString() == binding.rePasswordRegistration.text.toString()) {
                            binding.passwordRegistrationMes.visibility = View.GONE
                            binding.rePasswordRegistrationMes.visibility = View.GONE
                            val stringType = "text/plain".toMediaTypeOrNull()
                            val imageTypee = "image/JPEG".toMediaTypeOrNull()
                            val imageUri: Uri = Uri.parse(image_uri)
                            val imageFile: File = FileUtils.getFile(requireContext(), imageUri)
                            val image = saveBitmapToFile(imageFile)
                            vm.update(
                                UpdateBody(
                                    binding.name.text.toString().toRequestBody(stringType),
                                    "...".toRequestBody(stringType),
                                    binding.surname.text.toString().toRequestBody(stringType),
                                    binding.phone.text.toString().toRequestBody(stringType),
                                    binding.position.text.toString().toRequestBody(stringType),
                                    organization_sub_id.toString().toRequestBody(stringType),
                                    binding.currentPasswordRegistration.text.toString().toRequestBody(stringType),
                                    binding.newPasswordRegistration.text.toString().toRequestBody(stringType),
                                    if (image?.exists() == true) MultipartBody.Part.createFormData("photo", "", image.readBytes().toRequestBody(imageTypee)) else null
                                )
                            )
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    vm.updateResponse.collect {
                                        when (it) {
                                            is NetworkResult.Success -> {
                                                progressDialog?.dismiss()
                                                if (it.data?.succes == "ok") {
                                                    snackBar("Shaxsiy malumotlaringiz muvaffaqiyatli o'zgartirildi.")
                                                    finish()
                                                }
                                            }
                                            is NetworkResult.Error -> {

                                                progressDialog?.dismiss()
                                                if (it.code == 422) {
                                                    snackBar("Eski parol noto'g'ri kiritildi")
                                                } else {
                                                    snackBar(it.message.toString())
                                                }
                                            }
                                            is NetworkResult.Loading -> {
                                                if (progressDialog == null) {
                                                    progressDialog = ProgressDialog(binding.root.context, "Yuklanmoqda")
                                                }
                                                progressDialog?.show()
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (binding.newPasswordRegistration.text.toString().length < 6) {
                                binding.passwordRegistrationMes.visibility = View.VISIBLE
                                binding.passwordRegistrationMes.text = "Parol kamida 6ta belgidan iborat bo'lishi kerak"
                            } else {
                                binding.passwordRegistrationMes.visibility = View.GONE
                            }
                            if (binding.rePasswordRegistration.text.toString().length < 6) {
                                binding.rePasswordRegistrationMes.visibility = View.VISIBLE
                                binding.rePasswordRegistrationMes.text = "Parol kamida 6ta belgidan iborat bo'lishi kerak"
                            } else {
                                binding.rePasswordRegistrationMes.visibility = View.GONE
                            }
                            if (binding.newPasswordRegistration.text.toString() != binding.rePasswordRegistration.text.toString()) {
                                binding.rePasswordRegistrationMes.visibility = View.VISIBLE
                                binding.rePasswordRegistrationMes.text = "Parollar mos emas"
                            } else {
                                binding.rePasswordRegistrationMes.visibility = View.GONE
                            }
                        }
                    } else {
                        validator()
                    }
                } else {
                    if (organization_id > 0 && organization_sub_id > 0 &&  binding.surname.text.toString()
                            .isNotEmpty() && binding.name.text.toString().isNotEmpty() && binding.position.text.toString().isNotEmpty() && binding.phone.text.toString()
                            .isNotEmpty()
                    ) {
                        binding.currentPasswordRegistrationMes.visibility = View.GONE
                            binding.passwordRegistrationMes.visibility = View.GONE
                            binding.rePasswordRegistrationMes.visibility = View.GONE
                            val stringType = "text/plain".toMediaTypeOrNull()
                            val imageTypee = "image/JPEG".toMediaTypeOrNull()
                            val imageUri: Uri = Uri.parse(image_uri)
                            val imageFile: File = FileUtils.getFile(requireContext(), imageUri)
                            val image = saveBitmapToFile(imageFile)
                            vm.update(
                                UpdateBody(
                                    binding.name.text.toString().toRequestBody(stringType),
                                    "...".toRequestBody(stringType),
                                    binding.surname.text.toString().toRequestBody(stringType),
                                    binding.phone.text.toString().toRequestBody(stringType),
                                    binding.position.text.toString().toRequestBody(stringType),
                                    organization_sub_id.toString().toRequestBody(stringType),
                                    null,
                                    null,
                                    if (image?.exists() == true) MultipartBody.Part.createFormData("photo", "", image.readBytes().toRequestBody(imageTypee)) else null
                                )
                            )
                            viewLifecycleOwner.lifecycleScope.launch {
                                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                                    vm.updateResponse.collect {
                                        when (it) {
                                            is NetworkResult.Success -> {
                                                progressDialog?.dismiss()
                                                if (it.data?.succes == "ok") {
                                                    pref.save(pref.lavozim, binding.position.text.toString())
                                                    pref.save(pref.fam, binding.surname.text.toString())
                                                    pref.save(pref.name,binding.name.text.toString() )
                                                    pref.save(pref.phone, binding.phone.text.toString())
                                                    pref.save(pref.bolim_id, organization_id)
                                                    snackBar("Shaxsiy malumotlaringiz muvaffaqiyatli o'zgartirildi.")
                                                    finish()
                                                }
                                            }
                                            is NetworkResult.Error -> {

                                                progressDialog?.dismiss()
                                                if (it.code == 422) {
                                                    snackBar("Eski parol noto'g'ri kiritildi")
                                                } else {
                                                    snackBar(it.message.toString())
                                                }
                                            }
                                            is NetworkResult.Loading -> {
                                                if (progressDialog == null) {
                                                    progressDialog = ProgressDialog(binding.root.context, "Yuklanmoqda")
                                                }
                                                progressDialog?.show()
                                            }
                                        }
                                    }
                                }
                            }
                    } else {
                        validator()
                    }
                }

            }
        }
    }

    private fun checkAll(){

    }

    private fun validator(){
        if (binding.name.text.toString().isEmpty()) {
            binding.name.error = "Ismingizni kiriting"
        }
        if (binding.surname.text.toString().isEmpty()) {
            binding.surname.error = "Familiyangizni kiriting"
        }
        if (binding.phone.text.toString().isEmpty()) {
            binding.phone.error = "Telefoningizni kiriting"
        }
        if (binding.position.text.toString().isEmpty()) {
            binding.position.error = "lavozimingizni kiriting"
        }

        if (binding.newPasswordRegistration.text.toString().isEmpty()) {
            binding.passwordRegistrationMes.visibility = View.VISIBLE
            binding.passwordRegistrationMes.text = "Parolni kiriting"
        } else {
            binding.passwordRegistrationMes.visibility = View.GONE
        }
        if (binding.currentPasswordRegistration.text.toString().isEmpty()) {
            binding.currentPasswordRegistrationMes.visibility = View.VISIBLE
            binding.currentPasswordRegistrationMes.text = "Joriy parolni kiriting"
        } else {
            binding.currentPasswordRegistrationMes.visibility = View.GONE
        }
        if (binding.rePasswordRegistration.text.toString().isEmpty()) {
            binding.rePasswordRegistrationMes.visibility = View.VISIBLE
            binding.rePasswordRegistrationMes.text = "Parolni qayta kiriting"
        } else {
            binding.rePasswordRegistrationMes.visibility = View.GONE
        }
        if (organization_id <= 0) {
            binding.spinnerOrganizationMes.visibility = View.VISIBLE
        }
        if (organization_sub_id <= 0) {
            binding.spinnerOrganizationNameMes.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

//                binding.registrPassportImageMessage.visibility = View.GONE
            image_uri = filePhoto?.absolutePath?.toUri().toString()
            binding.imgUser.setImageURI(saveBitmapToFile(filePhoto)?.absolutePath?.toUri())


        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        if (requestCode == IMAGE_CHOOSE && resultCode == Activity.RESULT_OK) {

//                binding.registrPassportImageMessage.visibility = View.GONE
//                passportImageUri = filePhoto?.absolutePath?.toUri().toString()
//                binding.logo.setImageURI(saveBitmapToFile(filePhoto)?.absolutePath?.toUri())
//            binding.logo.setImageURI(data?.data)

            Glide
                .with(this)
                .load(data?.data)
                .into(binding.imgUser)
        }
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

            selectedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

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
                        lg("open_camera")
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

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = activity?.getExternalFilesDir(
            Environment.DIRECTORY_PICTURES
        )
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    private fun chooseImageGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

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
}