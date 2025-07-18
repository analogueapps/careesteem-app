/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Base64
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.aits.careesteem.R
import com.aits.careesteem.view.auth.model.CountryList
import com.aits.careesteem.view.visits.model.MedicationStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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

    fun checkClientName(value: String?): String {
        return if (value.isNullOrEmpty()) {
            "N/A"
        } else {
            //value.split(" ").last()
            value
        }
    }

    fun checkClientFirstName(value: String?): String {
        return if (value.isNullOrEmpty()) {
            "N/A"
        } else {
            //value.split(" ").first()
            value
        }
    }

    fun checkUserLastName(value: String?): String {
        return if (value.isNullOrEmpty()) {
            "N/A"
        } else {
            value.split(" ").last()
        }
    }

    fun checkNullOrNull(value: String?): String {
        return if (value.isNullOrEmpty()) {
            "N/A"
        } else {
            value
        }
    }

    fun checkAge(value: Int): String {
        return if (value > 0) {
            value.toString()
        } else {
            "N/A"
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
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm")

            val localDateTime = LocalDateTime.parse(input, inputFormatter)

            outputFormatter.format(localDateTime)
        } catch (e: Exception) {
            e.printStackTrace()
            "00/00/0000 at 00:00"
        }
    }

    @SuppressLint("NewApi")
    fun visitUvNotesListTimer(input: String): String {
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm")

            val localDateTime = LocalDateTime.parse(input, inputFormatter)

            outputFormatter.format(localDateTime)
        } catch (e: Exception) {
            e.printStackTrace()
            "00/00/0000 at 00:00"
        }
    }

    @SuppressLint("NewApi")
    fun alertsListTimer(input: String): String {
        return try {
            // Define input and output formatters
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

            // Parse the input string
            val localDateTime = LocalDateTime.parse(input, inputFormatter)

            // Format and return
            localDateTime.format(outputFormatter)
        } catch (e: Exception) {
            e.printStackTrace()
            "00/00/0000 at 00:00"
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

    fun getNewStatuses(context: Context): List<MedicationStatus> {
        return listOf(
            MedicationStatus("med_fully_taken", "Fully Taken"),
            MedicationStatus("med_pref_left", "Prepared & Left Out"),
            MedicationStatus("med_not_taken", "Not Taken"),
            MedicationStatus("med_missing", "Missing Medication"),
            MedicationStatus("med_destroyed", "Destroyed"),
            MedicationStatus("med_self_admin", "Self Administered"),
            MedicationStatus("med_not_observed", "Not Observed"),
            MedicationStatus("med_refused", "Refused"),
            MedicationStatus("med_not_given", "Not Given"),
            MedicationStatus("med_no_visit", "No Visit"),
            MedicationStatus("med_others", "Other"),
            MedicationStatus("med_part_taken", "Partially Taken")
        )
    }

    fun getCountryList(context: Context): List<CountryList> {
        val jsonString = loadJsonFromAssets(context, "country.json")
        // Parse JSON string into a list of Country objects
        val gson = Gson()
        val countryListType = object : TypeToken<List<CountryList>>() {}.type
        val countries: List<CountryList> = gson.fromJson(jsonString, countryListType)
        return countries
    }

    @SuppressLint("NewApi", "DefaultLocale")
    private fun startCountdownTimer(
        plannedEndTimeStr: String,
        onTick: (String) -> Unit
    ): Job {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

        // Parse planned end time as LocalTime
        val plannedEndTime = LocalTime.parse(plannedEndTimeStr, timeFormatter)

        // Get today's date and combine it with planned end time
        val plannedDateTime = LocalDateTime.of(LocalDate.now(), plannedEndTime)

        // Convert to Instant (Assuming UTC or system default timezone)
        val plannedEndInstant = plannedDateTime.atZone(ZoneId.systemDefault()).toInstant()

        return CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val now = Instant.now()
                val remaining = Duration.between(now, plannedEndInstant)

                if (remaining.isZero || remaining.isNegative) {
                    onTick("Time's up!")
                    break
                }

                val minutes = remaining.toMinutes()
                val seconds = remaining.seconds % 60

                // Format the remaining time as "mm:ss".
                onTick(String.format("%02d:%02d", minutes, seconds))

                delay(1000L)
            }
        }
    }

    fun bitmapToFile(context: Context, bitmap: Bitmap, fileName: String): File? {
        val file = File(context.cacheDir, fileName)
        return try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
        // Get the path to the Pictures directory
    }

    fun createRequestBody(value: String?): RequestBody {
        return value?.toRequestBody("text/plain".toMediaTypeOrNull())
            ?: "".toRequestBody("text/plain".toMediaTypeOrNull())
    }

    fun createMultipartBodyParts(files: List<File>, context: Context): List<MultipartBody.Part> {
        return files.mapNotNull { file ->
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("images", file.name, requestFile)
        }
    }

    private fun createMultipartBodyPart(
        name: String,
        uri: Uri?,
        activity: Activity
    ): MultipartBody.Part? {
        val file = uri?.let { uriToFile(activity, it) }
        return file?.let {
            val requestFile = it.asRequestBody(MultipartBody.FORM)
            MultipartBody.Part.createFormData(name, it.name, requestFile)
        }
    }

    fun isImageFile(context: Context, uri: Uri): Boolean {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        return mimeType?.startsWith("image/") == true // Ensures only images are processed
    }

    fun uriToFile(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            getFileNameFromUri(context, uri).toString()
        )

        return try {
            val outputStream: OutputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var bytesRead: Int

            inputStream?.use { input ->
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
            outputStream.flush()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream?.close()
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) {
                fileName = it.getString(nameIndex)
            }
        }
        return fileName
    }

    @SuppressLint("NewApi")
    fun isMoreThanTwoMinutesPassed(visitDate: String, visitTime: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

            val dateTimeString = "$visitDate $visitTime" // Ensure format has seconds

            val plannedDateTime = LocalDateTime.parse(dateTimeString, formatter)
            val ukZone = ZoneId.of("Europe/London")

            val plannedZoned = plannedDateTime.atZone(ukZone)
            val nowUK = ZonedDateTime.now(ukZone)

            val duration = Duration.between(plannedZoned, nowUK)
            duration.toMinutes() >= 2 // ✅ Changed from > 2 to >= 2
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun applyTextWithColoredAsterisk(
        textView: AppCompatTextView,
        fullText: String,
        maxWidth: Int,
        context: Context
    ) {
        val paint = textView.paint
        val asterisk = " *"
        val ellipsis = "…"

        val availableWidth = maxWidth - paint.measureText(asterisk)

        var cutIndex = fullText.length
        while (cutIndex > 0 && paint.measureText(
                fullText.substring(
                    0,
                    cutIndex
                )
            ) > availableWidth
        ) {
            cutIndex--
        }

        val trimmedText = if (cutIndex < fullText.length) {
            fullText.substring(0, cutIndex).trimEnd() + ellipsis
        } else {
            fullText
        }

        val finalText = trimmedText + asterisk
        val spannable = SpannableString(finalText)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorPrimary)),
            finalText.length - 1,
            finalText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannable
    }

    fun AppCompatTextView.setTextWithColoredStar(text: String, starColor: Int) {
        post {
            val availableWidth = width.toFloat()
            val paint = paint
            val asterisk = " *"
            val ellipsis = "…"

            // Reserve width for ellipsis and *
            val reservedWidth = paint.measureText("$ellipsis$asterisk")

            var cutIndex = text.length
            while (cutIndex > 0 && paint.measureText(
                    text.substring(
                        0,
                        cutIndex
                    )
                ) > availableWidth - reservedWidth
            ) {
                cutIndex--
            }

            val finalText = if (cutIndex < text.length) {
                text.substring(0, cutIndex).trimEnd() + ellipsis + asterisk
            } else {
                text + asterisk
            }

            val spannable = SpannableString(finalText)
            spannable.setSpan(
                ForegroundColorSpan(starColor),
                finalText.length - 1,
                finalText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            this.text = spannable
        }
    }

}