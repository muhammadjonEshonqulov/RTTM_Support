package uz.jbnuu.support.utils

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import dagger.hilt.android.internal.Contexts.getApplication
import retrofit2.Response

sealed class NetworkResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T?) : NetworkResult<T>(data)
    class Error<T>(message: String?, data: T?? = null) : NetworkResult<T>(data, message)
    class Loading<T> : NetworkResult<T>()
}
fun hasInternetConnection (application: Application): Boolean {
    val connectivityManager =
        getApplication(application).getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork =
        connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}

fun <T> handleResponse(response: Response<T>): NetworkResult<T> {
    when {
        response.message().toString().contains("timeout") -> {
            return NetworkResult.Error("Timeout.")
        }
        response.code() == 401 -> {
            return NetworkResult.Error("Login yoki parol noto'g'ri kiritildi")
        }
        response.code() == 404 -> {
            return NetworkResult.Error("Not found")
        }
        response.isSuccessful -> {
            val topicData = response.body()
            return NetworkResult.Success(topicData)
        }
        else -> return NetworkResult.Error(response.message())
    }
}