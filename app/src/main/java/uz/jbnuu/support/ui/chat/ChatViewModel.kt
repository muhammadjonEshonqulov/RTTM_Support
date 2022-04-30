package uz.jbnuu.support.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import uz.jbnuu.support.data.Repository
import uz.jbnuu.support.models.body.LoginBody
import uz.jbnuu.support.models.chat.ChatData
import uz.jbnuu.support.models.chat.CreateChatBody
import uz.jbnuu.support.models.chat.CreateChatResponse
import uz.jbnuu.support.models.login.LoginResponse
import uz.jbnuu.support.models.message.MessageActive
import uz.jbnuu.support.models.message.MessageBallBody
import uz.jbnuu.support.models.message.PushNotification
import uz.jbnuu.support.utils.NetworkResult
import uz.jbnuu.support.utils.handleResponse
import uz.jbnuu.support.utils.hasInternetConnection
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: Repository,
    application: Application,
) : AndroidViewModel(application) {

    private val _chatCreateResponse = Channel<NetworkResult<CreateChatResponse>>()
    var chatCreateResponse  = _chatCreateResponse.receiveAsFlow()

    fun chatCreate(body: CreateChatBody) = viewModelScope.launch {
        _chatCreateResponse.send( NetworkResult.Loading())
        if (hasInternetConnection(getApplication())) {
            try {
                val response =repository.remote.chatCreate(body)
                _chatCreateResponse.send( handleResponse(response))
            } catch (e: Exception) {
                _chatCreateResponse.send(NetworkResult.Error("Xatolik in create chat: " + e.message))
            }
        } else {
            _chatCreateResponse.send( NetworkResult.Error("Server bilan aloqa yo'q"))
        }
    }

    private val _getChatResponse: MutableStateFlow<NetworkResult<List<ChatData>>> = MutableStateFlow(NetworkResult.Loading())
    var getChatResponse: StateFlow<NetworkResult<List<ChatData>>> = _getChatResponse.asStateFlow()

    fun getChat(message_id:Int) = viewModelScope.launch {
        _getChatResponse.value = NetworkResult.Loading()
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.getChat(message_id)
                _getChatResponse.value = handleResponse(response)
            } catch (e: Exception) {
                _getChatResponse.value = NetworkResult.Error("Xatolik : " + e.message)
            }
        } else {
            _getChatResponse.value = NetworkResult.Error("Server bilan aloqa yo'q")
        }
    }

    private val _ballResponse = Channel<NetworkResult<String>>()
    var ballResponse = _ballResponse.receiveAsFlow()

    fun ball(body: MessageBallBody) = viewModelScope.launch {
        _ballResponse.send(NetworkResult.Loading())
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.messageBall(body)
                _ballResponse.send(handleResponse(response))
            } catch (e: Exception) {
                _ballResponse.send(NetworkResult.Error("Xatolik : " + e.message))
            }
        } else {
            _ballResponse.send(NetworkResult.Error("Server bilan aloqa yo'q"))
        }
    }

    fun chatActive(message_id: Int) = viewModelScope.launch {
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.chatActive(message_id)
            } catch (e: Exception) {
            }
        }
    }

    fun messageActive(message_id: Int) = viewModelScope.launch {
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.messageActive(MessageActive(message_id))
            } catch (e: Exception) {
            }
        }
    }

    private val _notificationResponse = Channel<NetworkResult<ResponseBody>>()
    var notificationResponse = _notificationResponse.receiveAsFlow()

    fun postNotify(notification: PushNotification) = viewModelScope.launch {
        _notificationResponse.send(NetworkResult.Loading())
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.postNotification(notification)
                _notificationResponse.send(handleResponse(response))
            } catch (e: Exception) {
                _notificationResponse.send( NetworkResult.Error("Xatolik : " + e.message))
            }
        } else {
            _notificationResponse.send( NetworkResult.Error("Server bilan aloqa yo'q"))
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
//    private val _userResponse: MutableStateFlow<NetworkResult<User>> = MutableStateFlow(NetworkResult.Loading())
//    var userResponse: StateFlow<NetworkResult<User>> = _userResponse.asStateFlow()
//
//    fun user() = viewModelScope.launch {
//        _userResponse.value = NetworkResult.Loading()
//        if (hasInternetConnection(getApplication())) {
//            try {
//                val response =repository.remote.user()
//                _userResponse.value = handleResponse(response)
//            } catch (e: Exception) {
//                _userResponse.value = NetworkResult.Error("Xatolik : "+e.message)
//            }
//        } else {
//            _userResponse.value = NetworkResult.Error("Server bilan aloqa yo'q")
//        }
//    }
}