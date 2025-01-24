/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.utils

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData

@BindingAdapter("android:visibility")
fun setVisibility(view: View, isVisible: LiveData<Boolean>?) {
    isVisible?.observeForever { visible ->
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }
}