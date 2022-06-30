package uz.rttm.support.ui

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType.FLEXIBLE
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import uz.rttm.support.R
import uz.rttm.support.databinding.ActivityMainBinding
import uz.rttm.support.utils.Prefs
import uz.rttm.support.utils.lg
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var prefss: Prefs
    private var appUpdateManager: AppUpdateManager? = null
    private val APP_UPDATE = 100


    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        val navGraph = navController.graph
        navController.graph = navGraph


//        checkVersion()
        if (appUpdateManager == null) {
            appUpdateManager = AppUpdateManagerFactory.create(this)
        }

        appUpdateManager?.appUpdateInfo?.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                appUpdateManager?.startUpdateFlowForResult(it, AppUpdateType.FLEXIBLE, this, APP_UPDATE)
            }
        }
        appUpdateManager?.registerListener(installStateUpdatedListener)
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener {
        if (it.installStatus() == InstallStatus.DOWNLOADED) {
            showCompletedUpdate()
        }
    }

    private fun showCompletedUpdate() {
        val snackbar = Snackbar.make(binding.root, "Ilova yangilashga tayyor", Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("O'rnatish") {
            appUpdateManager?.completeUpdate()
        }
        snackbar.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == APP_UPDATE && resultCode != RESULT_OK) {
            Toast.makeText(this, "Ilova yangilanishi keyinroqqa qoldirildi ", Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        if (appUpdateManager != null) {
            appUpdateManager?.unregisterListener(installStateUpdatedListener)
        }
        super.onStop()

    }
//    private fun checkVersion() {
//        if (BuildConfig.VERSION_CODE < prefss.get(prefss.versionCode, 1) + 1) {
//            val updateDialog = UpdateDialog(this)
//
//            if (prefss.get(prefss.versionType, 0) == 0) {
//                if (prefss.get(prefss.countEnter, 3) >= 3) {
//                    updateDialog.show()
//                }
//            } else {
//                updateDialog.show()
//            }
//            updateDialog.setCancelable(false)
//            updateDialog.setOnCancelClick {
//                updateDialog.dismiss()
//                startActivity(
//                    Intent(
//                        Intent.ACTION_VIEW,
//                        Uri.parse(getString(R.string.app_link))
//                    )
//                )
//            }
//            updateDialog.setOnSubmitClick {
//                updateDialog.dismiss()
//                if (prefss.get(prefss.versionType, 0) == 1) {
//                    finish()
//                } else {
//                    prefss.save(prefss.countEnter, 0)
//                }
//            }
//        }
//    }
}