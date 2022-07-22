package uz.rttm.support.utils

import android.annotation.SuppressLint
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import uz.rttm.support.BuildConfig
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun View?.blockClickable(blockTimeMilles: Long = 500) {
    this?.isClickable = false
    Handler().postDelayed({ this?.isClickable = true }, blockTimeMilles)
}

fun snack(view: View, text: String) {
    Snackbar.make(view, "" + text, Snackbar.LENGTH_SHORT).show()
}

fun lg(text: String) {
    if(BuildConfig.isDebug){
        Log.d("RTTM", text)
    }
}

fun Fragment.findNavControllerSafely(): NavController? {
    return if (isAdded) {
        findNavController()
    } else {
        null
    }
}


@SuppressLint("SimpleDateFormat")
fun formatDateStr(strDate: Date): String {
    val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ")
    formatter.timeZone = TimeZone.getTimeZone("Asia/Tashkent")
    var date = formatter.format(strDate).split("T")
    val day = date.first().split("-").last()
    val month = date.first().split("-")[1]
    val year = date.first().split("-").first()

    val hour = date.last().split(".").first().split(":").first()
    val minute = date.last().split(".").first().split(":")[1]
    val sekund = date.last().split(".").first().split(":").last()


    return "$day.$month.$year ($hour:$minute:$sekund)"
}