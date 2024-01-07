package uz.rttm.support.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import uz.rttm.support.utils.Constants.Companion.AD_UNIT_ID
import uz.rttm.support.utils.lg
import java.util.*


@HiltAndroidApp
class App : Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {
    //     var context: Context? = null
    private lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)

//        MobileAds.initialize(this) { }
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        appOpenAdManager = AppOpenAdManager();

        context = this.applicationContext
        FirebaseApp.initializeApp(applicationContext)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.
        currentActivity?.let {
            showAdIfAvailable(it)
        }
    }

    /** Show the ad if one isn't already showing. */
    fun showAdIfAvailable(activity: Activity) {
        appOpenAdManager.showAdIfAvailable(
            activity,
            object : OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                    // Empty because the user will go back to the activity that shows the ad.
                }
            })
    }

    //
    companion object {
        lateinit var context: Context
    }


    /** Inner class that loads and shows app open ads.  */
    private inner class AppOpenAdManager {
        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        private var loadTime: Long = 0

        var isShowingAd = false

        /** Request an ad. */
        fun loadAd(context: Context) {
            // Do not load ad if there is an unused ad or one is already loading.
            if (isLoadingAd || isAdAvailable()) {
                return
            }

            isLoadingAd = true
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                context, AD_UNIT_ID, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {

                    override fun onAdLoaded(ad: AppOpenAd) {
                        // Called when an app open ad has loaded.
                        lg("LOG_TAG" + "Ad was loaded.")
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Called when an app open ad has failed to load.
                        lg("LOG_TAG" + loadAdError.message)
                        isLoadingAd = false;
                    }
                })
        }

        /** Check if ad exists and can be shown. */
//        private fun isAdAvailable(): Boolean {
//            return appOpenAd != null
//        }

        fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener) {
            // If the app open ad is already showing, do not show the ad again.
            if (isShowingAd) {
                lg("LOG_TAG" + "The app open ad is already showing.")
                return
            }

            // If the app open ad is not available yet, invoke the callback then load the ad.
            if (!isAdAvailable()) {
                lg("LOG_TAG" + "The app open ad is not ready yet.")
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
                return
            }

            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    // Called when full screen content is dismissed.
                    // Set the reference to null so isAdAvailable() returns false.
                    lg("LOG_TAG" + "Ad dismissed fullscreen content.")
                    appOpenAd = null
                    isShowingAd = false

                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Called when fullscreen content failed to show.
                    // Set the reference to null so isAdAvailable() returns false.
                    lg("LOG_TAG" + adError.message)
                    appOpenAd = null
                    isShowingAd = false

                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when fullscreen content is shown.
                    lg("LOG_TAG" + "Ad showed fullscreen content.")
                }
            }
            isShowingAd = true
            appOpenAd?.show(activity)
        }

        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
            val dateDifference: Long = Date().time - loadTime
//            val numMilliSecondsPerHour: Long = 3600000
            val numMilliSecondsPerHour: Long = 60000
            return dateDifference < numMilliSecondsPerHour * numHours
        }

        private fun isAdAvailable(): Boolean {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }
    }

    private var currentActivity: Activity? = null

    /** ActivityLifecycleCallback methods. */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        // Updating the currentActivity only when an ad is not showing.
        if (!appOpenAdManager?.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }


}