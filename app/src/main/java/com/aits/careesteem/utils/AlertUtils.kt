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
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.aits.careesteem.BuildConfig
import com.aits.careesteem.R
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty

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
        //Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        showToast(activity, message, ToastyType.ERROR)
    }

    @SuppressLint("NewApi", "InflateParams")
    fun showToast(activity: Activity?, message: String?, type: ToastyType) {
//        //Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
//        activity?.findViewById<View>(android.R.id.content)?.let { rootView ->
//            val snackbar = Snackbar.make(rootView, message!!, Snackbar.LENGTH_LONG)
//            val snackbarView = snackbar.view
//            val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
//
//            textView.maxLines = 5  // Allow multi-line text
//            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f) // Adjust font size if needed
//            textView.typeface = activity.resources?.getFont(R.font.lora_regular) // Set custom font
//
//            snackbar.show()
//        }
        //Toasty.success(activity!!, message!!, Toast.LENGTH_SHORT, true).show()
        when (type) {
            ToastyType.SUCCESS -> Toasty.success(activity!!, message!!, Toast.LENGTH_SHORT, true).show()
            ToastyType.ERROR -> Toasty.error(activity!!, message!!, Toast.LENGTH_SHORT, true).show()
            ToastyType.INFO -> Toasty.info(activity!!, message!!, Toast.LENGTH_SHORT, true).show()
            ToastyType.WARNING -> Toasty.warning(activity!!, message!!, Toast.LENGTH_SHORT, true).show()
            ToastyType.NORMAL -> Toasty.normal(activity!!, message!!, Toast.LENGTH_SHORT).show()
        }
    }
}