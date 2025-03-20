package com.aits.careesteem.view.unscheduled_visits.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aits.careesteem.databinding.FragmentUnscheduledVisitsDetailsFragmentBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.unscheduled_visits.adapter.UvViewPagerAdapter
import com.aits.careesteem.view.visits.model.VisitDetailsResponse
import com.aits.careesteem.view.visits.viewmodel.OngoingVisitsDetailsViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

@AndroidEntryPoint
class UnscheduledVisitsDetailsFragmentFragment : Fragment() {
    private var _binding: FragmentUnscheduledVisitsDetailsFragmentBinding? = null
    private val binding get() = _binding!!
    private val args: UnscheduledVisitsDetailsFragmentFragmentArgs by navArgs()
    // Viewmodel
    private val viewModel: OngoingVisitsDetailsViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getVisitDetails(requireActivity(), args.visitDetailsId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUnscheduledVisitsDetailsFragmentBinding.inflate(inflater, container, false)
        setupViewModel()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setupViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                ProgressLoader.showProgress(requireActivity())
            } else {
                ProgressLoader.dismissProgress()
            }
        }

        // Data visibility
        viewModel.visitsDetails.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                setupCardData(data)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupCardData(data: VisitDetailsResponse.Data) {
        // Hold a reference to the coroutine Job for cancellation, if needed.
        var timerJob: Job? = null

        binding.apply {
            tvClientName.text = data?.clientName
            tvClientAddress.text = data?.clientAddress
            // You may have another field in your data representing the total planned time.
            // Here, we start a countdown using the planned end time.
            tvPlanTime.text = data?.totalPlannedTime

            btnCheckout.setOnClickListener {
                val direction = UnscheduledVisitsDetailsFragmentFragmentDirections.actionUnscheduledVisitsDetailsFragmentFragmentToCheckOutFragment(
                    visitDetailsId = args.visitDetailsId,
                    action = 1
                )
                findNavController().navigate(direction)
            }

            // Cancel any previous timer if this view is recycled
            timerJob?.cancel()

            if(data.actualStartTime.isNotEmpty() && data.actualStartTime[0].isNotEmpty()) {
                btnCheckout.text = "Check out"
                timerJob = DateTimeUtils.startCountdownTimer(data.visitDate, data.actualStartTime[0]) { remainingTime ->
                    println("Remaining Time: $remainingTime")
                    tvPlanTime.text = remainingTime
                }
            } else {
                btnCheckout.text = "Check in"
                tvPlanTime.text = "00:00"
            }

            val adapter = UvViewPagerAdapter(requireActivity(), data?.visitDetailsId.toString())
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
            binding.viewPager.isUserInputEnabled = AppConstant.TRUE
        }
    }
}