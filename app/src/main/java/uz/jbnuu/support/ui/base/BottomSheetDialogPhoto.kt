package uz.jbnuu.support.ui.base

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import uz.jbnuu.support.R
import uz.jbnuu.support.databinding.FragmentPhotoBinding

class BottomSheetDialogPhoto(val url:String) : BottomSheetDialogFragment(){
    
//    var slidrInterface: SlidrInterface? = null
    lateinit var oldview: View
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
        
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout = bottomSheetDialog.findViewById<View>(com.readystatesoftware.chuck.R.id.design_bottom_sheet)
            parentLayout?.let { it ->
                val behaviour = BottomSheetBehavior.from(it)
                setupFullHeight(it)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }
    
    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }
    
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        oldview  = inflater.inflate(R.layout.fragment_photo, container, false)
        val binding : FragmentPhotoBinding = FragmentPhotoBinding.bind(oldview)
        binding.photoClose.setOnClickListener {
            dismiss()
        }
//        binding.fragmentPhoto.setOnTouchListener(this)
        Glide.with(requireContext())
            .load(url)
            .placeholder(R.drawable.logo_rttm)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.photoVieww)
        return oldview
    }
    
//    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//        return event?.action == MotionEvent.ACTION_DOWN
//    }
    
//    override fun onClick(v: View?) {
//
//    }


//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        oldview = inflater.inflate(layout, container, false)
//        oldview.setOnTouchListener { _, _ -> false }
//
//        return FrameLayout(requireContext()).apply {
//            setBackgroundColor(
//                Color.TRANSPARENT)
//            addView(oldview)
////        return inflater.inflate(layout,container,false)
//        }
//    }
}