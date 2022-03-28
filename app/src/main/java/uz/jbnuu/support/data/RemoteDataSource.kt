package uz.jbnuu.support.data

import okhttp3.ResponseBody
import retrofit2.Response
import uz.jbnuu.support.NotificationApi
import uz.jbnuu.support.PushNotification
import uz.jbnuu.support.data.network.ApiService
import uz.jbnuu.support.models.body.LoginBody
import uz.jbnuu.support.models.login.LoginResponse
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val apiService: ApiService, private val notificationApi: NotificationApi) {

    suspend fun login(loginBody: LoginBody): Response<LoginResponse> {
        return apiService.login(loginBody)
    }

    suspend fun postNotification(notification: PushNotification): Response<ResponseBody> {
        return notificationApi.postNotification(notification)
    }

}