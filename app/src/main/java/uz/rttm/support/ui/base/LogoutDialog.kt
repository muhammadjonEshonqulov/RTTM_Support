package uz.rttm.support.ui.base

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import uz.rttm.support.R
import uz.rttm.support.databinding.DialogLogutBinding

class LogoutDialog:AlertDialog  {
    private var text: TextView? = null

    
    var submit_listener_onclick : (() -> Unit)?=null
    var cancel_listener_onclick : (() -> Unit)?=null
    var binding: DialogLogutBinding
    
    fun setOnSubmitClick(l:(() -> Unit)?){
        submit_listener_onclick = l
    }
    
    
    fun setOnCancelClick(l:(() -> Unit)?){
        cancel_listener_onclick = l
    }
    
    constructor(context: Context, tittle: String? = null, message: String? = null, ok_text: String? = null) : super(context) {
        this.setCancelable(true)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_logut, null, false)
        binding = DialogLogutBinding.bind(view)
        tittle?.let {
            binding.logoutTittle.text = it
        }
        message?.let {
            binding.logoutMessage.text = it
        }
        ok_text?.let {
            binding.logoutSubmit.text = it
        }
        view?.apply {

            binding.logoutSubmit.setOnClickListener {
                submit_listener_onclick?.invoke()
            }
            binding.cancelLogout.setOnClickListener {
                cancel_listener_onclick?.invoke()
            }
        }
        setView(view)
    }
}