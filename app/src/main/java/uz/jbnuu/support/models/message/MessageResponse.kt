package uz.jbnuu.support.models.message

import uz.jbnuu.support.models.login.User
import java.util.*

data class MessageResponse(
    val id : Int?,
    val text : String?,
    val title : String?,
    val status : Int?,
    val user_id : Int?,
    val created_at : Date?,
    val updated_at : Date?,
    val user : User?,
    val worker : User?,
    val reyting : String?,
    var expandable : Boolean = false,
    val manager : String?
)
