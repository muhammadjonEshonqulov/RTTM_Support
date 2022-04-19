package uz.jbnuu.support.ui.base

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import uz.jbnuu.support.R
import uz.jbnuu.support.databinding.DialogCloseAndRatingBinding
import uz.jbnuu.support.databinding.DialogLogutBinding

class CloseAndRatingDialog:AlertDialog  {
    private var text: TextView? = null

    
    var submit_listener_onclick : (() -> Unit)?=null
    var cancel_listener_onclick : (() -> Unit)?=null
    lateinit var binding: DialogCloseAndRatingBinding
    
    fun setOnSubmitClick(l:(() -> Unit)?){
        submit_listener_onclick = l
    }
    
    
    fun setOnCancelClick(l:(() -> Unit)?){
        cancel_listener_onclick = l
    }
    
    constructor(context:Context) : super(context){
        this.setCancelable(true)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_close_and_rating, null, false)
        binding = DialogCloseAndRatingBinding.bind(view)
        view?.apply {


//            binding.logoutTittle.text = title
//            if (title == "another_role_message"){
//                binding.logoutMessage.movementMethod = LinkMovementMethod.getInstance()
//                binding.logoutMessage.text = Html.fromHtml(message)
//            } else {
//                binding.logoutMessage.text = message
//            }
//            binding.cancelLogout.text = cancel_text
//            binding.logoutSubmit.text = ok_text
            
            binding.submit.setOnClickListener {
                submit_listener_onclick?.invoke()
            }
            binding.cancel.setOnClickListener {
                cancel_listener_onclick?.invoke()
            }
        }
        setView(view)
    }
}