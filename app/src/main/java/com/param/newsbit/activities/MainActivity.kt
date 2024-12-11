package com.param.newsbit.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.param.newsbit.R
import com.param.newsbit.databinding.ActivityMainBinding
import com.param.newsbit.notifaction.NewsNotificationService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val navController by lazy { (supportFragmentManager.findFragmentById(R.id.navHostFrag) as NavHostFragment).navController }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        createNotificationChannel()

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.bottomNavigationView.apply {

            setupWithNavController(navController)

            setOnItemSelectedListener {
                if (navController.currentDestination?.id != it.itemId) {
                    navController.navigate(it.itemId, null, null)
                }
                true
            }

        }

        requestPermissions()


    }

    override fun onSupportNavigateUp() =
        findNavController(R.id.navHostFrag).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()


    private fun createNotificationChannel() {

        val channel = NotificationChannel(
            NewsNotificationService.NEWS_DOWNLOAD_CHANNEL,
            "Latest news ready",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "To inform when latest news is downloaded."

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        Log.i(TAG, "createNotificationChannel: notification channel created")

    }


    private fun requestPermissions() {

        val hasNotificationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        val notificationPermissionResultLauncher = registerForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            callback = {isGranted ->

                Log.i(TAG, "requestPermissions: ${Manifest.permission.POST_NOTIFICATIONS} $isGranted")

                if(!isGranted){
                    AlertDialog.Builder(this)
                        .setTitle("Notification Permission Denied")
                        .setMessage("You won't be notified when the new articles are available.")
                        .setPositiveButton("OK") { p0, p1 ->  }
                        .show()
                }


            }
        )

        when {

            hasNotificationPermission -> {}

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) -> {

                AlertDialog.Builder(this)
                    .setTitle("Request Notification permission")
                    .setMessage("Enable Notification permission to be notified about new articles available.")
                    .setPositiveButton("OK") { p0, p1 ->  }
                    .show()

            }

            else -> {
                notificationPermissionResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }

        }

    }

}