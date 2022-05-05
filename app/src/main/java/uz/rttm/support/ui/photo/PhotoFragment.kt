package uz.rttm.support.ui.photo

import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import uz.rttm.support.R
import uz.rttm.support.databinding.FragmentPhotoBinding
import uz.rttm.support.ui.base.BaseFragment
import uz.rttm.support.utils.theme.Theme

class PhotoFragment : BaseFragment<FragmentPhotoBinding>(FragmentPhotoBinding::inflate) {


    override fun onCreate(view: View) {

        onCreateTheme(themeManager.currentTheme)

        try {
            arguments?.getString("url_img")?.let {
                Glide.with(requireContext())
                    .load(it)
                    .placeholder(R.mipmap.ic_launcher)
                    .diskCacheStrategy(
                        DiskCacheStrategy.ALL)
                    .into(binding.photoVieww)
            }

        } catch (e: Exception){

        }
    }

    override fun onCreateTheme(theme: Theme) {
        super.onCreateTheme(theme)

        val background = ContextCompat.getColor(requireContext(), theme.backgroundColor)

        binding.fragmentPhoto.setBackgroundColor(background)
    }
}