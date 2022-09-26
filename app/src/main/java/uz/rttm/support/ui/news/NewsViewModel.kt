package uz.rttm.support.ui.news

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.rttm.support.data.Repository
import uz.rttm.support.models.body.LoginBody
import uz.rttm.support.models.login.LoginResponse
import uz.rttm.support.models.message.MessageResponse
import uz.rttm.support.utils.NetworkResult
import uz.rttm.support.utils.handleResponse
import uz.rttm.support.utils.hasInternetConnection
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    private val _getMessageResponse: MutableStateFlow<NetworkResult<List<MessageResponse>>> = MutableStateFlow(NetworkResult.Loading())
    var getMessageResponse: StateFlow<NetworkResult<List<MessageResponse>>> = _getMessageResponse.asStateFlow()

    fun getMessage(status: Int) = viewModelScope.launch {
        _getMessageResponse.value = NetworkResult.Loading()
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.getMessage(status)
                _getMessageResponse.value = handleResponse(response)
            } catch (e: Exception) {
                _getMessageResponse.value = NetworkResult.Error("Xatolik : " + e.message)
            }
        } else {
            _getMessageResponse.value = NetworkResult.Error("Server bilan aloqa yo'q")
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