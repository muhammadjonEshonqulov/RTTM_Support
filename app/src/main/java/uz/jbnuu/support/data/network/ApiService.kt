package uz.jbnuu.support.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import uz.jbnuu.support.models.body.CreateMessageBody
import uz.jbnuu.support.models.body.LoginBody
import uz.jbnuu.support.models.chat.ChatData
import uz.jbnuu.support.models.chat.CreateChatResponse
import uz.jbnuu.support.models.login.Bolim
import uz.jbnuu.support.models.login.LoginResponse
import uz.jbnuu.support.models.message.CreateMessageResponse
import uz.jbnuu.support.models.message.MessageActive
import uz.jbnuu.support.models.message.MessageResponse
import uz.jbnuu.support.models.message.PushNotification
import uz.jbnuu.support.models.register.RegisterResponse
import uz.jbnuu.support.utils.Constants.Companion.CONTENT_TYPE
import uz.jbnuu.support.utils.Constants.Companion.SERVER_KEY

interface ApiService {

    @POST("login")
    suspend fun login(@Body loginBody: LoginBody): Response<LoginResponse>

    @Multipart
    @POST("register")
    suspend fun register(
        @Part("email") email: RequestBody,
        @Part("name") name: RequestBody,
        @Part("password") password: RequestBody,
        @Part("return_password") return_password: RequestBody,
        @Part("fam") fam: RequestBody,
        @Part("sh") sh: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("bolim_id") bolim_id: RequestBody,
        @Part("lavozim") lavozim: RequestBody,
        @Part photo: MultipartBody.Part?
    ): Response<RegisterResponse>

    @POST("message/create")
    suspend fun messageCreate(@Body body: CreateMessageBody): Response<CreateMessageResponse>

    @POST("message/active")
    suspend fun messageActive(@Body body: MessageActive): Response<String>

    @Multipart
    @POST("chat/create")
    suspend fun chatCreate(
        @Part("text") text: RequestBody?,
        @Part("message_id") message_id: RequestBody?,
        @Part photo: MultipartBody.Part?
    ): Response<CreateChatResponse>

    @GET("chat/{message_id}")
    suspend fun getChat(@Path("message_id") message_id: Int): Response<List<ChatData>>

    @GET("bolim/{id}")
    suspend fun getBolim(@Path("id") id: Int): Response<List<Bolim>>

    @Headers("Authorization: key=${SERVER_KEY}", "Content-Type:${CONTENT_TYPE}")
    @POST("fcm/send")
    suspend fun postNotification(@Body notification: PushNotification): Response<ResponseBody>

    @Headers("Authorization: key=${SERVER_KEY}", "Content-Type:${CONTENT_TYPE}")
    @GET("fcm/send")
    suspend fun getTopicSubscribes(@Body notification: PushNotification): Response<ResponseBody>

    @GET("message/{status}")
    suspend fun getMessage(@Path("status") status: Int): Response<List<MessageResponse>>
}