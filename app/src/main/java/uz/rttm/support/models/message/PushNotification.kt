package uz.rttm.support.models.message

data class PushNotification(
    val message: NotificationTo
)

data class  NotificationTo(
    val topic: String,
    val data: NotificationsData,

    )