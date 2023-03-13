package uz.rttm.support.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.AndroidEntryPoint
import uz.rttm.support.BuildConfig
import uz.rttm.support.R
import uz.rttm.support.databinding.ActivityMainBinding
import uz.rttm.support.utils.Prefs
import uz.rttm.support.utils.lg
import uz.rttm.support.utils.snack
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var prefss: Prefs

    lateinit var mAdView: AdView


    //    private var appUpdateManager: AppUpdateManager? = null
    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }
    private val MYREQUESTCODE = 100
//    private var mAppUpdateManager: AppUpdateManager? = null

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this)

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object : AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }

        readDataFromFireStore()

//        showSnackBarForCompleteUpdate()

//        checkUpdate()
    }

    private val listener = InstallStateUpdatedListener { installState ->
        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            notifyUser()
        }
    }

    private fun checkUpdate(priority: Long) {
        appUpdateManager.registerListener(listener)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                when (priority) {
                    0L -> { // Flexible
                        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this, MYREQUESTCODE)
                    }
                    1L -> { // IMMEDIATE
                        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, MYREQUESTCODE)
                    }
                }
//                resume()
            }
        }.addOnFailureListener {
            snack(binding.root, "Error in update -> " + it.message.toString())
            lg("Error in update -> " + it.message.toString())
        }
    }

    private fun notifyUser() {
        val snackbar = Snackbar.make(binding.root, "Yangi versiya yuklab olindi.", Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("O'rnatish") { appUpdateManager.completeUpdate() }
        snackbar.setActionTextColor(getColor(uz.rttm.support.R.color.cl_color_primary))
        snackbar.show()
    }

    private fun resume() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.installStatus() == InstallStatus.DOWNLOADED) {
                notifyUser()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MYREQUESTCODE) {
            when (resultCode) {
                RESULT_OK -> {
                    Toast.makeText(this, "RESULT_OK$resultCode", Toast.LENGTH_LONG).show()
                    lg("RESULT_OK  :" + "" + resultCode)
                }
                RESULT_CANCELED -> {
                    Toast.makeText(this, "RESULT_CANCELED$resultCode", Toast.LENGTH_LONG).show()
                    lg("RESULT_CANCELED  :" + "" + resultCode)
                }
                RESULT_IN_APP_UPDATE_FAILED -> {
                    Toast.makeText(this, "RESULT_IN_APP_UPDATE_FAILED$resultCode", Toast.LENGTH_LONG).show()
                    lg("RESULT_IN_APP_FAILED:" + "" + resultCode)
                }
            }

        }
    }

//    private fun showSnackBarForCompleteUpdate() {
//        val snackbar = Snackbar.make(binding.root, "New app is ready!", Snackbar.LENGTH_INDEFINITE)
//        snackbar.setAction("Install") { view: View? ->
//            appUpdateManager.completeUpdate()
//        }
//        snackbar.setActionTextColor(ContextCompat.getColor(binding.root.context, uz.rttm.support.R.color.cl_color_primary))
//        snackbar.show()
//    }

    private lateinit var mFirestore: FirebaseFirestore
    private fun readDataFromFireStore() {
        mFirestore = FirebaseFirestore.getInstance()
        mFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        mFirestore.collection("version_info").document("version")
            .get()
            .addOnSuccessListener { document ->
                try {
                    if (document != null) {
                        val version = document.data?.get("version_code") as Long
                        val priority = document.data?.get("priority") as Long
                        if (version > BuildConfig.VERSION_CODE) {
                            checkUpdate(priority)
                        }
                    }
                } catch (ex: Exception) {
                    lg("Error -->" + ex.message.toString())
                }
            }.addOnFailureListener { e ->
                lg("Error writing document->$e")
            }
    }
}

