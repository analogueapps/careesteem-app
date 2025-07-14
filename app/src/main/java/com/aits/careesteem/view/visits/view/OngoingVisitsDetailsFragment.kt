package com.aits.careesteem.view.visits.view

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentOngoingVisitsDetailsBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.bottomsheet.PreviousVisitNotesBottomSheetFragment
import com.aits.careesteem.view.bottomsheet.VisitNotesBottomSheetFragment
import com.aits.careesteem.view.visits.adapter.ViewPagerAdapter
import com.aits.careesteem.view.visits.model.VisitDetailsResponse
import com.aits.careesteem.view.visits.viewmodel.OngoingVisitsDetailsViewModel
import com.google.android.material.tabs.TabLayout
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
import javax.inject.Inject

@AndroidEntryPoint
class OngoingVisitsDetailsFragment : Fragment() {
    private var _binding: FragmentOngoingVisitsDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: OngoingVisitsDetailsFragmentArgs by navArgs()

    // Viewmodel
    private val viewModel: OngoingVisitsDetailsViewModel by viewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var editor: SharedPreferences.Editor

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getVisitDetails(requireActivity(), args.visitDetailsId)

        if(sharedPreferences.getBoolean(SharedPrefConstant.SHOW_PREVIOUS_NOTES, false)){
            viewModel.getPreviousVisitNotes(requireActivity(), args.visitDetailsId)
        }

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
                binding.topCard.visibility = View.VISIBLE
                setupCardData(data)
            }
        }

        viewModel.visitNotesList.observe(viewLifecycleOwner) {
            editor.putBoolean(SharedPrefConstant.SHOW_PREVIOUS_NOTES, false)
            editor.apply()
            it?.let {
                //showPreviousVisitNotes(it)
                val gson = Gson()
                val bottomSheet = PreviousVisitNotesBottomSheetFragment.newInstance(gson.toJson(it))
                bottomSheet.show(childFragmentManager, PreviousVisitNotesBottomSheetFragment.TAG)
            }
        }

        viewModel.isCheckOutEligible.observe(viewLifecycleOwner) { verified ->
            if (verified) {
                if (AppConstant.isMoreThanTwoMinutesPassed(
                        viewModel.visitsDetails.value?.visitDate.toString(),
                        viewModel.visitsDetails.value?.actualStartTime!![0].toString()
                    )
                ) {
                    val direction =
                        OngoingVisitsDetailsFragmentDirections.actionOngoingVisitsDetailsFragmentToCheckOutFragment(
                            visitDetailsId = args.visitDetailsId,
                            action = 1
                        )
                    findNavController().navigate(direction)
                } else {
                    AlertUtils.showToast(
                        requireActivity(),
                        "Checkout is only allowed after 2 minutes from check-in.",
                        ToastyType.WARNING
                    )
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupCardData(data: VisitDetailsResponse.Data) {
        // Hold a reference to the coroutine Job for cancellation, if needed.
        var timerJob: Job? = null

        binding.apply {
            tvClientName.text = AppConstant.checkClientName(data?.clientName)
            //tvClientAddress.text = data?.clientAddress
            tvClientAddress.text = AppConstant.checkNull(data.clientAddress)
            tvClientPostCode.text = "${AppConstant.checkNull(data.clientCity)}, ${AppConstant.checkNull(data.clientPostcode)}"
            // You may have another field in your data representing the total planned time.
            // Here, we start a countdown using the planned end time.
            tvPlanTime.text = data?.totalPlannedTime

            if (data?.plannedStartTime!!.isEmpty() && data?.plannedEndTime!!.isEmpty()) {
                plannedTime.text = "Unscheduled Visit"
            } else {
                plannedTime.text = "${data?.plannedStartTime} - ${data?.plannedEndTime}"
            }

            btnCheckout.setOnClickListener {
                viewModel.checkOutEligible(
                    requireActivity(),
                    args.visitDetailsId,
                )
            }

            // Cancel any previous timer if this view is recycled
            timerJob?.cancel()

            if (data.actualStartTime.isNotEmpty() && data.actualStartTime[0].isNotEmpty() && data.actualEndTime.isNotEmpty() && data.actualEndTime[0].isNotEmpty()) {
                btnCheckout.text = "Completed"
                btnCheckout.isEnabled = false
                btnCheckout.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.completeCardCorner)
                tvPlanTime.text = data.TotalActualTimeDiff[0]

                topCard.setStrokeColor(
                    ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.completeCardCorner
                    )
                )
                topCard.setCardBackgroundColor(
                    ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.completeCardBackground
                    )
                )
                tvPlanTime.backgroundTintList = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.completeCardInsideCount
                )
                dividerView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.completeCardCorner
                    )
                )
            } else if (data.actualStartTime.isNotEmpty() && data.actualStartTime[0].isNotEmpty() && data.actualEndTime.isEmpty()) {
                btnCheckout.text = "Check out"
                dividerView.setBackgroundColor(
                    (ContextCompat.getColor(
                        requireContext(),
                        R.color.ongoingCardCorner
                    ))
                )
                timerJob = DateTimeUtils.startCountdownTimer(
                    data.visitDate,
                    data.actualStartTime[0]
                ) { remainingTime ->
                    //println("Remaining Time: $remainingTime")
                    tvPlanTime.text = remainingTime
                    btnCheckout.isEnabled = false
                    btnCheckout.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.dialogTextColor)
                    btnCheckout.setTextColor(
                        ContextCompat.getColorStateList(
                            requireContext(),
                            R.color.black
                        )
                    )
                    val hasPassed = AppConstant.isMoreThanTwoMinutesPassed(
                        data.visitDate,
                        data.actualStartTime[0]
                    )
                    //println("Has more than 2 minutes passed? $hasPassed")
                    if (hasPassed) {
                        btnCheckout.text = "Check out"
                        btnCheckout.isEnabled = true
                        btnCheckout.backgroundTintList = ContextCompat.getColorStateList(
                            requireContext(),
                            R.color.notCompleteCardCorner
                        )
                        btnCheckout.setTextColor(
                            ContextCompat.getColorStateList(
                                requireContext(),
                                R.color.white
                            )
                        )
                    }
                }
            } else {
                btnCheckout.text = "Check in"
                tvPlanTime.text = "00:00"
                btnCheckout.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.upcomingCardCorner
                    )
                )
                dividerView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.upcomingCardCorner
                    )
                )
            }

            var changes = true
            changes =
                !(data.actualStartTime.isNotEmpty() && data.actualStartTime[0].isNotEmpty() && data.actualEndTime.isNotEmpty() && data.actualEndTime[0].isNotEmpty())

            if (isNotCompleted(data)) {
                btnCheckout.text = "Not Completed"
                btnCheckout.isEnabled = false
                btnCheckout.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.notCompleteCardCorner)
                tvPlanTime.text = "00:00"
                changes = false

                // change all the things
                topCard.setStrokeColor(
                    ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.notCompleteCardCorner
                    )
                )
                topCard.setCardBackgroundColor(
                    ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.notCompleteCardBackground
                    )
                )
                tvPlanTime.backgroundTintList = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.notCompleteCardInsideCount
                )
                dividerView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.notCompleteCardCorner
                    )
                )
            }

            val adapter = ViewPagerAdapter(
                requireActivity(),
                data?.visitDetailsId.toString(),
                data?.clientId.toString(),
                changes
            )
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
                    text?.setTypeface(
                        ResourcesCompat.getFont(
                            requireContext(),
                            R.font.robotoslab_regular
                        ), Typeface.BOLD
                    )
                    arrow?.visibility = View.VISIBLE
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    val view = tab.customView
                    val text = view?.findViewById<TextView>(R.id.tabText)
                    val arrow = view?.findViewById<ImageView>(R.id.tabArrow)

                    text?.setBackgroundResource(R.drawable.bg_tab_unselected)
                    text?.setTextColor(Color.parseColor("#1E3037"))
                    text?.setTypeface(
                        ResourcesCompat.getFont(
                            requireContext(),
                            R.font.robotoslab_regular
                        ), Typeface.NORMAL
                    )
                    arrow?.visibility = View.INVISIBLE
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
            text?.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.robotoslab_regular),
                Typeface.BOLD
            )
            arrow.visibility = View.VISIBLE
        } else {
            text.setBackgroundResource(R.drawable.bg_tab_unselected)
            text.setTextColor(Color.parseColor("#1E3037"))
            text?.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.robotoslab_regular),
                Typeface.NORMAL
            )
            arrow.visibility = View.INVISIBLE
        }

        return view
    }

    @SuppressLint("NewApi")
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