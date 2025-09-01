/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.home.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.aits.careesteem.R
import com.aits.careesteem.base.BaseActivity
import com.aits.careesteem.databinding.ActivityHomeBinding
import com.aits.careesteem.databinding.DialogConfirmExitBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.GooglePlaceHolder
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.notification.viewmodel.NotificationViewModel
import com.aits.careesteem.view.visits.viewmodel.VisitsViewModel
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.getValue


@AndroidEntryPoint
class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var editor: SharedPreferences.Editor

    // Double back press variables
    private var doubleBackToExitPressedOnce = false

    private val notificationViewModel: NotificationViewModel by viewModels()

    private lateinit var appUpdateManager: AppUpdateManager
    private val UPDATE_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Force show status bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Extend layout to draw behind status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Make status bar transparent
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        // Set status bar icons to dark (black)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            view.setPadding(
                0,
                insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
                0,
                0
            )
            insets
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_home) as NavHostFragment
        navController = navHostFragment.navController

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_HOME or
                    ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_HOME_AS_UP or ActionBar.DISPLAY_USE_LOGO
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setLogo(null) // Ensure default logo is removed
        }

        // Remove *all* insets and padding from toolbar
        //toolbar.setContentInsetsAbsolute(0, 0)
        //toolbar.setPadding(0, 0, 0, 0)

        val scale = resources.displayMetrics.density
        val logoWidthPx = 132
        val logoHeightPx = 40

        val logoImageView = ImageView(this).apply {
            setImageResource(R.drawable.toolbar_logo)

            layoutParams = Toolbar.LayoutParams(
                (logoWidthPx * scale).toInt(),
                (logoHeightPx * scale).toInt()
            ).apply {
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                // no marginStart here
            }

            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        // Add the custom logo to the toolbar
        toolbar.addView(logoImageView)


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
                    if (navController.currentDestination?.id == R.id.addAlertsFragment) {
                        showConfirmAlert {
                            navController.navigate(R.id.bottom_visits)
                        }
                        return@setOnItemSelectedListener false
                    } else if (navController.currentDestination?.id != R.id.bottom_visits) {
                        navController.navigate(R.id.bottom_visits)
                    }
                    return@setOnItemSelectedListener true
                }

                R.id.bottom_clients -> {
                    // Navigate to clients fragment if not already open
                    if (navController.currentDestination?.id == R.id.addAlertsFragment) {
                        showConfirmAlert {
                            navController.navigate(R.id.bottom_clients)
                        }
                        return@setOnItemSelectedListener false
                    } else if (navController.currentDestination?.id != R.id.bottom_clients) {
                        navController.navigate(R.id.bottom_clients)
                    }
                    return@setOnItemSelectedListener true
                }

                R.id.bottom_alerts -> {
                    // Navigate to alerts fragment if not already open
                    if (navController.currentDestination?.id == R.id.addAlertsFragment) {
                        showConfirmAlert {
                            navController.navigate(R.id.bottom_alerts)
                        }
                        return@setOnItemSelectedListener false
                    } else if (navController.currentDestination?.id != R.id.bottom_alerts) {
                        navController.navigate(R.id.bottom_alerts)
                    }
                    return@setOnItemSelectedListener true
                }

                else -> return@setOnItemSelectedListener false
            }
        }

        // Request notification permissions (for Android 13 and above)
        requestNotificationPermissions()

        // handle Viewmodels
        handleViewModel()

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForUpdate()
    }

