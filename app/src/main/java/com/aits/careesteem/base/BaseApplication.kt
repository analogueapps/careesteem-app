/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.WindowManager
import com.aits.careesteem.R
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.SharedPrefConstant
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import java.util.TimeZone
import javax.inject.Inject

@HiltAndroidApp
class BaseApplication : Application(), Application.ActivityLifecycleCallbacks {
    @Inject
    lateinit var editor: SharedPreferences.Editor

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate() {
        super.onCreate()

        // Set the default time zone to UK time
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"))

//        if (sharedPreferences.getBoolean(
//                SharedPrefConstant.IS_LOGGED,
//                AppConstant.FALSE
//            ) == AppConstant.TRUE
//        ) {
//            editor.putBoolean(SharedPrefConstant.SCREEN_LOCK, AppConstant.TRUE)
//            editor.apply()
//        } else {
//            editor.putBoolean(SharedPrefConstant.SCREEN_LOCK, AppConstant.FALSE)
//            editor.apply()
//        }

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_api_key))
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

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    // These are required overrides, but you can leave them empty
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    override fun attachBaseContext(newBase: Context) {
        val configuration = newBase.resources.configuration
        if (configuration.fontScale != 1f) {
            configuration.fontScale = 1f
            val context = newBase.createConfigurationContext(configuration)
            super.attachBaseContext(context)
        } else {
            super.attachBaseContext(newBase)
        }
    }
}