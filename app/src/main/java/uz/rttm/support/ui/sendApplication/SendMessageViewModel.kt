package uz.rttm.support.ui.sendApplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import uz.rttm.support.models.message.PushNotification
import uz.rttm.support.data.Repository
import uz.rttm.support.models.body.CreateMessageBody
import uz.rttm.support.models.body.LoginBody
import uz.rttm.support.models.login.LoginResponse
import uz.rttm.support.models.message.CreateMessageResponse
import uz.rttm.support.utils.*
import javax.inject.Inject

@HiltViewModel
class SendMessageViewModel @Inject constructor(
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
                _notificationResponse.send(catchErrors(e))
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
                _sendMessageResponse.send(catchErrors(e))
            }
        } else {
            _sendMessageResponse.send(NetworkResult.Error("Server bilan aloqa yo'q"))
        }
    }

    private val _loginResponse = Channel<NetworkResult<LoginResponse>>()
    var loginResponse = _loginResponse.receiveAsFlow()

    fun login(loginBody: LoginBody) = viewModelScope.launch {
        _loginResponse.send(NetworkResult.Loading())
        if (hasInternetConnection(getApplication())) {
            try {
                val response =repository.remote.login(loginBody)
                _loginResponse.send(handleResponse(response))
            } catch (e: Exception) {
                _loginResponse.send(catchErrors(e))
            }
        } else {
            _loginResponse.send(NetworkResult.Error("Server bilan aloqa yo'q"))
        }
    }
}