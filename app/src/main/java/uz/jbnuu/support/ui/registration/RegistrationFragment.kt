package uz.jbnuu.support.ui.registration

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
import android.text.Editable
import android.text.TextWatcher
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
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import uz.jbnuu.support.R
import uz.jbnuu.support.databinding.RegistrationFragmentBinding
import uz.jbnuu.support.models.register.RegisterBody
import uz.jbnuu.support.ui.base.BaseFragment
import uz.jbnuu.support.ui.base.ProgressDialog
import uz.jbnuu.support.utils.FileUtils
import uz.jbnuu.support.utils.NetworkResult
import uz.jbnuu.support.utils.lg
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.regex.Pattern

@AndroidEntryPoint
class RegistrationFragment : BaseFragment<RegistrationFragmentBinding>(RegistrationFragmentBinding::inflate), View.OnClickListener {

    private val vm:RegistrationViewModel by viewModels()
    private var filePhoto: File? = null
    private val PERMISSION_CODE = 1001
    private val IMAGE_CHOOSE = 1000
    private var image_uri = ""
    private val REQUEST_CODE = 13
    private var organization_id = -1
    private var organization_sub_id = -1
    private var progressDialog:ProgressDialog? = null

    override fun onCreate(view: View) {
        binding.logo.setOnClickListener(this)
        binding.passwordRegistrationShow.setOnClickListener(this)
        binding.rePasswordRegistrationShow.setOnClickListener(this)
        binding.backBtn.setOnClickListener(this)
        binding.send.setOnClickListener(this)
        binding.phone.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0!!.length < 9){
                    binding.phoneMes.visibility = View.VISIBLE
                } else {
                    binding.phoneMes.visibility = View.GONE
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        var password = ""
        binding.passwordRegistration.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                password = binding.passwordRegistration.text.toString()
                if(p0!!.length < 6){
                    binding.passwordRegistrationMes.visibility = View.VISIBLE
                    binding.passwordRegistrationMes.text = "Parol kamida 6ta belgidan iborat bo'lishi kerak"
                } else {
                    binding.passwordRegistrationMes.visibility = View.GONE
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        binding.rePasswordRegistration.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0!!.length < 6){
                    binding.rePasswordRegistrationMes.visibility = View.VISIBLE
                    binding.rePasswordRegistrationMes.text = "Parol kamida 6ta belgidan iborat bo'lishi kerak"
                } else {
                    lg("password->"+password)
                    lg("p0->"+p0)
                    if (password == p0.toString()){
                        binding.rePasswordRegistrationMes.visibility = View.GONE
                    } else {
                        binding.rePasswordRegistrationMes.visibility = View.VISIBLE
                        binding.rePasswordRegistrationMes.text = "Parollar mos emas"
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
//        binding.rePasswordRegistration.addTextChangedListener(this)
        setOrganizationItems()
    }

    private fun getBolim(id:Int){
        vm.bolim(id)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.bolimResponse.collect {
                    var arraySpinner = ArrayList<String>()
                    arraySpinner.clear()
                    arraySpinner.add("tanlang")
                    when(it){
                        is NetworkResult.Success->{

                            it.data?.let {
                                it.forEachIndexed { index, bolim ->
                                    arraySpinner.add(bolim.name.toString())
                                }
                            }
                        }
                    }
                    val organizationAdapter = ArrayAdapter(binding.root.context, android.R.layout.simple_spinner_item, arraySpinner)
                    organizationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerOrganizationName.adapter = organizationAdapter
                    binding.spinnerOrganizationName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                            organization_sub_id = p2
                            if (p2 > 0){
                                binding.spinnerOrganizationNameMes.visibility = View.GONE
                            }
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {}

                    }
                }
            }
        }
    }
    private fun setOrganizationItems() {
        var arraySpinner = arrayOf("tanlang","Tarkibiy bo'linma", "Fakultet","Kafedra")
        val organizationAdapter = ArrayAdapter(binding.root.context, android.R.layout.simple_spinner_item, arraySpinner)
        organizationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerOrganization.adapter = organizationAdapter
        binding.spinnerOrganization.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                organization_id = p2
                if (p2 > 0){
                    binding.spinnerOrganizationMes.visibility = View.GONE
                }
                getBolim(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}

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

    override fun onClick(p0: View?) {
        when (p0) {
            binding.send -> {
                hideKeyBoard()
                if (organization_id > 0 && organization_sub_id > 0 && binding.surname.text.toString().isNotEmpty() && binding.name.text.toString().isNotEmpty() &&  binding.position.text.toString().isNotEmpty() && binding.phone.text.toString().isNotEmpty() && binding.email.text.toString().endsWith("@jbnuu.uz") && binding.passwordRegistration.text.toString().isNotEmpty() && binding.rePasswordRegistration.text.toString().isNotEmpty()){
                    if (binding.passwordRegistration.text.toString().length >= 6 && binding.rePasswordRegistration.text.toString().length >= 6 && binding.email.text.toString().endsWith("@jbnuu.uz")){
                        val stringType = "text/plain".toMediaTypeOrNull()
                        val imageTypee = "image/JPEG".toMediaTypeOrNull()
                        val imageUri: Uri = Uri.parse(image_uri)
                        val imageFile: File = FileUtils.getFile(requireContext(), imageUri)
                        val image = saveBitmapToFile(imageFile)
                        vm.register(RegisterBody(
                            binding.email.text.toString().toRequestBody(stringType),
                            binding.name.text.toString().toRequestBody(stringType),
                            binding.passwordRegistration.text.toString().toRequestBody(stringType),
                            binding.rePasswordRegistration.text.toString().toRequestBody(stringType),
                            binding.surname.text.toString().toRequestBody(stringType),
                            " ".toRequestBody(stringType),
                            binding.phone.text.toString().toRequestBody(stringType),
                            organization_sub_id.toString().toRequestBody(stringType),
                            binding.position.text.toString().toRequestBody(stringType),
                            if (image?.exists() == true) MultipartBody.Part.createFormData("photo","", image.readBytes().toRequestBody(imageTypee)) else null
                        ))
                        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                                vm.registerResponse.collect {
                                    when(it){
                                        is NetworkResult.Success ->{
                                            progressDialog?.dismiss()
                                            snackBar("Ro'yxatdan o'tish muvaffaqiyatli amalga oshirildi.")
                                            finish()
                                        }
                                        is NetworkResult.Error -> {
                                            progressDialog?.dismiss()
                                            snackBar(it.message.toString())
                                        }
                                        is NetworkResult.Loading ->{
                                            if (progressDialog == null){
                                                progressDialog = ProgressDialog(binding.root.context, "Yuklanmoqda")
                                            }
                                            progressDialog?.show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (binding.name.text.toString().isEmpty()){
                        binding.name.error = "Ismingizni kiriting"
                    }
                    if (binding.surname.text.toString().isEmpty()){
                        binding.surname.error = "Familiyangizni kiriting"
                    }
                    if (binding.phone.text.toString().isEmpty()){
                        binding.phone.error = "Telefoningizni kiriting"
                    }
                    if (binding.email.text.toString().isEmpty()){
                        binding.email.error = "pochtangizni kiriting"
                    }
                    if (binding.position.text.toString().isEmpty()){
                        binding.position.error = "lavozimingizni kiriting"
                    }

                    if (binding.passwordRegistration.text.toString().isEmpty()){
                      binding.passwordRegistrationMes.visibility = View.VISIBLE
                      binding.passwordRegistrationMes.text = "parolni kiriting"
                    }
                    if (binding.rePasswordRegistration.text.toString().isEmpty()){
                        binding.rePasswordRegistrationMes.visibility = View.VISIBLE
                        binding.rePasswordRegistrationMes.text = "parolni qayta kiriting"
                    }
                    if (organization_id <= 0){
                        binding.spinnerOrganizationMes.visibility = View.VISIBLE
                    }
                    if (organization_sub_id <= 0){
                        binding.spinnerOrganizationNameMes.visibility = View.VISIBLE
                    }
                    if (binding.email.text.toString().isEmpty()){
                        binding.email.error = "Elektron pochta kiriting"
                    } else if (!checkEmail(binding.email.text.toString())){
                            snackBar("Bu email emas")
                    } else if (binding.email.text.toString().split("@jbnuu.uz").first().isEmpty()){
                        binding.email.error = "Elektron pochtangizni to'g'ri kiriting"
                    }
                }
            }
            binding.backBtn -> {
                hideKeyBoard()
                finish()
            }
            binding.logo -> {
                popupCamera(binding.logo)
            }
            binding.passwordRegistrationShow -> {
                if (binding.passwordRegistration.transformationMethod == PasswordTransformationMethod.getInstance()) {
                    binding.passwordRegistrationShow.setImageResource(R.drawable.ic_eye)
                    binding.passwordRegistration.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                } else {
                    binding.passwordRegistrationShow.setImageResource(R.drawable.ic_eyeslash)
                    binding.passwordRegistration.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                }
                binding.passwordRegistration.setSelection(binding.passwordRegistration.length())
            }
            binding.rePasswordRegistrationShow -> {
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
        }
    }

    val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
    private fun checkEmail(email: String): Boolean {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

//                binding.registrPassportImageMessage.visibility = View.GONE
                image_uri = filePhoto?.absolutePath?.toUri().toString()
            binding.logo.setImageURI(saveBitmapToFile(filePhoto)?.absolutePath?.toUri())


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
                .into(binding.logo)
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
}