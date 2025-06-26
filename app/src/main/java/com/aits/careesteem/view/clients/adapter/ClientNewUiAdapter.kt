package com.aits.careesteem.view.clients.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aits.careesteem.view.clients.view.AboutMeFragment
import com.aits.careesteem.view.clients.view.CareNetworkFragment
import com.aits.careesteem.view.clients.view.CarePlanFragment

class ClientNewUiAdapter(fragmentActivity: FragmentActivity, private val clientData: String) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

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
            0 -> AboutMeFragment.newInstance(clientData)
            1 -> CareNetworkFragment.newInstance(clientData)
            2 -> CarePlanFragment.newInstance(clientData)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}