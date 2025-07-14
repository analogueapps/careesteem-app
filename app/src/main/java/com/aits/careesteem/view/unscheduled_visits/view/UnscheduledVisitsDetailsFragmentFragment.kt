package com.aits.careesteem.view.unscheduled_visits.view

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.bottomsheet.PreviousVisitNotesBottomSheetFragment
import com.aits.careesteem.view.unscheduled_visits.adapter.UvViewPagerAdapter
import com.aits.careesteem.view.visits.model.VisitDetailsResponse
import com.aits.careesteem.view.visits.viewmodel.OngoingVisitsDetailsViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import javax.inject.Inject

@AndroidEntryPoint
class UnscheduledVisitsDetailsFragmentFragment : Fragment() {
    private var _binding: FragmentUnscheduledVisitsDetailsFragmentBinding? = null
    private val binding get() = _binding!!
    private val args: UnscheduledVisitsDetailsFragmentFragmentArgs by navArgs()

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
        _binding =
            FragmentUnscheduledVisitsDetailsFragmentBinding.inflate(inflater, container, false)
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
                if (AppConstant.isMoreThanTwoMinutesPassed(
                        viewModel.visitsDetails.value?.visitDate.toString(),
                        viewModel.visitsDetails.value?.actualStartTime!![0].toString()
                    )
                ) {
                    val direction =
                        UnscheduledVisitsDetailsFragmentFragmentDirections.actionUnscheduledVisitsDetailsFragmentFragmentToCheckOutFragment(
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
//                btnCheckout.text = "Check out"
//                timerJob = DateTimeUtils.startCountdownTimer(data.visitDate, data.actualStartTime[0]) { remainingTime ->
//                    println("Remaining Time: $remainingTime")
//                    tvPlanTime.text = remainingTime
//                }
                btnCheckout.text = "Check out"
                dividerView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.ongoingCardCorner
                    )
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
                btnCheckout.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.upcomingCardCorner)
                tvPlanTime.backgroundTintList = ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.upcomingCardInsideCount
                )
            }

            var changes = true
            changes =
                !(data.actualStartTime.isNotEmpty() && data.actualStartTime[0].isNotEmpty() && data.actualEndTime.isNotEmpty() && data.actualEndTime[0].isNotEmpty())

            val gson = Gson()
            val dataString = gson.toJson(data)

            val adapter = UvViewPagerAdapter(requireActivity(), dataString, changes)
            binding.viewPager.adapter = adapter

            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = when (position) {
                    //0 -> "To-Do's"
                    0 -> "Medication"
                    1 -> "Visit Notes"
                    else -> throw IllegalArgumentException("Invalid position")
                }
            }.attach()

            val titles = listOf("Medication", "Visit Notes")

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

        // Important: force equal width on each tab
        view.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

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
}