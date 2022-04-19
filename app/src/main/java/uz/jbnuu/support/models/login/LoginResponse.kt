package uz.jbnuu.support.models.login


data class LoginResponse(
    val user: User?,
    val token: String?,
)
