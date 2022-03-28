package uz.jbnuu.support.utils

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View?.blockClickable(
    blockTimeMilles: Long = 500
) {
    this?.isClickable = false
    Handler().postDelayed({ this?.isClickable = true }, blockTimeMilles)
}

fun snack(view: View, text:String) {
    Snackbar.make(view, ""+text, Snackbar.LENGTH_SHORT).show()
}
fun lg(text: String){
    Log.d("JBNUU", text)
}