package uz.rttm.support.models.login

data class User(
    val id: Int?,
    val name: String?,
    val fam: String?,
    val role: String?,
    val email: String?,
    val phone: String?,
    val photo: String?,
    val lavozim: String?,
    val bolim_id: Int?,
    val bolim: Bolim?
)
