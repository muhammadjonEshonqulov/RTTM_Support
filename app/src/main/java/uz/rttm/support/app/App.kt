package uz.rttm.support.app

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class App : Application() {
     var context: Context? = null

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)
        context = applicationContext
    }
}