package com.aits.careesteem.view.unscheduled_visits.view

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentUnscheduledVisitsDetailsFragmentBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.unscheduled_visits.adapter.UvViewPagerAdapter
import com.aits.careesteem.view.visits.model.VisitDetailsResponse
import com.aits.careesteem.view.visits.view.OngoingVisitsDetailsFragmentDirections
import com.aits.careesteem.view.visits.viewmodel.OngoingVisitsDetailsViewModel
import com.google.android.material.tabs.TabLayout
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
                if(AppConstant.isMoreThanTwoMinutesPassed(viewModel.visitsDetails.value?.visitDate.toString(), viewModel.visitsDetails.value?.actualStartTime!![0].toString())) {
                    val direction = UnscheduledVisitsDetailsFragmentFragmentDirections.actionUnscheduledVisitsDetailsFragmentFragmentToCheckOutFragment(
                        visitDetailsId = args.visitDetailsId,
                        action = 1
                    )
                    findNavController().navigate(direction)
                } else {
                    AlertUtils.showToast(requireActivity(), "Checkout is only allowed after 2 minutes from check-in.", ToastyType.WARNING)
                }
            }

            // Cancel any previous timer if this view is recycled
            timerJob?.cancel()

//            if(data.actualStartTime.isNotEmpty() && data.actualStartTime[0].isNotEmpty()) {
//                btnCheckout.text = "Check out"
//                timerJob = DateTimeUtils.startCountdownTimer(data.visitDate, data.actualStartTime[0]) { remainingTime ->
//                    println("Remaining Time: $remainingTime")
//                    tvPlanTime.text = remainingTime
//                }
//            } else {
//                btnCheckout.text = "Check in"
//                tvPlanTime.text = "00:00"
//            }
            if(data.actualStartTime.isNotEmpty() && data.actualStartTime[0].isNotEmpty() && data.actualEndTime.isNotEmpty() && data.actualEndTime[0].isNotEmpty()) {
                btnCheckout.text = "Completed"
                btnCheckout.isEnabled = false
                btnCheckout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray_button))
                tvPlanTime.text = data.TotalActualTimeDiff[0]
            } else if(data.actualStartTime.isNotEmpty() && data.actualStartTime[0].isNotEmpty() && data.actualEndTime.isEmpty()) {
//                btnCheckout.text = "Check out"
//                timerJob = DateTimeUtils.startCountdownTimer(data.visitDate, data.actualStartTime[0]) { remainingTime ->
//                    println("Remaining Time: $remainingTime")
//                    tvPlanTime.text = remainingTime
//                }
                btnCheckout.text = "Check out"
                timerJob = DateTimeUtils.startCountdownTimer(data.visitDate, data.actualStartTime[0]) { remainingTime ->
                    //println("Remaining Time: $remainingTime")
                    tvPlanTime.text = remainingTime
                    btnCheckout.isEnabled = false
                    val hasPassed = AppConstant.isMoreThanTwoMinutesPassed(data.visitDate, data.actualStartTime[0])
                    //println("Has more than 2 minutes passed? $hasPassed")
                    if (hasPassed) {
                        btnCheckout.text = "Check out"
                        btnCheckout.isEnabled = true
                        btnCheckout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.upcomingCardCorner))
                    }
                }
            } else {
                btnCheckout.text = "Check in"
                tvPlanTime.text = "00:00"
                btnCheckout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ongoingCardCorner))
            }

            var changes = true
            changes = !(data.actualStartTime.isNotEmpty() && data.actualStartTime[0].isNotEmpty() && data.actualEndTime.isNotEmpty() && data.actualEndTime[0].isNotEmpty())

            val adapter = UvViewPagerAdapter(requireActivity(), data?.visitDetailsId.toString(), changes)
            binding.viewPager.adapter = adapter

            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "To-Do's"
                    1 -> "Medication"
                    2 -> "Visit Notes"
                    else -> throw IllegalArgumentException("Invalid position")
                }
            }.attach()

            val titles = listOf("To-Do's", "Medication", "Visit Notes")

            for (i in 0 until binding.tabLayout.tabCount) {
                val tab = binding.tabLayout.getTabAt(i)
                val isSelected = (i == binding.viewPager.currentItem)
                tab?.customView = createCustomTab(titles[i], isSelected)
            }

            binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val view = tab.customView
                    val text = view?.findViewById<TextView>(R.id.tabText)
                    val arrow = view?.findViewById<ImageView>(R.id.tabArrow)

                    text?.setBackgroundResource(R.drawable.bg_tab_selected)
                    text?.setTextColor(Color.WHITE)
                    arrow?.visibility = View.VISIBLE
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    val view = tab.customView
                    val text = view?.findViewById<TextView>(R.id.tabText)
                    val arrow = view?.findViewById<ImageView>(R.id.tabArrow)

                    text?.setBackgroundResource(R.drawable.bg_tab_unselected)
                    text?.setTextColor(Color.parseColor("#607D8B"))
                    arrow?.visibility = View.GONE
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

            // Disable swiping by intercepting touch events
            binding.viewPager.isUserInputEnabled = AppConstant.TRUE
        }
    }

    private fun createCustomTab(title: String, isSelected: Boolean): View {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.tab_custom, null)
        val text = view.findViewById<TextView>(R.id.tabText)
        val arrow = view.findViewById<ImageView>(R.id.tabArrow)

        text.text = title

        if (isSelected) {
            text.setBackgroundResource(R.drawable.bg_tab_selected)
            text.setTextColor(Color.WHITE)
            arrow.visibility = View.VISIBLE
        } else {
            text.setBackgroundResource(R.drawable.bg_tab_unselected)
            text.setTextColor(Color.parseColor("#607D8B"))
            arrow.visibility = View.GONE
        }

        return view
    }
}