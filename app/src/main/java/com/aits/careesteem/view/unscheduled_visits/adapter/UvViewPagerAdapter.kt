/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.unscheduled_visits.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aits.careesteem.view.unscheduled_visits.view.UvMedicationFragment
import com.aits.careesteem.view.unscheduled_visits.view.UvVisitNotesFragment

class UvViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val visitData: String,
    private val changes: Boolean
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    // Return a unique ID for each fragment position.
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // Confirm whether an item with a given ID exists.
    override fun containsItem(itemId: Long): Boolean {
        return itemId in 0 until itemCount
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            //0 -> UvToDoFragment.newInstance(visitData, changes)
            0 -> UvMedicationFragment.newInstance(visitData, changes)
            1 -> UvVisitNotesFragment.newInstance(visitData, changes)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}