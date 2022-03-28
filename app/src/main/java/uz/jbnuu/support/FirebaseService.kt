package uz.jbnuu.support

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import uz.jbnuu.support.ui.MainActivity
import uz.jbnuu.support.utils.Prefs
import uz.jbnuu.support.utils.lg
import javax.inject.Inject
import kotlin.random.Random

const val CHANNEL_ID = "channel_jbnuu_support"
@AndroidEntryPoint
class FirebaseService : FirebaseMessagingService() {

    @Inject
    lateinit var prefs:Prefs


    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        lg("RemoteMessage data-> "+message.data)
        lg("RemoteMessage rawData-> "+message.rawData)
        lg("RemoteMessage from-> "+message.from)
        lg("RemoteMessage messageId-> "+message.messageId)
        lg("RemoteMessage messageType-> "+message.messageType)
        lg("RemoteMessage senderId-> "+message.senderId)
        lg("RemoteMessage notification-> "+message.notification)
        lg("RemoteMessage sentTime-> "+message.sentTime)
        lg("RemoteMessage ttl-> "+message.ttl)
        lg("RemoteMessage collapseKey-> "+message.collapseKey)
        lg("RemoteMessage originalPriority-> "+message.originalPriority)
        lg("RemoteMessage describeContents-> "+message.describeContents())
        val intent = Intent(baseContext, uz.jbnuu.support.ui.MainActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        val bundle = bundleOf("bundle" to 121)
        val pendingIntent = NavDeepLinkBuilder(baseContext)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.notificationsFragment)
            .setArguments(bundle)
            .createPendingIntent()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivities(this, 0, arrayOf(intent, intent), FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["tittle"])
            .setContentText(message.data["message"])
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.logo)
            .build()

        notification.contentIntent = pendingIntent
        notificationManager.notify(notificationID, notification)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "JBNUU_Support_channel"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "my channel description"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        prefs.save(prefs.firebaseToken, newToken)
        lg("token --> FirebaseService -> "+newToken)
    }
}