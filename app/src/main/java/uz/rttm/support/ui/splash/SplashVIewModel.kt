package uz.rttm.support.ui.splash

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uz.rttm.support.R
import uz.rttm.support.data.Repository
import uz.rttm.support.models.body.LoginBody
import uz.rttm.support.models.getMe.GetMeResponse
import uz.rttm.support.models.login.LoginResponse
import uz.rttm.support.utils.*
import javax.inject.Inject

@HiltViewModel
class SplashVIewModel @Inject constructor(
    val repository: Repository,
    val context: Context,
    val prefss: Prefs,
    application: Application,
) : AndroidViewModel(application) {

    private val _getMeResponse = Channel<NetworkResult<GetMeResponse>>()
    var getMeResponse = _getMeResponse.receiveAsFlow()

    fun getMe() = viewModelScope.launch {
        _getMeResponse.send(NetworkResult.Loading())
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.getMe()
                _getMeResponse.send(handleResponse(response))
            } catch (e: Exception) {
                _getMeResponse.send(catchErrors(e))
            }
        } else {
            _getMeResponse.send(NetworkResult.Error(context.getString(R.string.connection_error_message)))
        }
    }

    private val _loginResponse = Channel<NetworkResult<LoginResponse>>()
    var loginResponse = _loginResponse.receiveAsFlow()

    fun login(loginBody: LoginBody? = null) = viewModelScope.launch {
        _loginResponse.send(NetworkResult.Loading())
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.login(
                    loginBody ?: LoginBody(
                        prefss.get(prefss.email, ""),
                        prefss.get(prefss.password, "")
                    )
                )
                _loginResponse.send(handleResponse(response))
            } catch (e: Exception) {
                _loginResponse.send(catchErrors(e))
            }
        } else {
            _loginResponse.send(NetworkResult.Error(context.getString(R.string.connection_error_message)))
        }
    }
}