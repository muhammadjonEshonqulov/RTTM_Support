package uz.rttm.support.ui

import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.gms.ads.AdView
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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


    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }
    private val MYREQUESTCODE = 100

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        readDataFromFireStore()
    }

    private val listener = InstallStateUpdatedListener { installState ->
        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            notifyUser()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(1000)

            try {
                val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val pData = clipboardManager.primaryClip
                val item = pData?.getItemAt(0)
                val txtPaste = item?.text?.toString() ?: "No text found on clipboard"
                if (txtPaste.startsWith("additional#")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        clipboardManager.clearPrimaryClip()
                    }
                    val navController = findNavController(R.id.nav_host_fragment)
                    navController.navigate(R.id.sendApplicationFragment, bundleOf("MyKeyTech" to txtPaste))
                }

            } catch (e: Exception) {
                lg("clipboardManager error -> $e")
            }
        }

    }

    override fun onDestroy() {
        mAdView.destroy()
        super.onDestroy()
    }

    private fun checkUpdate(priority: Long) {
        appUpdateManager.registerListener(listener)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                when (priority) {
                    0L -> {
                        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this, MYREQUESTCODE)
                    }

                    1L -> {
                        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, MYREQUESTCODE)
                    }
                }
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

