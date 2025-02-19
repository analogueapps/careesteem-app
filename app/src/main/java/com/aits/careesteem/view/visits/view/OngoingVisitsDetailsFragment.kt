package com.aits.careesteem.view.visits.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aits.careesteem.databinding.FragmentOngoingVisitsDetailsBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.visits.adapter.ViewPagerAdapter
import com.aits.careesteem.view.visits.model.VisitListResponse
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

@AndroidEntryPoint
class OngoingVisitsDetailsFragment : Fragment() {
    private var _binding: FragmentOngoingVisitsDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: OngoingVisitsDetailsFragmentArgs by navArgs()

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private var visitData : VisitListResponse.Data? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the JSON string from Safe Args.
        val dataString = args.visitData
        // Convert the JSON string back to your data model.
        visitData = Gson().fromJson(dataString, VisitListResponse.Data::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOngoingVisitsDetailsBinding.inflate(inflater, container, false)
        setupCardData()
        setupWidget()
        return binding.root
    }

    private fun setupCardData() {
        // Hold a reference to the coroutine Job for cancellation, if needed.
        var timerJob: Job? = null

        binding.apply {
            tvClientName.text = visitData?.clientName
            tvClientAddress.text = visitData?.clientAddress
            // You may have another field in your data representing the total planned time.
            // Here, we start a countdown using the planned end time.
            tvPlanTime.text = visitData?.totalPlannedTime

            btnCheckout.setOnClickListener {
                val direction = OngoingVisitsDetailsFragmentDirections.actionOngoingVisitsDetailsFragmentToCheckOutFragment(
                    visitData = args.visitData,
                    action = 1
                )
                findNavController().navigate(direction)
            }

            // Cancel any previous timer if this view is recycled
            timerJob?.cancel()

            // Start the countdown timer if plannedEndTime is available.
            // (Assumes data.plannedEndTime is an ISO 8601 string)
//            if (visitData?.plannedEndTime?.isNotEmpty() == true) {
//                timerJob = startCountdownTimer(visitData?.plannedEndTime.toString()) { remainingText ->
//                    tvPlanTime.text = remainingText
//                }
//            }
        }
    }

    private fun setupWidget() {
        val adapter = ViewPagerAdapter(requireActivity(), visitData?.visitDetailsId.toString())
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