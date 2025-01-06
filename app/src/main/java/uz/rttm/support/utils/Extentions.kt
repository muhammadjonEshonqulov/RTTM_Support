package uz.rttm.support.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import uz.rttm.support.BuildConfig
import uz.rttm.support.R
import uz.rttm.support.app.App
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone


fun View?.blockClickable(blockTimeMilles: Long = 500) {
    this?.isClickable = false
    Handler().postDelayed({ this?.isClickable = true }, blockTimeMilles)
}

fun snack(view: View, text: String) {
    Snackbar.make(view, "" + text, Snackbar.LENGTH_SHORT).show()
}
fun getNowDate():String{
    val currentDateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDateTime.now()
    } else {
        TODO("VERSION.SDK_INT < O")
    }
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.ENGLISH)
    val formattedDateTime = currentDateTime.format(formatter)
    return formattedDateTime
}
fun lg(text: String) {
    if (BuildConfig.isDebug) {
        Log.d("RTTM_SUPPORT", text)
    }
}

fun isAppAvailable(context: Context, appName: String?): Boolean {
    val pm: PackageManager = context.packageManager
    return try {
        pm.getPackageInfo(appName!!, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun Fragment.findNavControllerSafely(): NavController? {
    return if (isAdded) {
        findNavController()
    } else {
        null
    }
}

fun <T> catchErrors(e: Exception): NetworkResult.Error<T> {
    return when (e) {
        is SocketTimeoutException -> {
            NetworkResult.Error(App.context.getString(R.string.bad_network_message), code = 101)
        }

        is UnknownHostException -> {
            NetworkResult.Error(App.context.getString(R.string.bad_network_message), code = 101)
        }

        else -> {
            print("Error in else => ${App.context.getString(R.string.onother_error) + e.message.toString()}")

            NetworkResult.Error(App.context.getString(R.string.onother_error) + e.message.toString(), code = 101)
        }
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