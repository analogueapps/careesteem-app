/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.aits.careesteem.BuildConfig
import es.dmoral.toasty.Toasty

object AlertUtils {
    @SuppressLint("LongLogTag")
    fun showLog(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.e(tag, message) else Log.d(tag, message)
    }

    fun responseToast(activity: Activity?, code: Int) {
        val message: String = when (code) {
            100 -> "Continue"
            101 -> "Switching Protocols"
            102 -> "Processing"
            103 -> "Early Hints"

            200 -> "OK"
            201 -> "Created"
            202 -> "Accepted"
            203 -> "Non-Authoritative Information"
            204 -> "No Content"
            205 -> "Reset Content"
            206 -> "Partial Content"
            207 -> "Multi-Status"
            208 -> "Already Reported"
            226 -> "IM Used"

            300 -> "Multiple Choices"
            301 -> "Moved Permanently"
            302 -> "Found"
            303 -> "See Other"
            304 -> "Not Modified"
            305 -> "Use Proxy"
            307 -> "Temporary Redirect"
            308 -> "Permanent Redirect"

            400 -> "Bad Request"
            401 -> "Unauthorized"
            402 -> "Payment Required"
            403 -> "Forbidden"
            404 -> "Not Found"
            405 -> "Method Not Allowed"
            406 -> "Not Acceptable"
            407 -> "Proxy Authentication Required"
            408 -> "Request Timeout"
            409 -> "Conflict"
            410 -> "Gone"
            411 -> "Length Required"
            412 -> "Precondition Failed"
            413 -> "Payload Too Large"
            414 -> "URI Too Long"
            415 -> "Unsupported Media Type"
            416 -> "Range Not Satisfiable"
            417 -> "Expectation Failed"
            418 -> "I'm a teapot"
            421 -> "Misdirected Request"
            422 -> "Unprocessable Content"
            423 -> "Locked"
            424 -> "Failed Dependency"
            425 -> "Too Early"
            426 -> "Upgrade Required"
            428 -> "Precondition Required"
            429 -> "Too Many Requests"
            431 -> "Request Header Fields Too Large"
            451 -> "Unavailable For Legal Reasons"

            500 -> "Internal Server Error"
            501 -> "Not Implemented"
            502 -> "Bad Gateway"
            503 -> "Service Unavailable"
            504 -> "Gateway Timeout"
            505 -> "HTTP Version Not Supported"
            506 -> "Variant Also Negotiates"
            507 -> "Insufficient Storage"
            508 -> "Loop Detected"
            510 -> "Not Extended"
            511 -> "Network Authentication Required"

            else -> "Unexpected error occurred"
        }
        //Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        showToast(activity, message, ToastyType.ERROR)
    }

    @SuppressLint("NewApi", "InflateParams")
    fun showToast(activity: Activity?, message: String?, type: ToastyType) {
//        //Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
//        activity?.findViewById<View>(android.R.id.content)?.let { rootView ->
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
            ToastyType.SUCCESS -> Toasty.success(activity!!, message!!, Toast.LENGTH_SHORT, true)
                .show()

            ToastyType.ERROR -> Toasty.error(activity!!, message!!, Toast.LENGTH_SHORT, true).show()
            ToastyType.INFO -> Toasty.info(activity!!, message!!, Toast.LENGTH_SHORT, true).show()
            ToastyType.WARNING -> Toasty.warning(activity!!, message!!, Toast.LENGTH_SHORT, true)
                .show()

            ToastyType.NORMAL -> Toasty.normal(activity!!, message!!, Toast.LENGTH_SHORT).show()
        }
    }
}