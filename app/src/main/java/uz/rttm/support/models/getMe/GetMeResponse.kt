package uz.rttm.support.models.getMe

import uz.rttm.support.models.login.User

data class GetMeResponse(
    val user: User,
    val app_version: AppVersion
)
