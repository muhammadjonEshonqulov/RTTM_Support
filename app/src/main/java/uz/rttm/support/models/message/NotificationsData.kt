package uz.rttm.support.models.message

import java.util.*

data class NotificationsData(
    val messageId: String?,
    val data_text: String?,
    val content_text: String?,
    val file: String?,
    val data_updated_at: Date?,
    val fam: String?,
    val fam_mes: String?,
    val name: String?,
    val name_mes: String?,
    val lavozim: String?,
    val role: String?,
    val phone: String?,
    val bolim_name: String,
    val bolim_name_mes: String,
    val user_name: String?,
    val code: String?,
//    val status: Int? = null
)
