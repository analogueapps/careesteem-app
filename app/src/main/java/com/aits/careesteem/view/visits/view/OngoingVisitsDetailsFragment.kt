package com.aits.careesteem.view.visits.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.aits.careesteem.databinding.FragmentOngoingVisitsDetailsBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.visits.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OngoingVisitsDetailsFragment : Fragment() {
    private var _binding: FragmentOngoingVisitsDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOngoingVisitsDetailsBinding.inflate(inflater, container, false)
        setupWidget()
        return binding.root

    }

    private fun setupWidget() {
        val adapter = ViewPagerAdapter(requireActivity())
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "To-Do's"
                1 -> "Medication"
                2 -> "Visit Notes"
                else -> throw IllegalArgumentException("Invalid position")
            }
        }.attach()

        // Disable swiping by intercepting touch events
        binding.viewPager.isUserInputEnabled = AppConstant.FALSE
    }
}