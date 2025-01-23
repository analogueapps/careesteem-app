/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
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

        // Initialize Firebase
        //FirebaseApp.initializeApp(this)
        // Get FCM token
        getFCMToken()
    }

    private fun getFCMToken() {
//        FirebaseMessaging.getInstance().token
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val token = task.result
//                    AlertUtils.showLog("FCM Token", "Token: $token")
//                    editor.putString(Preference.FCM_TOKEN, token)
//                    editor.apply()
//                } else {
//                    AlertUtils.showLog("FCM Token", "Error getting token: ${task.exception}")
//                }
//            }
    }
}