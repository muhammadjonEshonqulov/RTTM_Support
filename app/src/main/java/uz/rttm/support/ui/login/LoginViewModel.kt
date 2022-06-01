package uz.rttm.support.ui.login

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
import uz.rttm.support.models.login.User
import uz.rttm.support.models.register.ForgetResponse
import uz.rttm.support.models.repeat.RepeatBody
import uz.rttm.support.utils.NetworkResult
import uz.rttm.support.utils.handleResponse
import uz.rttm.support.utils.hasInternetConnection
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: Repository,
    application: Application,
) : AndroidViewModel(application) {

    private val _loginResponse = Channel<NetworkResult<LoginResponse>>()
    var loginResponse = _loginResponse.receiveAsFlow()

    fun login(loginBody: LoginBody) = viewModelScope.launch {
        _loginResponse.send( NetworkResult.Loading())
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.login(loginBody)
                _loginResponse.send(handleResponse(response))
            } catch (e: Exception) {
                _loginResponse.send( NetworkResult.Error("Xatolik : "+e.message))
            }
        } else {
            _loginResponse.send( NetworkResult.Error("Server bilan aloqa yo'q"))
        }
    }
    private val _forgetResponse = Channel<NetworkResult<ForgetResponse>>()
    var forgetResponse = _forgetResponse.receiveAsFlow()

    fun forget(email:String) = viewModelScope.launch {
        _forgetResponse.send( NetworkResult.Loading())
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.forget(email)
                _forgetResponse.send(handleResponse(response))
            } catch (e: Exception) {
                _forgetResponse.send( NetworkResult.Error("Xatolik : "+e.message))
            }
        } else {
            _forgetResponse.send( NetworkResult.Error("Server bilan aloqa yo'q"))
        }
    }
    private val _repeatResponse = Channel<NetworkResult<Int>>()
    var repeatResponse = _repeatResponse.receiveAsFlow()

    fun repeat(repeatBody: RepeatBody) = viewModelScope.launch {
        _repeatResponse.send( NetworkResult.Loading())
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.repeat(repeatBody)
                _repeatResponse.send(handleResponse(response))
            } catch (e: Exception) {
                _repeatResponse.send( NetworkResult.Error("Xatolik : "+e.message))
            }
        } else {
            _repeatResponse.send( NetworkResult.Error("Server bilan aloqa yo'q"))
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