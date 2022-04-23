package uz.jbnuu.support.models.chat

import java.util.*

data class CreateChatResponse(
    val text: String?,
    val message_id: String?,
    val file: String?,
    val user_id: Int?,
    val updated_at: Date?,
    val created_at: Date?,
    val id: Int?
)
