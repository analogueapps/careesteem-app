/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.Html
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.aits.careesteem.R
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import androidx.core.net.toUri

const val FCM_CHANNEL_ID = "care_esteem_channel"

@Suppress("DEPRECATION")
class MyNotificationManager(private var mCtx: Context) {

    fun showBigNotification(title: String?, message: String?, url: String, intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val resultPendingIntent: PendingIntent? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(mCtx, 0, intent, PendingIntent.FLAG_MUTABLE)
            } else {
                PendingIntent.getActivity(mCtx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        val bigPictureStyle = NotificationCompat.BigPictureStyle()
        bigPictureStyle.setBigContentTitle(Html.fromHtml(title).toString())
        bigPictureStyle.setSummaryText(Html.fromHtml(message).toString())
        bigPictureStyle.bigPicture(getBitmapFromURL(url))
        val mBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(mCtx, FCM_CHANNEL_ID)
        val notification: Notification =
            mBuilder.setSmallIcon(R.drawable.logo_preview).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(Html.fromHtml(title).toString())
                .setContentText(Html.fromHtml(message).toString())
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setStyle(bigPictureStyle)
                .setSmallIcon(R.drawable.logo_preview)
//                .setLargeIcon(
//                    BitmapFactory.decodeResource(
//                        mCtx.resources,
//                        R.drawable.logo_preview
//                    )
//                )
                .setPriority(Notification.PRIORITY_HIGH)
                .build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        notification.defaults =
            Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE or Notification.FLAG_AUTO_CANCEL or Notification.DEFAULT_SOUND
        val notificationManager =
            mCtx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(notificationManager)
        }
        if (!notificationManager.areNotificationsEnabled()) {
            openNotificationSettings()
            return
        }
        val importance: Int = notificationManager.importance
        val soundAllowed =
            importance < 0 || importance >= NotificationManager.IMPORTANCE_DEFAULT
        if (!soundAllowed) {
            openNotificationSettings()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            isChannelBlocked(FCM_CHANNEL_ID)
        ) {
            openChannelSettings(FCM_CHANNEL_ID)
            return
        }
        notificationManager.notify(0, notification)
    }

    @RequiresApi(26)
    private fun isChannelBlocked(channelId: String): Boolean {
        val manager: NotificationManager = mCtx.getSystemService<NotificationManager>(
            NotificationManager::class.java
        )
        val channel = manager.getNotificationChannel(channelId)
        return channel != null &&
                channel.importance == NotificationManager.IMPORTANCE_NONE
    }

    @RequiresApi(26)
    private fun openChannelSettings(channelId: String) {
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, mCtx.packageName)
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
        mCtx.startActivity(intent)
    }

    fun showSmallNotification(title: String?, message: String?, intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val resultPendingIntent: PendingIntent? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(mCtx, 0, intent, PendingIntent.FLAG_MUTABLE)
            } else {
                PendingIntent.getActivity(
                    mCtx,
                    0,
                    intent,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        val mBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(mCtx, FCM_CHANNEL_ID)
        val notification: Notification =
            mBuilder.setSmallIcon(R.drawable.logo_preview).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(Html.fromHtml(title).toString())
                .setContentText(Html.fromHtml(message).toString())
                .setSmallIcon(R.drawable.logo_preview)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(resultPendingIntent)
//                .setLargeIcon(
//                    BitmapFactory.decodeResource(
//                        mCtx.resources,
//                        R.drawable.logo_preview
//                    )
//                )
                .setPriority(Notification.PRIORITY_HIGH)
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(Html.fromHtml(message).toString())
                )
                .build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        notification.defaults =
            Notification.DEFAULT_LIGHTS or Notification.DEFAULT_VIBRATE or Notification.FLAG_AUTO_CANCEL or Notification.DEFAULT_SOUND
        val notificationManager =
            mCtx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(notificationManager)
        }
        if (!notificationManager.areNotificationsEnabled()) {
            openNotificationSettings()
            return
        }
        val importance: Int = notificationManager.importance
        val soundAllowed =
            importance < 0 || importance >= NotificationManager.IMPORTANCE_DEFAULT
        if (!soundAllowed) {
            openNotificationSettings()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            isChannelBlocked(FCM_CHANNEL_ID)
        ) {
            openChannelSettings(FCM_CHANNEL_ID)
            return
        }
        notificationManager.notify(0, notification)
    }

    private fun openNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, mCtx.packageName)
            mCtx.startActivity(intent)
        } else {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = ("package:" + mCtx.packageName).toUri()
            mCtx.startActivity(intent)
        }
    }

    //The method will return Bitmap from an image URL
    private fun getBitmapFromURL(strURL: String): Bitmap? {
        return try {
            val url = URL(strURL)
            val connection =
                url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(notificationManager: NotificationManager) {
        val name: String = mCtx.getString(R.string.notification_channel_id)
        val description = "Notifications for download status"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(
            mCtx.getString(R.string.notification_channel_id),
            name,
            importance
        )
        mChannel.description = description
        mChannel.enableLights(true)
        mChannel.enableVibration(true)
        notificationManager.createNotificationChannel(mChannel)
    }
}