package uz.rttm.support.models.register

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class RegisterBody(
    val email: RequestBody,
    val name: RequestBody,
    val password: RequestBody,
    val return_password: RequestBody,
    val fam: RequestBody,
    val sh: RequestBody,
    val phone: RequestBody,
    val bolim_id: RequestBody,
    val lavozim: RequestBody,
    val photo: MultipartBody.Part?,
    var token: RequestBody? = null
)
