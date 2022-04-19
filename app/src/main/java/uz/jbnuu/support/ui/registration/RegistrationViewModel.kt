package uz.jbnuu.support.ui.registration

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
import uz.jbnuu.support.data.Repository
import uz.jbnuu.support.models.body.LoginBody
import uz.jbnuu.support.models.login.Bolim
import uz.jbnuu.support.models.login.LoginResponse
import uz.jbnuu.support.models.register.RegisterBody
import uz.jbnuu.support.models.register.RegisterResponse
import uz.jbnuu.support.utils.NetworkResult
import uz.jbnuu.support.utils.handleResponse
import uz.jbnuu.support.utils.hasInternetConnection
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val repository: Repository,
    application: Application,
) : AndroidViewModel(application) {


    private val _registerResponse = Channel<NetworkResult<RegisterResponse>>()
    var registerResponse = _registerResponse.receiveAsFlow()

    fun register(registrBody: RegisterBody) = viewModelScope.launch {
        _registerResponse.send( NetworkResult.Loading())
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.register(registrBody)
                _registerResponse.send( handleResponse(response))
            } catch (e: Exception) {
                _registerResponse.send( NetworkResult.Error("Xatolik : " + e.message))
            }
        } else {
            _registerResponse.send( NetworkResult.Error("Server bilan aloqa yo'q"))
        }
    }

    private val _bolimResponse = Channel<NetworkResult<List<Bolim>>>()
    var bolimResponse = _bolimResponse.receiveAsFlow()

    fun bolim(id:Int) = viewModelScope.launch {
        _bolimResponse.send( NetworkResult.Loading())
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.getBolim(id)
                _bolimResponse.send( handleResponse(response))
            } catch (e: Exception) {
                _bolimResponse.send( NetworkResult.Error("Xatolik : " + e.message))
            }
        } else {
            _bolimResponse.send( NetworkResult.Error("Server bilan aloqa yo'q"))
        }
    }

}