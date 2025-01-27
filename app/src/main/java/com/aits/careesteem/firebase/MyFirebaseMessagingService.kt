/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.firebase

import android.content.Intent
import android.util.Log
import com.aits.careesteem.view.home.view.HomeActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

open class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMsgService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("MyFirebase", "onNewToken: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            try {
                remoteMessage.notification
                val data = remoteMessage.data
                Log.d(TAG, data.toString())
                Log.d(
                    TAG,
                    "onMessageReceived: DATA" + remoteMessage.notification
                )
                sendPushNotification(data)
            } catch (e: Exception) {
                Log.d(TAG, "Exception: " + e.message)
            }
        } else {
            Log.d(TAG, remoteMessage.from!!)
        }

        super.onMessageReceived(remoteMessage)

    }

    private fun sendPushNotification(data: Map<String, String?>) {
        try {
            val title = data["title"]
            val message = data["body"]
            val imageUrl = data["image"]

            val intent: Intent?
            intent = Intent(applicationContext, HomeActivity::class.java)

            val mNotificationManager = MyNotificationManager(applicationContext)
            if (imageUrl == "null" || imageUrl == "") {
                mNotificationManager.showSmallNotification(title, message, intent)
            } else {
                mNotificationManager.showBigNotification(title, message, imageUrl!!, intent)
            }
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "sendPushNotification Exception: " + e.message)
        }
    }
}