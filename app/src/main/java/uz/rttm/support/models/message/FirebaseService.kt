package uz.rttm.support.models.message

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
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
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import uz.rttm.support.R
import uz.rttm.support.ui.MainActivity
import uz.rttm.support.utils.Prefs
import javax.inject.Inject
import kotlin.random.Random

const val CHANNEL_ID = "channel_jbnuu_support"

@AndroidEntryPoint
class FirebaseService : FirebaseMessagingService() {

    @Inject
    lateinit var prefs: Prefs

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val intent = Intent(baseContext, MainActivity::class.java)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

//        val role = prefs.get(prefs.role, "")

        val bundle = bundleOf(
            "message_id" to message.data["messageId"],
            "data_text" to message.data["data_text"],
            "name" to message.data["name_mes"],
            "fam" to message.data["fam_mes"],
            "file" to message.data["file"],
            "lavozim" to message.data["lavozim"],
            "user_name" to message.data["user_name"],
            "role" to message.data["role"],
            "bolim_name" to message.data["bolim_name_mes"]
        )
        bundle.putString("data_updated_at", Gson().toJson(message.data["data_updated_at"]))

        val pendingIntent = NavDeepLinkBuilder(baseContext)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.chatFragment)  // if (role == prefs.admin) R.id.allNotificationsFragment else R.id.allNotificationsFragment
            .setArguments(bundle)
            .createPendingIntent()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

//        val pendingIntent = PendingIntent.getActivities(this, 0, arrayOf(intent, intent), FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["fam"] + " " + message.data["name"])
            .setContentText(message.data["content_text"])
            .setSubText(message.data["bolim_name"])
//            .setSettingsText(message.data["fromUserName"])
            .setAutoCancel(true)
            .setSmallIcon(R.mipmap.ic_launcher)
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
    }
}