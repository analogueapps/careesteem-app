package com.aits.careesteem.view.auth.view

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ActivityAuthBinding
import com.aits.careesteem.databinding.DialogExitBinding
import com.aits.careesteem.databinding.DialogLogoutBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.SharedPrefConstant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

        if (sharedPreferences.getBoolean(SharedPrefConstant.IS_LOGGED, AppConstant.FALSE) == AppConstant.TRUE) {
            navController.navigate(R.id.enterPasscodeFragment)
        }
    }

    override fun onBackPressed() {
        if (navController.currentDestination!!.id == R.id.verifyOtpFragment) {
            navController.navigate(R.id.welcomeFragment)
        } else {
            val dialog = Dialog(this)
            val binding: DialogExitBinding =
                DialogExitBinding.inflate(layoutInflater)
            dialog.window?.setDimAmount(0.8f)
            dialog.setContentView(binding.root)
            dialog.setCancelable(AppConstant.FALSE)

            // Handle button clicks
            binding.btnPositive.setOnClickListener {
                dialog.dismiss()
            }
            binding.btnNegative.setOnClickListener {
                dialog.dismiss()
                finishAffinity()
            }

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val window = dialog.window
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialog.show()
        }
    }
}