package com.aits.careesteem.network

import android.app.Activity
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.ToastyType
import retrofit2.HttpException
import java.net.SocketTimeoutException

fun handleException(e: Exception, activity: Activity) {
    when (e) {
        is SocketTimeoutException -> {
            AlertUtils.showToast(activity, "Request Timeout. Please try again.", ToastyType.ERROR)
        }
        is HttpException -> {
            AlertUtils.showToast(activity, "Server error: ${e.message}", ToastyType.ERROR)
        }
        else -> {
            AlertUtils.showToast(activity, "An error occurred: ${e.message}", ToastyType.ERROR)
            e.printStackTrace()
        }
    }
}