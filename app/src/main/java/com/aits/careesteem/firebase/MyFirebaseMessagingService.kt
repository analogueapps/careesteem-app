package com.aits.careesteem.firebase

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.view.home.view.HomeActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMsgService"

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("MyFirebase", "onNewToken: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            val isNotificationEnabled = sharedPreferences.getBoolean(
                SharedPrefConstant.NOTIFICATION_ENABLE,
                true
            )

            if (!isNotificationEnabled) {
                Log.d("FCM", "Notifications disabled by user")
                return
            }
            try {
                val data = remoteMessage.data
                sendPushNotification(data)
            } catch (e: Exception) {
                Log.d(TAG, "Exception: ${e.message}")
            }
        }

        super.onMessageReceived(remoteMessage)
    }

    private fun sendPushNotification(data: Map<String, String?>) {
        try {
            val title = data["title"]
            val message = data["body"]

            // Send local broadcast
            val intent = Intent("com.aits.careesteem.ACTION_VISIT_REFRESH")
            intent.putExtra("title", title)
            intent.putExtra("message", message)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

            // Optional: show small notification
            val launchIntent = Intent(applicationContext, HomeActivity::class.java)
            val mNotificationManager = MyNotificationManager(applicationContext)
            mNotificationManager.showSmallNotification(title, message, launchIntent)

        } catch (e: Exception) {
            Log.d(TAG, "sendPushNotification Exception: ${e.message}")
        }
    }
}
