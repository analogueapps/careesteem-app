/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.view

import android.content.Context
import android.graphics.Canvas
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

class CustomDividerItemDecoration(context: Context, orientation: Int) :
    DividerItemDecoration(context, orientation) {

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        for (i in 0 until childCount - 1) { // Skip last item
            super.onDrawOver(c, parent, state)
        }
    }
}
