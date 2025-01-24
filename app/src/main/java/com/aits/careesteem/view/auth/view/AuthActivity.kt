package com.aits.careesteem.view.auth.view

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_auth) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun onBackPressed() {
        if (navController.currentDestination!!.id == R.id.verifyOtpFragment) {
            navController.navigate(R.id.welcomeFragment)
        }
//        if (navController.currentDestination!!.id == R.id.mobileAuthFragment) {
//            finishAffinity()
//        } else if (navController.currentDestination!!.id == R.id.otpAuthFragment) {
//            navController.navigate(R.id.mobileAuthFragment)
//        } else if (navController.currentDestination!!.id == R.id.passcodeCreateFragment) {
//            finishAffinity()
//        } else if (navController.currentDestination!!.id == R.id.passcodeAuthFragment) {
//            navController.navigate(R.id.mobileAuthFragment)
//        } else if (navController.currentDestination!!.id == R.id.profileAuthFragment) {
//            finishAffinity()
//        } else if (navController.currentDestination!!.id == R.id.webViewFragment) {
//            navController.navigate(R.id.mobileAuthFragment)
//        }
    }
}