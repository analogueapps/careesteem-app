/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.utils

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object AppConstant {
    // def Values
    const val TRUE: Boolean = true
    const val FALSE: Boolean = false
    const val YES: String = "Yes"
    const val NO: String = "No"
    const val ACTIVE: String = "Active"

    fun maskPhoneNumber(phoneNumber: String): String {
        return if (phoneNumber.length > 2) {
            // Mask all characters except the last two
            val maskedPart = "*".repeat(phoneNumber.length - 2)
            val lastTwoDigits = phoneNumber.takeLast(2)
            "$maskedPart$lastTwoDigits"
        } else {
            // If the number is too short, return it as is
            phoneNumber
        }
    }


    @SuppressLint("NewApi")
    fun visitListTimer(input: String): String {
        try {
            // Parse the input string as a ZonedDateTime.
            val zonedDateTime = ZonedDateTime.parse(input)

            // Define a formatter for just the time portion.
            // For 24-hour format (e.g., "10:00")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

            // Alternatively, for 12-hour format with AM/PM (e.g., "10:00 AM"):
            // val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

            // Format the ZonedDateTime to extract only the time.
            val timeOnly = zonedDateTime.format(timeFormatter)

            // Output: 10:00 (or "10:00 AM" if using the alternate pattern)
            return timeOnly
        } catch (e: Exception) {
            return "00:00"
        }
    }
}