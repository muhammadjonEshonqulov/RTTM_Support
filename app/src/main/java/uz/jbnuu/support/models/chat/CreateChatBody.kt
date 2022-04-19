package uz.jbnuu.support.models.chat

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class CreateChatBody(
    val text: RequestBody?,
    val message_id: RequestBody?,
    val photo: MultipartBody.Part?,
)
