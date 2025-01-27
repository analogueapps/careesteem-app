/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.home.view

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ActivityHomeBinding
import com.aits.careesteem.databinding.DialogLogoutBinding
import com.aits.careesteem.databinding.DialogUnscheduledVisitBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.auth.view.AuthActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var editor: SharedPreferences.Editor

    // Double back press variables
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom - systemBars.bottom)
            insets
        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_home) as NavHostFragment
        navController = navHostFragment.navController

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_HOME or
                ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_HOME_AS_UP or ActionBar.DISPLAY_USE_LOGO
        supportActionBar!!.setIcon(R.drawable.toolbar_logo)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.bottom_visits,
                R.id.bottom_clients,
                R.id.bottom_alerts
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        // Set up BottomNavigationView with NavController
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNav.setupWithNavController(navController)

        // Add a listener to handle item selection
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_visits -> {
                    // Navigate to visits fragment if not already open
                    if (navController.currentDestination?.id != R.id.bottom_visits) {
                        navController.navigate(R.id.bottom_visits)
                    }
                    true
                }
                R.id.bottom_clients -> {
                    // Navigate to clients fragment if not already open
                    if (navController.currentDestination?.id != R.id.bottom_clients) {
                        navController.navigate(R.id.bottom_clients)
                    }
                    true
                }
                R.id.bottom_alerts -> {
                    // Navigate to alerts fragment if not already open
                    if (navController.currentDestination?.id != R.id.bottom_alerts) {
                        navController.navigate(R.id.bottom_alerts)
                    }
                    true
                }
                else -> false
            }
        }

        // Request notification permissions (for Android 13 and above)
        requestNotificationPermissions()
    }

    // Inflate the menu resource
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Handle menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_notification -> {
                if (navController.currentDestination?.id != R.id.notificationsFragment) {
                    navController.navigate(R.id.notificationsFragment)
                }
                true
            }
            R.id.menu_profile -> {
                //showLogout()
                if (navController.currentDestination?.id != R.id.profileFragment) {
                    navController.navigate(R.id.profileFragment)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Handle back press for double tap to exit
    override fun onBackPressed() {
        // Check if the current destination is bottom_visits
        when (navController.currentDestination?.id) {
            R.id.bottom_visits -> {
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed() // Exit the app
                    return
                }

                // Show a toast message
                this.doubleBackToExitPressedOnce = true
                AlertUtils.showToast(this, "Press back again to exit")

                // Reset the flag after 2 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000) // 2 seconds delay
            }
            R.id.bottom_clients -> {
                navController.navigate(R.id.bottom_visits)
            }
            R.id.bottom_alerts -> {
                navController.navigate(R.id.bottom_visits)
            }
            else -> {
                // If not in bottom_visits, handle back navigation normally
                super.onBackPressed()
            }
        }
    }

    private fun requestNotificationPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            val hasPermission = ContextCompat.checkSelfPermission(this, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE_NOTIFICATION_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                AlertUtils.showLog("Notification Permission", "Notification permission granted.")
            } else {
                AlertUtils.showLog("Notification Permission", "Notification permission denied.")
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_NOTIFICATION_PERMISSION = 1001
    }
}