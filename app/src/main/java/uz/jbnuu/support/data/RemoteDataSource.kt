package uz.jbnuu.support.data

import okhttp3.ResponseBody
import retrofit2.Response
import uz.jbnuu.support.data.network.NotificationApi
import uz.jbnuu.support.models.message.PushNotification
import uz.jbnuu.support.data.network.ApiService
import uz.jbnuu.support.models.body.CreateMessageBody
import uz.jbnuu.support.models.body.LoginBody
import uz.jbnuu.support.models.chat.ChatData
import uz.jbnuu.support.models.chat.CreateChatBody
import uz.jbnuu.support.models.chat.CreateChatResponse
import uz.jbnuu.support.models.login.Bolim
import uz.jbnuu.support.models.login.LoginResponse
import uz.jbnuu.support.models.message.CreateMessageResponse
import uz.jbnuu.support.models.message.MessageActive
import uz.jbnuu.support.models.message.MessageResponse
import uz.jbnuu.support.models.register.RegisterBody
import uz.jbnuu.support.models.register.RegisterResponse
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val apiService: ApiService, private val notificationApi: NotificationApi) {

    suspend fun login(loginBody: LoginBody): Response<LoginResponse> {
        return apiService.login(loginBody)
    }
    suspend fun register(registerBody: RegisterBody): Response<RegisterResponse> {
        return apiService.register(registerBody.email,registerBody.name, registerBody.password, registerBody.return_password, registerBody.fam, registerBody.sh, registerBody.phone, registerBody.bolim_id, registerBody.lavozim,registerBody.photo )
    }
    suspend fun getMessage(status: Int): Response<List<MessageResponse>> {
        return apiService.getMessage(status)
    }
    suspend fun messageCreate(body: CreateMessageBody): Response<CreateMessageResponse> {
        return apiService.messageCreate(body.title, body.text,body.photo)
    }
    suspend fun messageActive(body: MessageActive): Response<String> {
        return apiService.messageActive(body)
    }
    suspend fun chatCreate(body: CreateChatBody): Response<CreateChatResponse> {
        return apiService.chatCreate(body.text, body.message_id, body.photo)
    }
    suspend fun getChat(message_id:Int): Response<List<ChatData>> {
        return apiService.getChat(message_id)
    }
    suspend fun getBolim(id:Int): Response<List<Bolim>> {
        return apiService.getBolim(id)
    }
    suspend fun postNotification(notification: PushNotification): Response<ResponseBody> {
        return notificationApi.postNotification(notification)
    }

}