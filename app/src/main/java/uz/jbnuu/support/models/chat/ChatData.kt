package uz.jbnuu.support.models.chat

import uz.jbnuu.support.models.login.User
import java.util.*

data class ChatData(
    val id:Int?,
    val text:String?,
    val file:String?,
    val user_id:Int?,
    val message_id:Int?,
    val created_at:Date?,
    val updated_at: Date?,
    val user:User?,
)