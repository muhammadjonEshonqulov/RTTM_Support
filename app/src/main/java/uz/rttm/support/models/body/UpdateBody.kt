package uz.rttm.support.models.body

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class UpdateBody(
    val name: RequestBody?,
    val sh: RequestBody?,
    val fam: RequestBody?,
    val phone: RequestBody?,
    val lavozim: RequestBody?,
    val bolim_id: RequestBody?,
    val oldpassword: RequestBody?,
    val newpassword: RequestBody?,
    val photo: MultipartBody.Part?
)
