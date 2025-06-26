/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.network

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.auth.view.AuthActivity
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject

class ErrorHandler @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
) {

    fun handleErrorResponse(response: Response<*>, activity: Activity) {
        when (response.code()) {
            401 -> handleUnauthorizedError(response, activity)
            else -> handleGeneralError(response, activity)
        }
    }

    private fun handleUnauthorizedError(response: Response<*>, activity: Activity) {
        val errorBody = response.errorBody()?.string()
        val jsonObject = JSONObject(errorBody!!)
        val displayMessage = jsonObject.optString("message").takeIf { it.isNotEmpty() }
            ?: jsonObject.optString("error")
        AlertUtils.showToast(activity, displayMessage, ToastyType.ERROR)
        val fcmToken = sharedPreferences.getString(SharedPrefConstant.FCM_TOKEN, null)
        editor.clear()
        fcmToken?.let {
            editor.putString(SharedPrefConstant.FCM_TOKEN, it)
        }
        editor.apply()
        val intent = Intent(activity, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
    }

    private fun handleGeneralError(response: Response<*>, activity: Activity) {
        val errorBody = response.errorBody()?.string()
        if (errorBody != null) {
            try {
                // Check if the response is valid JSON
                val jsonObject = JSONObject(errorBody)
                val displayMessage = jsonObject.optString("message").takeIf { it.isNotEmpty() }
                    ?: jsonObject.optString("error")
                AlertUtils.showToast(activity, displayMessage, ToastyType.ERROR)
            } catch (e: JSONException) {
                // If parsing fails, assume it's an HTML error or unknown format
                AlertUtils.responseToast(activity, response.code())
            }
        } else {
            AlertUtils.responseToast(activity, response.code())
        }
    }
}