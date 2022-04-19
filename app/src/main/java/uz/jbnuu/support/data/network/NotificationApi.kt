package uz.jbnuu.support.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import uz.jbnuu.support.models.message.PushNotification
import uz.jbnuu.support.utils.Constants.Companion.CONTENT_TYPE
import uz.jbnuu.support.utils.Constants.Companion.SERVER_KEY

interface NotificationApi {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(@Body notification: PushNotification): Response<ResponseBody>
}