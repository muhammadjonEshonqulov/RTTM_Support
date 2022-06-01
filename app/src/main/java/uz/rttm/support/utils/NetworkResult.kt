package uz.rttm.support.utils

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.gson.JsonObject
import dagger.hilt.android.internal.Contexts.getApplication
import org.json.JSONObject
import retrofit2.Response


sealed class NetworkResult<T>(
    val data: T? = null,
    val message: String? = null,
    val code: Int? = null
) {
    class Success<T>(data: T?, code: Int? = null) : NetworkResult<T>(data, code = code)
    class Error<T>(message: String?, data: T? = null, code: Int? = null) :
        NetworkResult<T>(data, message, code)

    class Loading<T> : NetworkResult<T>()
}

fun hasInternetConnection(application: Application): Boolean {
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
            return NetworkResult.Error("Login yoki parol noto'g'ri kiritildi", code = 401)
        }
        response.code() == 404 -> {
            return NetworkResult.Error("Not found", code = 404)
        }
        response.code() == 422 -> {
            val jsonObject = response.errorBody()?.string()?.let { JSONObject(it) }
            return NetworkResult.Error(jsonObject?.getString("msg"), code = 422)
        }
        response.isSuccessful -> {
            val data = response.body()
            return NetworkResult.Success(data = data, code = 200)
        }
        else -> return NetworkResult.Error(response.message())
    }
}