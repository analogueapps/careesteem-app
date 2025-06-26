/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.entry.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aits.careesteem.R
import com.aits.careesteem.base.BaseActivity
import com.aits.careesteem.utils.BiometricAuthListener
import com.aits.careesteem.view.auth.view.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity(), BiometricAuthListener {
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var editor: SharedPreferences.Editor

    private val splashTimeOut: Long = 2500 // 2.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Start the coroutine for the splash screen
        CoroutineScope(Dispatchers.Main).launch {
            delay(splashTimeOut)
            verifyPassword()
        }
    }

    private fun verifyPassword() {
//        if (sharedPreferences.getBoolean(SharedPrefConstant.SCREEN_LOCK, AppConstant.FALSE) == AppConstant.TRUE) {
//            BiometricUtils.showBiometricPrompt(
//                activity = this,
//                listener = this,
//                cryptoObject = null,
//            )
//        } else {
        navigation()
//        }
    }

    private fun navigation() {
//        if (sharedPreferences.getBoolean(SharedPrefConstant.IS_LOGGED, AppConstant.FALSE) == AppConstant.TRUE) {
//            val intent = Intent(this, HomeActivity::class.java)
//            startActivity(intent)
//            finish()
//        } else {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
//        }
    }

    override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
        when (error) {
            BiometricPrompt.ERROR_USER_CANCELED -> finish()
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {

            }
        }
    }

    override fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
        navigation()
    }
}