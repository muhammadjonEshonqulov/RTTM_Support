package uz.rttm.support.ui.profile

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
import uz.rttm.support.models.body.UpdateBody
import uz.rttm.support.models.login.Bolim
import uz.rttm.support.models.login.LoginResponse
import uz.rttm.support.models.register.RegisterBody
import uz.rttm.support.models.register.RegisterResponse
import uz.rttm.support.models.register.UpdateResponse
import uz.rttm.support.utils.NetworkResult
import uz.rttm.support.utils.handleResponse
import uz.rttm.support.utils.hasInternetConnection
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: Repository,
    application: Application,
) : AndroidViewModel(application) {


    private val _updateResponse = Channel<NetworkResult<UpdateResponse>>()
    var updateResponse = _updateResponse.receiveAsFlow()

    fun update(updateBody: UpdateBody) = viewModelScope.launch {
        _updateResponse.send( NetworkResult.Loading())
        if (hasInternetConnection(getApplication())) {
            try {
                val response = repository.remote.update(updateBody)
                _updateResponse.send( handleResponse(response))
            } catch (e: Exception) {
                _updateResponse.send( NetworkResult.Error("Xatolik : " + e.message))
            }
        } else {
            _updateResponse.send( NetworkResult.Error("Server bilan aloqa yo'q"))
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