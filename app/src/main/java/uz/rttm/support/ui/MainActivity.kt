package uz.rttm.support.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import uz.rttm.support.BuildConfig
import uz.rttm.support.R
import uz.rttm.support.databinding.ActivityMainBinding
import uz.rttm.support.utils.Prefs
import uz.rttm.support.utils.snack
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var prefss: Prefs

    //    private var appUpdateManager: AppUpdateManager? = null
    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }
    private val MYREQUESTCODE = 100
//    private var mAppUpdateManager: AppUpdateManager? = null

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fakeAppUpdateManager = FakeAppUpdateManager(binding.root.context)
        fakeAppUpdateManager.setUpdateAvailable(1)

        fakeAppUpdateManager.userAcceptsUpdate()
        fakeAppUpdateManager.downloadStarts()
        fakeAppUpdateManager.downloadCompletes()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        val navGraph = navController.graph
        navController.graph = navGraph

//        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkUpdate()
    }

    private fun checkUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
              snack(binding.root, "Build version -> " + BuildConfig.VERSION_CODE + "\navailableVersionCode  -> ${ appUpdateInfo.availableVersionCode() == UpdateAvailability.UPDATE_AVAILABLE }"  )

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) { // && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                appUpdateManager.registerListener(listener)
                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE))
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this, MYREQUESTCODE)
                else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE))
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, MYREQUESTCODE)
            }
        }
    }

    private val listener: InstallStateUpdatedListener = InstallStateUpdatedListener { installState ->
        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            showSnackBarForCompleteUpdate()
        }
    }

    private fun showSnackBarForCompleteUpdate() {
        val snackbar = Snackbar.make(
            binding.root, "New app is ready!", Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction("Install") { view: View? ->
            appUpdateManager.completeUpdate()
        }
        snackbar.setActionTextColor(ContextCompat.getColor(binding.root.context, R.color.cl_color_primary))
        snackbar.show()
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, IMMEDIATE, this, MYREQUESTCODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MYREQUESTCODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                }
                Activity.RESULT_CANCELED -> {
                    checkUpdate()
                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                    checkUpdate()

                }
            }
        }
    }

    override fun onStop() {
        appUpdateManager.unregisterListener(listener)
        super.onStop()

    }
}