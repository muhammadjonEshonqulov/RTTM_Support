package uz.rttm.support.models.message

data class ManagerResponse(
    val my_data: List<ManagerData>?
) {
    data class ManagerData(
        val first_name: String?,
        val image: String?,
        val last_name: String?,
        val middle_name: String?,
        val phone: String?,
        val telegram: String?,
    )
}

