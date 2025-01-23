/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.utils

import android.annotation.SuppressLint
import android.util.Log
import com.aits.careesteem.BuildConfig

object AlertUtils {
    @SuppressLint("LongLogTag")
    fun showLog(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.e(tag, message) else Log.e(tag, message)
    }
}