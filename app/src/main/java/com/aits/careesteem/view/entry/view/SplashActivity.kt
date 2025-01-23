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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aits.careesteem.MainActivity
import com.aits.careesteem.R
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.view.auth.view.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
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
//        if (sharedPreferences.getBoolean(Preference.SCREEN_LOCK, Const.FALSE) == Const.TRUE) {
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
        if (sharedPreferences.getBoolean(SharedPrefConstant.IS_LOGGED, AppConstant.FALSE)) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}