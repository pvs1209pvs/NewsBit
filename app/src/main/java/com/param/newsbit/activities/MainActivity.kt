package com.param.newsbit.activities

import android.os.Bundle
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private lateinit var appBarConfiguration: AppBarConfiguration

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val navController by lazy { (supportFragmentManager.findFragmentById(R.id.navHostFrag) as NavHostFragment).navController }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

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

}