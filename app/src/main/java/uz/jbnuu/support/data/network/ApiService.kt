package uz.jbnuu.support.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import uz.jbnuu.support.PushNotification
import uz.jbnuu.support.models.body.LoginBody
import uz.jbnuu.support.models.login.LoginResponse
import uz.jbnuu.support.utils.Constants.Companion.CONTENT_TYPE
import uz.jbnuu.support.utils.Constants.Companion.SERVER_KEY

interface ApiService {

    @POST("login")
    suspend fun login(@Body loginBody: LoginBody): Response<LoginResponse>

    @Headers("Authorization: key=${SERVER_KEY}", "Content-Type:${CONTENT_TYPE}")
    @POST("fcm/send")
    suspend fun postNotification(@Body notification: PushNotification): Response<ResponseBody>

    @Headers("Authorization: key=${SERVER_KEY}", "Content-Type:${CONTENT_TYPE}")
    @GET("fcm/send")
    suspend fun getTopicSubscribes(@Body notification: PushNotification): Response<ResponseBody>
}