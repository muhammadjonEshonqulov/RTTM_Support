package uz.intalim.ui.splash

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.rttm.support.R
import uz.rttm.support.data.Repository
import uz.rttm.support.models.body.LoginBody
import uz.rttm.support.models.login.LoginResponse
import uz.rttm.support.utils.NetworkResult
import uz.rttm.support.utils.Prefs
import uz.rttm.support.utils.handleResponse
import uz.rttm.support.utils.hasInternetConnection
import javax.inject.Inject

@HiltViewModel
class SplashVIewModel @Inject constructor(
    val repository: Repository,
    val context: Context,
    val prefss: Prefs,
    application: Application,
) : AndroidViewModel(application) {

    private val _loginResponse: MutableStateFlow<NetworkResult<LoginResponse>> = MutableStateFlow(NetworkResult.Loading())
    var loginResponse: StateFlow<NetworkResult<LoginResponse>> = _loginResponse.asStateFlow()

    fun login(loginBody: LoginBody? = null) = viewModelScope.launch {
        _loginResponse.value = NetworkResult.Loading()
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.login(loginBody ?: LoginBody(prefss.get(prefss.email, ""), prefss.get(prefss.password, "")))
                _loginResponse.value = handleResponse(response)
            } catch (e: Exception) {
                _loginResponse.value = NetworkResult.Error("Xatolik:"+e.message.toString())
            }
        } else {
            _loginResponse.value = NetworkResult.Error(context.getString(R.string.connection_error_message))
        }
    }
}