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
import android.view.WindowManager
import android.widget.ImageView
import com.aits.careesteem.R
import com.bumptech.glide.Glide

object ProgressLoader {
    private var dialog: Dialog? = null

//    fun showProgress(activity: Activity): Dialog {
//        dialog = Dialog(activity)
//        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog?.window?.setBackgroundDrawable(ColorDrawable(0)) // Transparent background
//        dialog?.setContentView(R.layout.lyt_custom_progress_dialog) // Use the updated layout
//        dialog?.setCancelable(false) // Prevent dismissing by tapping outside
//
//        val gifView = dialog?.findViewById<ImageView>(R.id.loaderImage)
//        Glide.with(activity)
//            .asGif()
//            .load(R.drawable.animated_logo) // your loader.gif in drawable
//            .into(gifView!!)
//
//        dialog?.show()
//        return dialog!!
//    }

    fun showProgress(activity: Activity): Dialog {
        dialog = Dialog(activity)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(R.layout.lyt_custom_progress_dialog)
        dialog?.setCancelable(false)

        // Set transparent background for dialog content
        dialog?.window?.setBackgroundDrawable(ColorDrawable(0))

        // Set dim amount for background (0.0 to 1.0)
        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.window?.setDimAmount(0.8f) // 👈 Background dim amount here

        // Load GIF using Glide
        val gifView = dialog?.findViewById<ImageView>(R.id.loaderImage)
        Glide.with(activity)
            .asGif()
            .load(R.drawable.logo_animation) // Replace with your actual GIF resource
            .into(gifView!!)

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