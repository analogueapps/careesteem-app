/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.aits.careesteem.BuildConfig

object AlertUtils {
    @SuppressLint("LongLogTag")
    fun showLog(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.e(tag, message) else Log.d(tag, message)
    }

    fun responseToast(activity: Activity?, code: Int) {
        val message: String = when (code) {
            200 -> {
                "Success"
            }
            201 -> {
                "Created"
            }
            204 -> {
                "No Content"
            }
            400 -> {
                "Bad Request"
            }
            401 -> {
                "Unauthorised"
            }
            403 -> {
                "Forbidden"
            }
            404 -> {
                "Not Found"
            }
            405 -> {
                "Method Not Allowed"
            }
            413 -> {
                "Payload Too Large"
            }
            500 -> {
                "Internal Server Error"
            }
            503 -> {
                "Service Unavailable"
            }
            429 -> {
                "The HTTP 429 Too Many Requests response status code"
            }
            else -> {
                "Unexpected error occurred"
            }
        }
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun showToast(activity: Activity?, message: String?) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}