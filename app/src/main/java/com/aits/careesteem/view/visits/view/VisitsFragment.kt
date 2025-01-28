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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentVisitsBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.view.auth.view.WelcomeFragmentDirections
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
class VisitsFragment : Fragment() {
    private var _binding: FragmentVisitsBinding? = null
    private val binding get() = _binding!!

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVisitsBinding.inflate(inflater, container, false)
        setupWidget()
        setupSwipeRefresh()
        setupViewModel()
        return binding.root
    }

    private fun setupSwipeRefresh() {
        val coroutineScope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            coroutineScope.launch {
                try {
                    delay(2000)
                    binding.swipeRefresh.isRefreshing = AppConstant.FALSE

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

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

    private fun setupViewModel() {
        binding.emptyLayout.setOnClickListener {
            val direction = VisitsFragmentDirections.actionBottomVisitsToOngoingVisitsDetailsFragment(
                taskId = ""
            )
            findNavController().navigate(direction)
        }
    }

}