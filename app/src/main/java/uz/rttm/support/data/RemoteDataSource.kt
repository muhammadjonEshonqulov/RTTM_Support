package uz.rttm.support.data

import okhttp3.ResponseBody
import retrofit2.Response
import uz.rttm.support.data.network.NotificationApi
import uz.rttm.support.data.network.ApiService
import uz.rttm.support.models.body.CreateMessageBody
import uz.rttm.support.models.body.LoginBody
import uz.rttm.support.models.body.UpdateBody
import uz.rttm.support.models.chat.ChatData
import uz.rttm.support.models.chat.CreateChatBody
import uz.rttm.support.models.chat.CreateChatResponse
import uz.rttm.support.models.getMe.GetMeResponse
import uz.rttm.support.models.login.Bolim
import uz.rttm.support.models.login.LoginResponse
import uz.rttm.support.models.message.*
import uz.rttm.support.models.register.*
import uz.rttm.support.models.repeat.RepeatBody
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val apiService: ApiService, private val notificationApi: NotificationApi) {

    suspend fun login(loginBody: LoginBody): Response<LoginResponse> {
        return apiService.login(loginBody)
    }
    suspend fun register(registerBody: RegisterBody): Response<RegisterResponse> {
        return apiService.register(registerBody.email,registerBody.name, registerBody.password, registerBody.return_password, registerBody.fam, registerBody.sh, registerBody.phone, registerBody.bolim_id, registerBody.lavozim,registerBody.photo, registerBody.token )
    }
    suspend fun update(updateBody: UpdateBody): Response<UpdateResponse> {
        return apiService.update(updateBody.name,updateBody.sh,updateBody.fam,updateBody.phone,updateBody.lavozim,updateBody.bolim_id,updateBody.oldpassword,updateBody.newpassword,updateBody.photo )
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
    suspend fun messageBall(body: MessageBallBody): Response<String> {
        return apiService.messageBall(body)
    }
    suspend fun chatCreate(body: CreateChatBody): Response<CreateChatResponse> {
        return apiService.chatCreate(body.text, body.message_id, body.photo)
    }
    suspend fun getChat(message_id:Int): Response<List<ChatData>> {
        return apiService.getChat(message_id)
    }
    suspend fun forget(email:String): Response<ForgetResponse> {
        return apiService.forget(email)
    }
    suspend fun repeat(repeatBody: RepeatBody): Response<Int> {
        return apiService.repeat(repeatBody)
    }
    suspend fun registerVerify(registerVerifyBody: RegisterVerifyBody): Response<ForgetResponse> {
        return apiService.registerVerify(registerVerifyBody)
    }
    suspend fun chatActive(message_id:Int): Response<Int> {
        return apiService.chatActive(message_id)
    }
    suspend fun getBolim(id:Int): Response<List<Bolim>> {
        return apiService.getBolim(id)
    }
    suspend fun postNotification(notification: PushNotification): Response<ResponseBody> {
        return notificationApi.postNotification(notification)
    }

    suspend fun getMe(): Response<GetMeResponse> {
        return apiService.getMe()
    }

}