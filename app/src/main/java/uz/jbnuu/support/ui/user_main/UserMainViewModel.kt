package uz.jbnuu.support.ui.user_main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import uz.jbnuu.support.models.message.PushNotification
import uz.jbnuu.support.data.Repository
import uz.jbnuu.support.models.body.CreateMessageBody
import uz.jbnuu.support.models.body.LoginBody
import uz.jbnuu.support.models.login.LoginResponse
import uz.jbnuu.support.models.message.CreateMessageResponse
import uz.jbnuu.support.utils.NetworkResult
import uz.jbnuu.support.utils.handleResponse
import uz.jbnuu.support.utils.hasInternetConnection
import uz.jbnuu.support.utils.lg
import javax.inject.Inject

@HiltViewModel
class UserMainViewModel @Inject constructor(
    private val repository: Repository,
    application: Application,
) : AndroidViewModel(application) {

    private val _notificationResponse = Channel<NetworkResult<ResponseBody>>()
    var notificationResponse = _notificationResponse.receiveAsFlow()

    fun postNotify(notification: PushNotification) = viewModelScope.launch {
        _notificationResponse.send(NetworkResult.Loading())
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.postNotification(notification)
                _notificationResponse.send(handleResponse(response))
            } catch (e: Exception) {
                _notificationResponse.send(NetworkResult.Error("Xatolik : " + e.message))
            }
        } else {
            _notificationResponse.send(NetworkResult.Error("Server bilan aloqa yo'q"))
        }
    }

//    private val _sendMessageResponse: MutableStateFlow<NetworkResult<CreateMessageResponse>> = Channel(NetworkResult.Loading())
    private var _sendMessageResponse: Channel<NetworkResult<CreateMessageResponse>> = Channel()
    val sendMessageResponse = _sendMessageResponse.receiveAsFlow()
    fun sendMessage(body: CreateMessageBody) = viewModelScope.launch {
        _sendMessageResponse.send(NetworkResult.Loading())
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.messageCreate(body)
                _sendMessageResponse.send(handleResponse(response))
            } catch (e: Exception) {
                _sendMessageResponse.send(NetworkResult.Error("Xatolik : " + e.message))
            }
        } else {
            _sendMessageResponse.send(NetworkResult.Error("Server bilan aloqa yo'q"))
        }
    }

    private val _loginResponse: MutableStateFlow<NetworkResult<LoginResponse>> = MutableStateFlow(NetworkResult.Loading())
    var loginResponse: StateFlow<NetworkResult<LoginResponse>> = _loginResponse.asStateFlow()

    fun login(loginBody: LoginBody) = viewModelScope.launch {
        _loginResponse.value = NetworkResult.Loading()
        if (hasInternetConnection(getApplication())) {
            try {
                val response =repository.remote.login(loginBody)
                _loginResponse.value = handleResponse(response)
            } catch (e: Exception) {
                _loginResponse.value = NetworkResult.Error("Xatolik : "+e.message)
            }
        } else {
            _loginResponse.value = NetworkResult.Error("Server bilan aloqa yo'q")
        }
    }
}