//    private fun checkForUpdate() {
//        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
//
//        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
//            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
//                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) // or FLEXIBLE
//            ) {
//                // Start update
//                appUpdateManager.startUpdateFlowForResult(
//                    appUpdateInfo,
//                    AppUpdateType.IMMEDIATE,  // or FLEXIBLE
//                    this,
//                    UPDATE_REQUEST_CODE
//                )
//            }
//        }
//    }

    @SuppressLint("NewApi")
    private fun checkForUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            val pkgInfo = packageManager.getPackageInfo(packageName, 0)
            val currentVersionCode = pkgInfo.longVersionCode
            val currentVersionName = pkgInfo.versionName

            val availableVersionCode = appUpdateInfo.availableVersionCode()

            Log.d("AppUpdateCheck", "Current Version Name: $currentVersionName")
            Log.d("AppUpdateCheck", "Current Version Code: $currentVersionCode")
            Log.d("AppUpdateCheck", "Play Store Version Code: $availableVersionCode")

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            }
        }.addOnFailureListener { e ->
            Log.e("AppUpdateCheck", "Failed to check update: ${e.message}")
        }
    }

    private val updateActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            Log.d("AppUpdateCheck", "Update flow failed or canceled by user")
        }
    }


    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                Log.d("AppUpdateCheck", "Update downloaded, completing installation...")
                appUpdateManager.completeUpdate()
            }
        }
    }


    // Inflate the menu resource
    @SuppressLint("InflateParams")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val menuItem = menu.findItem(R.id.menu_profile)
        val scale = resources.displayMetrics.density
        val profileSizePx = 40

        val profileImageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                (profileSizePx * scale).toInt(),
                (profileSizePx * scale).toInt()
            )

            scaleType = ImageView.ScaleType.CENTER_CROP
            isClickable = true
            isFocusable = true
        }

        val gson = Gson()
        val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
        val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)

        val savedPhoto = sharedPreferences.getString(SharedPrefConstant.PROFILE_IMAGE, null)
        //val savedName = sharedPreferences.getString(SharedPrefConstant.PROFILE_NAME, "User")

        println(savedPhoto)

        if (!savedPhoto.isNullOrEmpty()) {
//            AppConstant.base64ToBitmap(savedPhoto)?.let { bitmap ->
//                val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap).apply {
//                    isCircular = true
//                }
//                profileImageView.setImageDrawable(roundedDrawable)
//            }
            Glide.with(this)
                .load(savedPhoto)
                .override(400, 300)
                
                .error(R.drawable.logo_preview)
                .circleCrop() // Makes the image circular
                .into(profileImageView)
        } else {
            if (userData != null) {
                val initials =
                    GooglePlaceHolder().getInitialsDouble(userData.first_name, userData.last_name)
                val initialsBitmap = GooglePlaceHolder().createInitialsAvatar(this, initials)
                profileImageView.setImageBitmap(initialsBitmap)
            }
        }

        menuItem.actionView = profileImageView

        profileImageView.setOnClickListener {
            onOptionsItemSelected(menuItem)
        }

        val notificationMenuItem = menu.findItem(R.id.menu_notification)
        val actionView = layoutInflater.inflate(R.layout.menu_notification_badge, null)

        val badgeTextView = actionView.findViewById<TextView>(R.id.badge)
        // Read count from SharedPreferences
        val notificationCount = sharedPreferences.getString(SharedPrefConstant.NOTIFICATION_COUNT, "0") ?: "0"

        if ((notificationCount.toIntOrNull() ?: 0) > 0) {
            badgeTextView.visibility = View.VISIBLE
            badgeTextView.text = if (notificationCount.toInt() > 99) "99+" else notificationCount
        } else {
            badgeTextView.visibility = View.GONE
        }

        // Set click listener to propagate click to menu item
        actionView.setOnClickListener {
            onOptionsItemSelected(notificationMenuItem)
        }

        // Set custom view
        notificationMenuItem.actionView = actionView

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
                AlertUtils.showToast(this, "Press back again to exit", ToastyType.INFO)

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
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                permission
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    REQUEST_CODE_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(visitRefreshReceiver,
                IntentFilter("com.aits.careesteem.ACTION_VISIT_REFRESH")
            )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(visitRefreshReceiver)
    }

    private val visitRefreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val title = intent?.getStringExtra("title")
            val message = intent?.getStringExtra("message")
            AlertUtils.showLog("HomeActivity", "Received visit refresh: $title - $message")
            // Also refresh notifications when activity starts
            notificationViewModel.getNotificationList(this@HomeActivity)
        }
    }

    private fun handleViewModel() {
        // notificationViewModel.notificationList and list count after that menu icon refreshed
        notificationViewModel.notificationList.observe(this) {
            // toolbar menu refresh
            invalidateOptionsMenu()
        }
    }

    private fun showConfirmAlert(onConfirmed: () -> Unit) {
        val dialog = Dialog(this)
        val binding: DialogConfirmExitBinding = DialogConfirmExitBinding.inflate(layoutInflater)

        dialog.window?.setDimAmount(0.8f)
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        binding.btnPositive.setOnClickListener {
            dialog.dismiss()
            onConfirmed() // call the callback
        }

        binding.btnNegative.setOnClickListener {
            dialog.dismiss()
            // do nothing
        }

        dialog.show()
    }
}
