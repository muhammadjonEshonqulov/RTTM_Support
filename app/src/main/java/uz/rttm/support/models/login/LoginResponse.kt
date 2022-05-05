package uz.rttm.support.models.login


data class LoginResponse(
    val user: User?,
    val token: String?,
)
