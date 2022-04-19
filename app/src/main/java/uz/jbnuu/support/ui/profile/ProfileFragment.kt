package uz.jbnuu.support.ui.profile

import android.provider.ContactsContract
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import uz.jbnuu.support.R
import uz.jbnuu.support.databinding.ProfileFragmentBinding
import uz.jbnuu.support.ui.base.BaseFragment
import uz.jbnuu.support.utils.Constants.Companion.BASE_URL_IMG
import uz.jbnuu.support.utils.Prefs
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment:BaseFragment<ProfileFragmentBinding>(ProfileFragmentBinding::inflate) {

    @Inject
    lateinit var pref:Prefs

    override fun onCreate(view: View) {
        pref.get(pref.photo,"").let {
            Glide
                .with(binding.root)
                .load(BASE_URL_IMG+it)
        }
        binding.position.setText(pref.get(pref.lavozim,""))
        binding.surname.setText(pref.get(pref.fam,""))
        binding.name.setText(pref.get(pref.name,""))
        binding.phone.setText(pref.get(pref.phone,""))
        binding.email.setText(pref.get(pref.email,""))
        binding.passwordRegistration.setText(pref.get(pref.password,""))
        binding.rePasswordRegistration.setText(pref.get(pref.password,""))

        binding.backBtnProfile.setOnClickListener {
            hideKeyBoard()
            finish()
        }

        binding.passwordRegistrationShow.setOnClickListener {
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
    }
}