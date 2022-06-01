package uz.rttm.support.models.register

data class RegisterResponse(
    val msg: String?,
    val name: String?,
    val fam: String?,
    val sh: String?,
    val phone: String?,
    val bolim_id: String?,
    val lavozim: String?,
    val photo: String?,
    val email: String?,
    val updated_at: String?,
    val created_at: String?,
    val id: Int?
)