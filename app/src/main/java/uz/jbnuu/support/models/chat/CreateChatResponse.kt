package uz.jbnuu.support.models.chat

import java.util.*

data class CreateChatResponse(
    val text: String?,
    val message_id: String?,
    val user_id: Int?,
    val updated_at: Date?,
    val created_at: String?,
    val id: Int?
)
