package uz.rttm.support.ui.news

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
import uz.rttm.support.data.Repository
import uz.rttm.support.models.body.LoginBody
import uz.rttm.support.models.login.LoginResponse
import uz.rttm.support.models.message.MessageResponse
import uz.rttm.support.utils.NetworkResult
import uz.rttm.support.utils.catchErrors
import uz.rttm.support.utils.handleResponse
import uz.rttm.support.utils.hasInternetConnection
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    private val _getMessageResponse = Channel<NetworkResult<List<MessageResponse>>>()
    var getMessageResponse = _getMessageResponse.receiveAsFlow()

    fun getMessage(status: Int) = viewModelScope.launch {
        _getMessageResponse.send(NetworkResult.Loading())
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.getMessage(status)
                _getMessageResponse.send(handleResponse(response))
            } catch (e: Exception) {
                _getMessageResponse.send(catchErrors(e))
            }
        } else {
            _getMessageResponse.send(NetworkResult.Error("Server bilan aloqa yo'q"))
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