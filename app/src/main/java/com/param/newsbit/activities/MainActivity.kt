package com.param.newsbit.activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup.LayoutParams
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.layout.Layout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.param.newsbit.R
import com.param.newsbit.databinding.ActivityMainBinding
import com.param.newsbit.notifaction.NewsNotificationService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val hostFragment = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> R.id.listFrag
            Configuration.ORIENTATION_PORTRAIT -> R.id.navHostFrag
            else -> throw IllegalStateException("Invalid state, should be portrait of landscape.")
        }

        val navHost = supportFragmentManager.findFragmentById(hostFragment) as NavHostFragment
        navController = navHost.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.bottomNavigationView.apply {

            setupWithNavController(navController)

            setOnItemSelectedListener {
                if (navController.currentDestination?.id != it.itemId) {
                    navController.navigate(it.itemId, null, null)
                }
                true
            }

        }

        val bottomNavHeight = binding.bottomNavigationView.height
        val dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomNavHeight * 1f, resources.displayMetrics)
        Log.i(TAG, "onCreate: dp $bottomNavHeight")



//        setSupportActionBar(binding.toolbar)
//        setupActionBarWithNavController(navController, appBarConfiguration)


        createNotificationChannel()
        requestPermissions()

    }

    override fun onSupportNavigateUp() =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//        findNavController(R.id.navHostFrag).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()


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
            callback = { isGranted ->

                Log.i(
                    TAG,
                    "requestPermissions: ${Manifest.permission.POST_NOTIFICATIONS} $isGranted"
                )

                if (!isGranted) {
                    AlertDialog.Builder(this)
                        .setTitle("Notification Permission Denied")
                        .setMessage("You won't be notified when the new articles are available.")
                        .setPositiveButton("OK") { p0, p1 -> }
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
                    .setPositiveButton("OK") { p0, p1 -> }
                    .show()

            }

            else -> {
                notificationPermissionResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }

        }

    }

}