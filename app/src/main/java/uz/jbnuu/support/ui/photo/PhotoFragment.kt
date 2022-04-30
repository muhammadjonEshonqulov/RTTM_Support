package uz.jbnuu.support.ui.photo

import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import uz.jbnuu.support.R
import uz.jbnuu.support.databinding.FragmentPhotoBinding
import uz.jbnuu.support.ui.base.BaseFragment
import uz.jbnuu.support.utils.theme.Theme

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