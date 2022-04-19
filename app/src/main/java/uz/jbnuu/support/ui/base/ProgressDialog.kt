package uz.jbnuu.support.ui.base

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import uz.jbnuu.support.R

class ProgressDialog : AlertDialog {

    private var text: TextView? = null
    

    constructor(context: Context, message: String) : super(context) {
        this.setCancelable(false)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_wait, null, false)
        view?.apply {
            text = TextView(context)
            text = findViewById(R.id.text_pd)
            text?.text = message
        }
        setView(view)
    }

    fun setMessage(s: String) {
        text?.text = s
    }
}