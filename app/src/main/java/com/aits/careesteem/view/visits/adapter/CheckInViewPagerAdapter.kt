package com.aits.careesteem.view.visits.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aits.careesteem.view.visits.view.MapCheckInFragment
import com.aits.careesteem.view.visits.view.QrCheckInFragment

class CheckInViewPagerAdapter (fragmentActivity: FragmentActivity, private val visitData: String, private val args: Int) : FragmentStateAdapter(fragmentActivity) {
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
            0 -> MapCheckInFragment.newInstance(visitData, args)
            1 -> QrCheckInFragment.newInstance(visitData, args)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}