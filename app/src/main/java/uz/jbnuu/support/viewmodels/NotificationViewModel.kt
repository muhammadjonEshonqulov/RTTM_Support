package uz.jbnuu.support.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import uz.jbnuu.support.PushNotification
import uz.jbnuu.support.data.Repository
import uz.jbnuu.support.utils.NetworkResult
import uz.jbnuu.support.utils.handleResponse
import uz.jbnuu.support.utils.hasInternetConnection
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: Repository,
    application: Application,
) : AndroidViewModel(application) {

    private val _notificationResponse: MutableStateFlow<NetworkResult<ResponseBody>> = MutableStateFlow(
        NetworkResult.Loading())
    var notificationResponse: StateFlow<NetworkResult<ResponseBody>> = _notificationResponse.asStateFlow()

    fun postNotify(notification: PushNotification) = viewModelScope.launch {
        _notificationResponse.value = NetworkResult.Loading()
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.postNotification(notification)
                _notificationResponse.value = handleResponse(response)
            } catch (e: Exception) {
                _notificationResponse.value = NetworkResult.Error("Xatolik : " + e.message)
            }
        } else {
            _notificationResponse.value = NetworkResult.Error("Server bilan aloqa yo'q")
        }
    }
}