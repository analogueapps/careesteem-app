/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.view.home.view.HomeActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BaseApplication : Application() {
    @Inject
    lateinit var editor: SharedPreferences.Editor

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate() {
        super.onCreate()

//        val isDarkModeEnabled = sharedPreferences.getBoolean(Preference.THEME, Const.FALSE)
//        AppCompatDelegate.setDefaultNightMode(
//            if (isDarkModeEnabled) {
//                AppCompatDelegate.MODE_NIGHT_YES
//            } else {
//                AppCompatDelegate.MODE_NIGHT_NO
//            }
//        )

        if (sharedPreferences.getBoolean(SharedPrefConstant.IS_LOGGED, AppConstant.FALSE) == AppConstant.TRUE) {
            editor.putBoolean(SharedPrefConstant.SCREEN_LOCK, AppConstant.TRUE)
            editor.apply()
        } else {
            editor.putBoolean(SharedPrefConstant.SCREEN_LOCK, AppConstant.FALSE)
            editor.apply()
        }

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        // Get FCM token
        getFCMToken()
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    AlertUtils.showLog("FCM Token", "Token: $token")
                    editor.putString(SharedPrefConstant.FCM_TOKEN, token)
                    editor.apply()
                } else {
                    AlertUtils.showLog("FCM Token", "Error getting token: ${task.exception}")
                }
            }
    }
}