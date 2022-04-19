package uz.jbnuu.support.models.message

data class PushNotification(
    val data: NotificationsData,
    val to: String
)