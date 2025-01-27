/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.visits.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aits.careesteem.view.visits.view.MedicationFragment
import com.aits.careesteem.view.visits.view.ToDoFragment
import com.aits.careesteem.view.visits.view.VisitNotesFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ToDoFragment()
            1 -> MedicationFragment()
            2 -> VisitNotesFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}