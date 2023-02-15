package uz.rttm.support.ui.managers

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import uz.rttm.support.R
import uz.rttm.support.app.App
import uz.rttm.support.data.Repository
import uz.rttm.support.utils.NetworkResult
import uz.rttm.support.utils.catchErrors
import uz.rttm.support.utils.hasInternetConnection
import javax.inject.Inject

@HiltViewModel
class ManagersViewModel @Inject constructor(
    repository: Repository,
    val application: Application
) : ViewModel() {
    private val _managers = Channel<NetworkResult<List<Map<String, String>>>>()
    val managers = _managers.receiveAsFlow()

    fun getManagers() = viewModelScope.launch {
        _managers.send(NetworkResult.Loading())
        if (hasInternetConnection(application)) {
            try {
                val mFirestore = FirebaseFirestore.getInstance()
                mFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

                mFirestore.collection("managers_list").document("managers")
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            viewModelScope.launch {
                                _managers.send(NetworkResult.Success(document.data?.get("my_data") as List<Map<String, String>>))
                            }
                        } else {
                            viewModelScope.launch {
                                _managers.send(NetworkResult.Error("No document"))
                            }
                        }

                    }.addOnFailureListener { e ->
                        viewModelScope.launch {
                            _managers.send(NetworkResult.Error("Error writing document->$e"))
                        }
                    }
            } catch (e: Exception) {
                _managers.send(catchErrors(e))
            }
        } else {
            _managers.send(NetworkResult.Error(App.context.getString(R.string.connection_error_message)))
        }
    }

}