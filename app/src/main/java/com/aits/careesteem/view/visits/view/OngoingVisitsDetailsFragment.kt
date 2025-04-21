package com.aits.careesteem.view.visits.view

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
import com.aits.careesteem.databinding.FragmentOngoingVisitsDetailsBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.visits.adapter.ViewPagerAdapter
import com.aits.careesteem.view.visits.model.VisitDetailsResponse
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.aits.careesteem.view.visits.viewmodel.OngoingVisitsDetailsViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime

@AndroidEntryPoint
class OngoingVisitsDetailsFragment : Fragment() {
    private var _binding: FragmentOngoingVisitsDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: OngoingVisitsDetailsFragmentArgs by navArgs()
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
        _binding = FragmentOngoingVisitsDetailsBinding.inflate(inflater, container, false)
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

        viewModel.isCheckOutEligible.observe(viewLifecycleOwner) { verified ->
            if (verified) {
                if(AppConstant.isMoreThanTwoMinutesPassed(viewModel.visitsDetails.value?.visitDate.toString(), viewModel.visitsDetails.value?.actualStartTime!![0].toString())) {
                    val direction = OngoingVisitsDetailsFragmentDirections.actionOngoingVisitsDetailsFragmentToCheckOutFragment(
                        visitDetailsId = args.visitDetailsId,
                        action = 1
                    )
                    findNavController().navigate(direction)
                } else {
                    AlertUtils.showToast(requireActivity(), "Checkout is only allowed after 2 minutes from check-in.")
                }
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
                viewModel.checkOutEligible(
                    requireActivity(),
                    args.visitDetailsId,
                )
            }

            // Cancel any previous timer if this view is recycled
            timerJob?.cancel()

            if(data.actualStartTime[0].isNotEmpty() && data.actualEndTime[0].isNotEmpty()) {
                btnCheckout.text = "Completed"
                btnCheckout.isEnabled = false
                tvPlanTime.text = data.TotalActualTimeDiff[0]
            } else if(data.actualStartTime.isNotEmpty() && data.actualStartTime[0].isNotEmpty() && data.actualEndTime[0].isEmpty()) {
                btnCheckout.text = "Check out"
                timerJob = DateTimeUtils.startCountdownTimer(data.visitDate, data.actualStartTime[0]) { remainingTime ->
                    println("Remaining Time: $remainingTime")
                    tvPlanTime.text = remainingTime
                }
            } else {
                btnCheckout.text = "Check in"
                tvPlanTime.text = "00:00"
            }

            var changes = true
            changes = !(data.actualStartTime[0].isNotEmpty() && data.actualEndTime[0].isNotEmpty())

            if(isNotCompleted(data)) {
                btnCheckout.text = "Not Completed"
                btnCheckout.isEnabled = false
                tvPlanTime.text = "00:00"
                changes = false
            }

            val adapter = ViewPagerAdapter(requireActivity(), data?.visitDetailsId.toString(), data?.clientId.toString(), changes)
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

    private fun isNotCompleted(data: VisitDetailsResponse.Data): Boolean {
        val startEmpty = data.actualStartTime.getOrNull(0).isNullOrEmpty()
        val endEmpty = data.actualEndTime.getOrNull(0).isNullOrEmpty()

        return if (startEmpty && endEmpty) {
            try {
                val plannedStart = LocalDateTime.parse("${data.visitDate}T${data.plannedStartTime}")
                val now = LocalDateTime.now()
                Duration.between(plannedStart, now).toHours() >= 4
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    /**
     * Starts a countdown timer until the provided [plannedEndTimeStr].
     *
     * @param plannedEndTimeStr The ISO 8601 string for the planned end time.
     * @param onTick A callback invoked every second with the remaining time formatted
     * as "mm:ss". When the countdown is finished, it will be updated to "Time's up!".
     * @return The Job representing the coroutine timer.
     */
    @SuppressLint("NewApi", "DefaultLocale")
    private fun startCountdownTimer(
        plannedEndTimeStr: String,
        onTick: (String) -> Unit
    ): Job {
        // Parse the planned end time from the ISO string.
        val plannedEndTime = Instant.parse(plannedEndTimeStr)

        // Launch a coroutine on the main thread.
        return CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val now = Instant.now()
                val remaining = Duration.between(now, plannedEndTime)

                if (remaining.isZero || remaining.isNegative) {
                    onTick("Time's up!")
                    break
                }

                val minutes = remaining.toMinutes()
                val seconds = remaining.seconds % 60

                // Format the remaining time as "mm:ss".
                onTick(String.format("%02d:%02d", minutes, seconds))

                delay(1000L)
            }
        }
    }
}