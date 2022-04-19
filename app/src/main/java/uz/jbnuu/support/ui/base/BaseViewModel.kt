package uz.jbnuu.support.ui.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.jbnuu.support.R
import uz.jbnuu.support.data.Repository
import uz.jbnuu.support.models.body.LoginBody
import uz.jbnuu.support.models.login.LoginResponse
import uz.jbnuu.support.utils.NetworkResult
import uz.jbnuu.support.utils.Prefs
import uz.jbnuu.support.utils.handleResponse
import uz.jbnuu.support.utils.hasInternetConnection
import javax.inject.Inject

open class BaseViewModel (application: Application): AndroidViewModel(application) {
    
    @Inject
    lateinit var  repository: Repository
    @SuppressLint("StaticFieldLeak")
    @Inject
    lateinit var  context: Context
    @Inject
    lateinit var  prefss: Prefs
    
//    private val _loginResponse: MutableStateFlow<NetworkResult<LoginResponse>> = MutableStateFlow(NetworkResult.Loading())
//    var loginResponse: StateFlow<NetworkResult<LoginResponse>> = _loginResponse.asStateFlow()
//
//    fun login(loginBody: LoginBody? = null) = viewModelScope.launch {
//        _loginResponse.value = NetworkResult.Loading()
//        if (hasInternetConnection(getApplication())) {
//            try {
//                val response = repository.remote.login(loginBody ?: LoginBody(prefss.get(prefss.username, ""), prefss.get(prefss.password, ""))
//                )
//                _loginResponse.value = handleResponse(response)
//            } catch (e: Exception) {
//                _loginResponse.value = NetworkResult.Error(e.message)
//            }
//        } else {
//            _loginResponse.value = NetworkResult.Error(context.getString(R.string.connection_error_message))
//        }
//    }
    
}