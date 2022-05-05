package uz.rttm.support.models.body

import okhttp3.MultipartBody
import okhttp3.RequestBody

data class CreateMessageBody(
    val title: RequestBody,
    val text: RequestBody,
    val photo: MultipartBody.Part?
)
