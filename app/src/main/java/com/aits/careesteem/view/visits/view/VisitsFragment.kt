package com.aits.careesteem.view.visits.view

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import java.time.format.DateTimeFormatter
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentVisitsBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.view.auth.view.WelcomeFragmentDirections
import com.aits.careesteem.view.visits.adapter.CompleteVisitsAdapter
import com.aits.careesteem.view.visits.adapter.OngoingVisitsAdapter
import com.aits.careesteem.view.visits.adapter.UpcomingVisitsAdapter
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.aits.careesteem.view.visits.viewmodel.VisitsViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class VisitsFragment : Fragment(),
    OngoingVisitsAdapter.OnItemItemClick,
    UpcomingVisitsAdapter.OnItemItemClick
{
    private var _binding: FragmentVisitsBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: VisitsViewModel by viewModels()

    // Adapters
    private lateinit var ongoingVisitsAdapter: OngoingVisitsAdapter
    private lateinit var upcomingVisitsAdapter: UpcomingVisitsAdapter
    private lateinit var completeVisitsAdapter: CompleteVisitsAdapter

    // Date
    @RequiresApi(Build.VERSION_CODES.O)
    private var currentStartOfWeek: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY)

    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate: LocalDate = currentStartOfWeek // Default to the first date of the week
    @RequiresApi(Build.VERSION_CODES.O)
    private var initialStartOfWeek: LocalDate = currentStartOfWeek // Store the initial week
    private val maxWeeksAhead = 3 // Maximum number of weeks to allow navigation
    private val maxWeeksBehind = 3 // Maximum number of weeks to allow navigation backward

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getVisits(requireActivity())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVisitsBinding.inflate(inflater, container, false)
        setupAdapter()
        setupWidget()
        setupSwipeRefresh()
        setupViewModel()
        return binding.root
    }

    private fun setupAdapter() {
        ongoingVisitsAdapter = OngoingVisitsAdapter(requireContext(), this@VisitsFragment)
        binding.rvOngoingVisits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOngoingVisits.adapter = ongoingVisitsAdapter

        upcomingVisitsAdapter = UpcomingVisitsAdapter(requireContext(), this@VisitsFragment)
        binding.rvUpcomingVisits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUpcomingVisits.adapter = upcomingVisitsAdapter

        completeVisitsAdapter = CompleteVisitsAdapter(requireContext())
        binding.rvCompletedVisits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCompletedVisits.adapter = completeVisitsAdapter
    }

    private fun setupSwipeRefresh() {
        val coroutineScope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            coroutineScope.launch {
                try {
                    delay(2000)
                    binding.swipeRefresh.isRefreshing = AppConstant.FALSE
                    viewModel.getVisits(requireActivity())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupWidget() {
        // Initialize the calendar with the current week
        addCheckedDate(currentStartOfWeek)
        updateCalendar(currentStartOfWeek)

        // Left Arrow Click: Show previous 3 weeks
        binding.arrowLeft.setOnClickListener {
            val previousWeek = currentStartOfWeek.minusWeeks(1)
            if (previousWeek.isBefore(initialStartOfWeek.minusWeeks(maxWeeksBehind.toLong()))) {
                // Show error message if trying to navigate beyond the allowed 3 weeks
                AlertUtils.showToast(requireActivity(), "Cannot navigate beyond the previous 3 weeks")
            } else {
                currentStartOfWeek = previousWeek
                addCheckedDate(currentStartOfWeek)
                updateCalendar(currentStartOfWeek)
            }
        }

        // Right Arrow Click: Show next 3 weeks
        binding.arrowRight.setOnClickListener {
            val nextWeek = currentStartOfWeek.plusWeeks(1)
            if (nextWeek.isAfter(initialStartOfWeek.plusWeeks(maxWeeksAhead.toLong()))) {
                // Show error message if trying to navigate beyond the allowed 3 weeks
                AlertUtils.showToast(requireActivity(), "Cannot navigate beyond the next 3 weeks")
            } else {
                currentStartOfWeek = nextWeek
                addCheckedDate(currentStartOfWeek)
                updateCalendar(currentStartOfWeek)
            }
        }

        // Expended list view
        binding.apply {
            tvOngoingVisits.setOnClickListener {
                if(tvOngoingVisits.tag == "Invisible") {
                    tvOngoingVisits.tag = "Visible"
                    tvOngoingVisits.setCompoundDrawablesWithIntrinsicBounds(null, null, requireContext().getDrawable(R.drawable.ic_keyboard_arrow_up), null)
                    rvOngoingVisits.visibility = View.VISIBLE
                } else {
                    tvOngoingVisits.tag = "Invisible"
                    tvOngoingVisits.setCompoundDrawablesWithIntrinsicBounds(null, null, requireContext().getDrawable(R.drawable.ic_keyboard_arrow_down), null)
                    rvOngoingVisits.visibility = View.GONE
                }
            }

            tvUpcomingVisits.setOnClickListener {
                if(tvUpcomingVisits.tag == "Invisible") {
                    tvUpcomingVisits.tag = "Visible"
                    tvUpcomingVisits.setCompoundDrawablesWithIntrinsicBounds(null, null, requireContext().getDrawable(R.drawable.ic_keyboard_arrow_up), null)
                    rvUpcomingVisits.visibility = View.VISIBLE
                } else {
                    tvUpcomingVisits.tag = "Invisible"
                    tvUpcomingVisits.setCompoundDrawablesWithIntrinsicBounds(null, null, requireContext().getDrawable(R.drawable.ic_keyboard_arrow_down), null)
                    rvUpcomingVisits.visibility = View.GONE
                }
            }

            tvCompletedVisits.setOnClickListener {
                if(tvCompletedVisits.tag == "Invisible") {
                    tvCompletedVisits.tag = "Visible"
                    tvCompletedVisits.setCompoundDrawablesWithIntrinsicBounds(null, null, requireContext().getDrawable(R.drawable.ic_keyboard_arrow_up), null)
                    rvCompletedVisits.visibility = View.VISIBLE
                } else {
                    tvCompletedVisits.tag = "Invisible"
                    tvCompletedVisits.setCompoundDrawablesWithIntrinsicBounds(null, null, requireContext().getDrawable(R.drawable.ic_keyboard_arrow_down), null)
                    rvCompletedVisits.visibility = View.GONE
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addCheckedDate(startOfWeek: LocalDate) {
        // Calculate the end of the week (Sunday)
        val endOfWeek = startOfWeek.plusDays(6)

        // Check if the current date is within this week
        val currentDate = LocalDate.now()
        selectedDate = if (currentDate in startOfWeek..endOfWeek) {
            currentDate // Select the current date if it's in the week
        } else {
            startOfWeek // Otherwise, select the first date of the week (Monday)
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCalendar(startOfWeek: LocalDate) {
        // Clear the existing views
        binding.calendarContainer.removeAllViews()

        // Calculate the end of the week (Sunday)
        val endOfWeek = startOfWeek.plusDays(6)

        // Format the date range as "Aug 15 to Aug 21 2024"
        val dateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
        val yearFormatter = DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH)
        val formattedStartDate = startOfWeek.format(dateFormatter)
        val formattedEndDate = endOfWeek.format(dateFormatter)
        val formattedYear = endOfWeek.format(yearFormatter)

        val dateRangeString = "$formattedStartDate to $formattedEndDate $formattedYear"
        binding.dateRangeText.text = dateRangeString

        // Generate and add day views for the week
        for (i in 0 until 7) {
            val dayDate = startOfWeek.plusDays(i.toLong())
            val dayView = LayoutInflater.from(requireContext()).inflate(R.layout.item_day, binding.calendarContainer, false)

            val dayName = dayView.findViewById<TextView>(R.id.dayName)
            val dayNumber = dayView.findViewById<TextView>(R.id.dayNumber)

            // Set day name (e.g., Mon, Tue, etc.)
            val dayOfWeek = dayDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            dayName.text = dayOfWeek

            // Set day number
            dayNumber.text = dayDate.dayOfMonth.toString()

            // Highlight the selected date
            if (dayDate == selectedDate) {
                dayView.setBackgroundResource(R.drawable.ic_day_select_bg) // Highlight background
                dayNumber.setTextColor(requireContext().getColor(R.color.white)) // Change text color
            } else {
                dayView.setBackgroundResource(R.drawable.ic_day_unselect_bg) // Highlight background
                dayNumber.setTextColor(requireContext().getColor(R.color.black)) // Default text color
            }

            // Set click listener for the day view
            dayView.setOnClickListener {
                Log.d("VisitsFragment", "Date clicked: $dayDate")
                selectedDate = dayDate // Update the selected date
                updateCalendar(currentStartOfWeek) // Refresh the calendar
            }

            // Add the day view to the container
            binding.calendarContainer.addView(dayView)
        }
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
        viewModel.visitsList.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                binding.apply {
                    dataLayout.visibility = View.VISIBLE
                    emptyLayout.visibility = View.GONE
                }
            } else {
                binding.apply {
                    emptyLayout.visibility = View.VISIBLE
                    dataLayout.visibility = View.GONE
                }
            }
        }

        // Ongoing visibility
        viewModel.scheduledVisits.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                binding.apply {
                    tvOngoingVisits.text = requireContext().getString(R.string.ongoing_visits) + " (${data.size})"
                }
                ongoingVisitsAdapter.updatedList(data)
            } else {
                binding.apply {
                    tvOngoingVisits.text = requireContext().getString(R.string.ongoing_visits) + " (0)"
                }
            }
        }

        // Upcoming visibility
        viewModel.upcomingVisits.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                binding.apply {
                    tvUpcomingVisits.text = requireContext().getString(R.string.upcoming_visits) + " (${data.size})"
                }
                upcomingVisitsAdapter.updatedList(data)
            } else {
                binding.apply {
                    tvUpcomingVisits.text = requireContext().getString(R.string.upcoming_visits) + " (0)"
                }
            }
        }

        // Completed visibility
        viewModel.completedVisits.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                binding.apply {
                    tvCompletedVisits.text = requireContext().getString(R.string.completed_visits) + " (${data.size})"
                }
                completeVisitsAdapter.updatedList(data)
            } else {
                binding.apply {
                    tvCompletedVisits.text = requireContext().getString(R.string.completed_visits) + " (0)"
                }
            }
        }
    }

    override fun onItemItemClicked(data: VisitListResponse.Data) {
        val gson = Gson()
        val dataString = gson.toJson(data)
        if(data.visitStatus == "Unscheduled") {
            val direction = VisitsFragmentDirections.actionBottomVisitsToUnscheduledVisitsDetailsFragmentFragment(
                visitData = dataString
            )
            findNavController().navigate(direction)
        } else {
            val direction = VisitsFragmentDirections.actionBottomVisitsToOngoingVisitsDetailsFragment(
                visitData = dataString
            )
            findNavController().navigate(direction)
        }
    }

}