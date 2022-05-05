package uz.rttm.support.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import uz.rttm.support.models.body.CreateMessageBody
import uz.rttm.support.models.body.LoginBody
import uz.rttm.support.models.chat.ChatData
import uz.rttm.support.models.chat.CreateChatResponse
import uz.rttm.support.models.login.Bolim
import uz.rttm.support.models.login.LoginResponse
import uz.rttm.support.models.message.*
import uz.rttm.support.models.register.RegisterResponse
import uz.rttm.support.models.register.UpdateResponse
import uz.rttm.support.utils.Constants.Companion.CONTENT_TYPE
import uz.rttm.support.utils.Constants.Companion.SERVER_KEY

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

    @Multipart
    @POST("update")
    suspend fun update(
        @Part("name") name: RequestBody,
        @Part("sh") sh: RequestBody,
        @Part("fam") fam: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("lavozim") lavozim: RequestBody,
        @Part("bolim_id") bolim_id: RequestBody,
        @Part("oldpassword") oldpassword: RequestBody,
        @Part("newpassword") newpassword: RequestBody,
        @Part photo: MultipartBody.Part?
    ): Response<UpdateResponse>

    @Multipart
    @POST("message/create")
    suspend fun messageCreate(
        @Part("title") title:RequestBody,
        @Part("text") text:RequestBody,
        @Part photo: MultipartBody.Part?,
    ): Response<CreateMessageResponse>

    @POST("message/active")
    suspend fun messageActive(@Body body: MessageActive): Response<String>

    @POST("message/ball")
    suspend fun messageBall(@Body body: MessageBallBody): Response<String>

    @Multipart
    @POST("chat/create")
    suspend fun chatCreate(
        @Part("text") text: RequestBody?,
        @Part("message_id") message_id: RequestBody?,
        @Part photo: MultipartBody.Part?
    ): Response<CreateChatResponse>

    @GET("chat/{message_id}")
    suspend fun getChat(@Path("message_id") message_id: Int): Response<List<ChatData>>

    @GET("chat/active/{message_id}")
    suspend fun chatActive(@Path("message_id") message_id: Int): Response<Int>

    @GET("bolim/{id}")
    suspend fun getBolim(@Path("id") id: Int): Response<List<Bolim>>

    @Headers("Authorization: key=${SERVER_KEY}", "Content-Type:${CONTENT_TYPE}")
    @POST("fcm/send")
    suspend fun postNotification(@Body notification: PushNotification): Response<ResponseBody>

    @GET("message/{status}")
    suspend fun getMessage(@Path("status") status: Int): Response<List<MessageResponse>>
}