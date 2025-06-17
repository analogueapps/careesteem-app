package com.aits.careesteem.view.notification.model

data class NotificationId(
    val id: String
)

data class ClearNotificationRequest(
    val notificationStatus: List<NotificationId>
)
