/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import org.json.JSONObject
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object AppConstant {
    // def Values
    const val TRUE: Boolean = true
    const val FALSE: Boolean = false
    const val YES: String = "Yes"
    const val NO: String = "No"
    const val ACTIVE: String = "Active"

    fun checkNull(value: String?): String {
        return if (value.isNullOrEmpty()) {
            "N/A"
        } else {
            value
        }
    }

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

    @SuppressLint("NewApi")
    fun visitNotesListTimer(input: String): String {
        try {
            // Parse the ISO date string to an Instant
            val instant = Instant.parse(input)

            // Define your desired output format.
            // Adjust the pattern as needed (here it is: day/month/year at hour:minute)
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm")
                .withZone(ZoneId.systemDefault()) // or use a specific zone like ZoneId.of("UTC")

            val formattedDate = outputFormatter.format(instant)
            return formattedDate
        } catch (e: Exception) {
            println(e)
            return "00/00/0000 at 00:00"
        }
    }

    @SuppressLint("NewApi")
    fun visitUvNotesListTimer(input: String): String {
        try {
            // Parse the string as an OffsetDateTime (since it has 'Z' timezone)
            val offsetDateTime = OffsetDateTime.parse(input)

            // Convert to LocalDateTime (ignoring UTC offset)
            val localDateTime = offsetDateTime.toLocalDateTime()

            // Define the output format
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm")

            // Format the date-time
            val formattedDate = localDateTime.format(formatter)
            return formattedDate
        } catch (e: Exception) {
            println(e)
            return "00/00/0000 at 00:00"
        }
    }

    fun base64ToBitmap(base64Str: String): Bitmap? {
        // Remove the prefix if it exists
        val pureBase64Encoded = base64Str.substringAfter("base64,")
        // Decode the Base64 string to a byte array
        val decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT)
        // Convert the byte array to a Bitmap
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun loadJsonFromAssets(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    fun getStatusesFromJson(context: Context): List<String> {
        val jsonString = loadJsonFromAssets(context, "medication_statuses.json")
        val jsonObject = JSONObject(jsonString)
        return jsonObject.getJSONArray("statuses").let { jsonArray ->
            List(jsonArray.length()) { jsonArray.getString(it) }
        }
    }
}