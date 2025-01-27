/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.firebase

class Config {
    // global topic to receive app wide push notifications
    companion object {
        val TOPIC_GLOBAL = "global"

        // broadcast receiver intent filters
        val REGISTRATION_COMPLETE = "registrationComplete"
        val PUSH_NOTIFICATION = "pushNotification"

        // id to handle the notification in the notification tray
        val NOTIFICATION_ID = 100
        val NOTIFICATION_ID_BIG_IMAGE = 101

        val SHARED_PREF = "ah_firebase"
    }
}