/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.utils

import android.app.Activity
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.aits.careesteem.R

object ProgressLoader {
    private var dialog: Dialog? = null

    fun showProgress(activity: Activity): Dialog {
        dialog = Dialog(activity)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(0)) // Transparent background
        dialog?.setContentView(R.layout.lyt_custom_progress_dialog) // Use the updated layout
        dialog?.setCancelable(false) // Prevent dismissing by tapping outside
        dialog?.show()
        return dialog!!
    }

    fun dismissProgress() {
        dialog?.dismiss()
        dialog = null // Clear the dialog reference
    }

    fun toggle(activity: Activity, isLoading: Boolean) {
        if (isLoading) {
            showProgress(activity)
        } else {
            dismissProgress()
        }
    }
}