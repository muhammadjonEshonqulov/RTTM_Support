package uz.jbnuu.support.models.message

import java.util.*

data class CreateMessageResponse(
    val text:String?,
    val user_id:Int?,
    val updated_at:Date?,
    val created_at:Date?,
    val id:Int?,
    val title:String?,
    val img:String?,
)
