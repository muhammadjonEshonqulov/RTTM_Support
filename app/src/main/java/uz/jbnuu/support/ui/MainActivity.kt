package uz.jbnuu.support.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import uz.jbnuu.support.BuildConfig
import uz.jbnuu.support.R
import uz.jbnuu.support.databinding.ActivityMainBinding
import uz.jbnuu.support.ui.base.LogoutDialog
import uz.jbnuu.support.ui.base.UpdateDialog
import uz.jbnuu.support.utils.Prefs
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    @Inject
    lateinit var prefss:Prefs

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        val navGraph = navController.graph
        navController.graph = navGraph

        checkVersion()

    }

    private fun checkVersion() {
        if (BuildConfig.VERSION_CODE < prefss.get(prefss.versionCode,1)){
            val updateDialog = UpdateDialog(this)

            if (prefss.get( prefss.versionType, "0") == "0"){
                if (prefss.get( prefss.countEnter, 3) >= 3){
                    updateDialog.show()
                }
            } else {
                updateDialog.show()
            }
            updateDialog.setCancelable(false)
            updateDialog.setOnCancelClick {
                updateDialog.dismiss()
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.app_link))
                    )
                )
            }
            updateDialog.setOnSubmitClick {
                updateDialog.dismiss()
                if (prefss.get( prefss.versionType, "0") == "1"){
                    finish()
                } else {
                    prefss.save( prefss.countEnter, 0)
                }
            }
        }
    }
}