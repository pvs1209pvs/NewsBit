package com.param.newsbit.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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


    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navHostFrag)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun createNotificationChannel() {

        val channel = NotificationChannel(
            NewsNotificationService.NEWS_DOWNLOAD_CHANNEL,
            "Latest news ready",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "To inform when latest news is downloaded."

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        Log.i(TAG, "createNotificationChannel: notification channel created")

    }

}