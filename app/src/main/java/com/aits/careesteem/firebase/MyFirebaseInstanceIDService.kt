/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.firebase

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessaging

class MyFirebaseInstanceIDService : MyFirebaseMessagingService() {
    private val TAG = MyFirebaseInstanceIDService::class.java.simpleName

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val refreshedToken = FirebaseMessaging.getInstance().token.toString()
        Log.d("TAG", "token in firebase instance class is $refreshedToken")
        storeRegIdInPref(refreshedToken)
        sendRegistrationToServer(refreshedToken)
        val registrationComplete = Intent(Config.REGISTRATION_COMPLETE)
        registrationComplete.putExtra("token", refreshedToken)
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete)
    }


    private fun sendRegistrationToServer(token: String) {
        // sending gcm token to server
        Log.d(TAG, "sendRegistrationToServer: $token")
    }

    private fun storeRegIdInPref(token: String) {
        val pref: SharedPreferences =
            applicationContext.getSharedPreferences(Config.SHARED_PREF, 0)
        val editor = pref.edit()
        editor.putString("regId", token)
        editor.apply()
    }
}