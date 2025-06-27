package com.aits.careesteem.utils

import android.annotation.SuppressLint
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

object DateTimeUtils {

    //private val gmtTimeZone: TimeZone = TimeZone.getTimeZone("GMT")
    private val gmtTimeZone: TimeZone = TimeZone.getTimeZone("Europe/London")

    /**
     * Get current date in GMT with format "yyyy-MM-dd"
     */
    fun getCurrentDateGMT(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = gmtTimeZone
        }
        return dateFormat.format(Date())
    }

    /**
     * Get current time in GMT with format "HH:mm"
     */
    fun getCurrentTimeGMT(): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
            timeZone = gmtTimeZone
        }
        return timeFormat.format(Date())
    }

    /**
     * Get current time in GMT with format "HH:mm:ss"
     */
    fun getCurrentTimeWithSecGMT(): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
            timeZone = gmtTimeZone
        }
        return timeFormat.format(Date())
    }

    /**
     * Get current time in GMT with format "HH:mm:ss"
     */
    fun getCurrentTimeInSecGMT(): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
            timeZone = gmtTimeZone
        }
        return timeFormat.format(Date())
    }

    /**
     * Get current time in GMT with format "HH:mm"
     */
    fun getCurrentTimeAndSecGMT(): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
            timeZone = gmtTimeZone
        }
        return timeFormat.format(Date())
    }

    /**
     * Get current timestamp in GMT with format "yyyy-MM-dd HH:mm:ss"
     */
    fun getCurrentTimestampGMT(): String {
        val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = gmtTimeZone
        }
        return timestampFormat.format(Date())
    }

    /**
     * Get current timestamp in GMT with format "yyyy-MM-dd HH:mm:ss"
     */
    fun getCurrentTimestampForCheckOutGMT(): String {
        val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = gmtTimeZone
        }
        return timestampFormat.format(Date())
    }

    fun getCurrentTimestampAddVisitNotesGMT(): String {
        val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = gmtTimeZone
        }
        return timestampFormat.format(Date())
    }

    /**
     * Convert a given date to GMT format
     * @param date - Date to be converted
     * @param format - Desired format (default: "yyyy-MM-dd HH:mm:ss")
     */
    fun convertToGMT(date: Date, format: String = "yyyy-MM-dd HH:mm:ss"): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault()).apply {
            timeZone = gmtTimeZone
        }
        return dateFormat.format(date)
    }

    // convert HH:mm:ss to HH:ss
    fun convertTime(time: String): String {
        val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(time)
        return outputFormat.format(date)
    }

    /**
     * Starts a countdown timer until the provided [plannedEndTimeStr].
     *
     * @param plannedEndTimeStr The ISO 8601 string for the planned end time.
     * @param onTick A callback invoked every second with the remaining time formatted
     * as "mm:ss". When the countdown is finished, it will be updated to "Time's up!".
     * @return The Job representing the coroutine timer.
     */
    @SuppressLint("NewApi", "DefaultLocale")
    fun startCountdownTimer(
        plannedDateStr: String,  // "yyyy-MM-dd"
        plannedTimeStr: String,  // "HH:mm:ss"
        onTick: (String) -> Unit
    ): Job {
        // Get the current UTC time
        val currentUtcTime = ZonedDateTime.now(ZoneOffset.UTC)

        println("Current UTC time: $currentUtcTime")

        // Convert provided date and time into a ZonedDateTime in UTC.
        val plannedDateTime = ZonedDateTime.of(
            LocalDate.parse(plannedDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            LocalTime.parse(plannedTimeStr, DateTimeFormatter.ofPattern("HH:mm:ss")),
            ZoneId.of("Europe/London") // Ensure it's UTC
        )

        // Convert ZonedDateTime to Instant for calculations
        val plannedEndTime = plannedDateTime.toInstant()

        return CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val now = Instant.now()

                // Calculate the remaining time
                val remaining = Duration.between(plannedEndTime, now)

                // If the remaining time is negative, zero, or greater than 10 hours, stop
                if (remaining.isNegative || remaining.isZero || remaining.toHours() > 10) {
                    onTick("Time's up!")
                    break
                }

                val minutes = remaining.toMinutes()
                val seconds = remaining.seconds % 60

                // Format the remaining time as "mm:ss".
                onTick(String.format("%02d:%02d", minutes, seconds))

                delay(1000L)  // Wait for 1 second before checking the time again
            }
        }
    }
}
