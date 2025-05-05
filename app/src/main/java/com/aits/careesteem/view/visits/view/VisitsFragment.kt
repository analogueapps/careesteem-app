package com.aits.careesteem.view.visits.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.time.format.DateTimeFormatter
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentVisitsBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.view.visits.adapter.CompleteVisitsAdapter
import com.aits.careesteem.view.visits.adapter.NotCompleteVisitsAdapter
import com.aits.careesteem.view.visits.adapter.OngoingVisitsAdapter
import com.aits.careesteem.view.visits.adapter.UpcomingVisitsAdapter
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.aits.careesteem.view.visits.viewmodel.VisitsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.core.net.toUri
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.view.profile.model.UserDetailsResponse
import com.aits.careesteem.view.profile.viewmodel.ProfileViewModel
import javax.inject.Inject

@AndroidEntryPoint
class VisitsFragment : Fragment(),
    OngoingVisitsAdapter.OngoingItemItemClick,
    OngoingVisitsAdapter.OngoingCheckoutItemItemClick,
    UpcomingVisitsAdapter.OnItemItemClick,
    UpcomingVisitsAdapter.OnCheckoutItemItemClick,
    CompleteVisitsAdapter.OnViewItemItemClick,
    NotCompleteVisitsAdapter.OnDirectionItemItemClick,
    NotCompleteVisitsAdapter.OnViewItemItemClick
{
    private var _binding: FragmentVisitsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var editor: SharedPreferences.Editor

    // Viewmodel
    private val viewModel: VisitsViewModel by viewModels()
    // Viewmodel
    private val profileViewModel: ProfileViewModel by viewModels()

    // Adapters
    private lateinit var ongoingVisitsAdapter: OngoingVisitsAdapter
    private lateinit var upcomingVisitsAdapter: UpcomingVisitsAdapter
    private lateinit var completeVisitsAdapter: CompleteVisitsAdapter
    private lateinit var notCompleteVisitsAdapter: NotCompleteVisitsAdapter

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
        profileViewModel.getUserDetailsById(requireActivity())
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
        ongoingVisitsAdapter = OngoingVisitsAdapter(requireContext(), this@VisitsFragment, this@VisitsFragment)
        binding.rvOngoingVisits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOngoingVisits.adapter = ongoingVisitsAdapter

        upcomingVisitsAdapter = UpcomingVisitsAdapter(requireContext(), this@VisitsFragment, this@VisitsFragment)
        binding.rvUpcomingVisits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUpcomingVisits.adapter = upcomingVisitsAdapter

        completeVisitsAdapter = CompleteVisitsAdapter(requireContext(), this@VisitsFragment)
        binding.rvCompletedVisits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCompletedVisits.adapter = completeVisitsAdapter

        notCompleteVisitsAdapter = NotCompleteVisitsAdapter(requireContext(), this@VisitsFragment, this@VisitsFragment)
        binding.rvNotCompletedVisits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotCompletedVisits.adapter = notCompleteVisitsAdapter
    }


    private fun setupSwipeRefresh() {
        val coroutineScope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            coroutineScope.launch {
                try {
                    delay(2000)
                    binding.swipeRefresh.isRefreshing = AppConstant.FALSE
                    callApi()
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

            tvNotCompletedVisits.setOnClickListener {
                if(tvNotCompletedVisits.tag == "Invisible") {
                    tvNotCompletedVisits.tag = "Visible"
                    tvNotCompletedVisits.setCompoundDrawablesWithIntrinsicBounds(null, null, requireContext().getDrawable(R.drawable.ic_keyboard_arrow_up), null)
                    rvNotCompletedVisits.visibility = View.VISIBLE
                } else {
                    tvNotCompletedVisits.tag = "Invisible"
                    tvNotCompletedVisits.setCompoundDrawablesWithIntrinsicBounds(null, null, requireContext().getDrawable(R.drawable.ic_keyboard_arrow_down), null)
                    rvNotCompletedVisits.visibility = View.GONE
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
        //Log.d("VisitsFragment", "Selected Date: $selectedDate")
        callApi()
    }

    @SuppressLint("NewApi")
    private fun callApi() {
        viewModel.getVisits(requireActivity(), selectedDate.toString())
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
                callApi()
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

        // Upcoming visibility
        viewModel.scheduledVisits.observe(viewLifecycleOwner) { data ->
            if (data != null && data.isNotEmpty()) {
                binding.apply {
                    tvUpcomingVisits.text = requireContext().getString(R.string.upcoming_visits) + " (${data.size})"
                }
                upcomingVisitsAdapter.updatedList(data)
            } else {
                binding.apply {
                    tvUpcomingVisits.text = requireContext().getString(R.string.upcoming_visits) + " (0)"
                }
                upcomingVisitsAdapter.updatedList(emptyList())
            }
        }

        // Ongoing visibility
        viewModel.inProgressVisits.observe(viewLifecycleOwner) { data ->
            if (data != null && data.isNotEmpty()) {
                binding.apply {
                    tvOngoingVisits.text = requireContext().getString(R.string.ongoing_visits) + " (${data.size})"
                }
                ongoingVisitsAdapter.updatedList(data)
                upcomingVisitsAdapter.updatedUpcomingList(data)
            } else {
                binding.apply {
                    tvOngoingVisits.text = requireContext().getString(R.string.ongoing_visits) + " (0)"
                }
                ongoingVisitsAdapter.updatedList(emptyList())
                upcomingVisitsAdapter.updatedUpcomingList(emptyList())
            }
        }

        // Completed visibility
        viewModel.completedVisits.observe(viewLifecycleOwner) { data ->
            if (data != null && data.isNotEmpty()) {
                binding.apply {
                    tvCompletedVisits.text = requireContext().getString(R.string.completed_visits) + " (${data.size})"
                }
                completeVisitsAdapter.updatedList(data)
            } else {
                binding.apply {
                    tvCompletedVisits.text = requireContext().getString(R.string.completed_visits) + " (0)"
                }
                completeVisitsAdapter.updatedList(emptyList())
            }
        }

        // Not Completed visibility
        viewModel.notCompletedVisits.observe(viewLifecycleOwner) { data ->
            if (data != null && data.isNotEmpty()) {
                binding.apply {
                    tvNotCompletedVisits.text = requireContext().getString(R.string.not_completed_visits) + " (${data.size})"
                }
                notCompleteVisitsAdapter.updatedList(data)
            } else {
                binding.apply {
                    tvNotCompletedVisits.text = requireContext().getString(R.string.not_completed_visits) + " (0)"
                }
                notCompleteVisitsAdapter.updatedList(emptyList())
            }
        }

        // Data visibility
        profileViewModel.userDetails.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                updateProfileDetails(data)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateProfileDetails(data: UserDetailsResponse.Data) {
        if(data.profile_photo.isNotEmpty()) {
            editor.putString(SharedPrefConstant.PROFILE_IMAGE, data.profile_photo)
            editor.apply()
        }
        requireActivity().invalidateOptionsMenu()
    }

    override fun onItemItemClicked(data: VisitListResponse.Data) {
        val direction = VisitsFragmentDirections.actionBottomVisitsToCheckOutFragment(
            visitDetailsId = data.visitDetailsId,
            action = 0
        )
        findNavController().navigate(direction)
    }

    override fun ongoingItemItemClicked(data: VisitListResponse.Data) {
        if(data.visitStatus == "Unscheduled") {
            val direction = VisitsFragmentDirections.actionBottomVisitsToUnscheduledVisitsDetailsFragmentFragment(
                visitDetailsId = data.visitDetailsId
            )
            findNavController().navigate(direction)
        } else {
            val direction = VisitsFragmentDirections.actionBottomVisitsToOngoingVisitsDetailsFragment(
                visitDetailsId = data.visitDetailsId
            )
            findNavController().navigate(direction)
        }
    }

    override fun ongoingCheckoutItemItemClicked(data: VisitListResponse.Data) {
        viewModel.checkOutEligible(requireActivity(), data, findNavController())
    }

    override fun onDirectionItemItemClicked(data: VisitListResponse.Data) {
        if(data.placeId.toString().isNotEmpty()) {
            val placeId = data.placeId // your place ID
            val gmmIntentUri =
                "https://www.google.com/maps/search/?api=1&query=Google&query_place_id=$placeId".toUri()
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            // Check if Google Maps is installed
            if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(mapIntent)
            } else {
                AlertUtils.showToast(requireActivity(), "Google Maps is not installed")
            }

        } else {
            AlertUtils.showToast(requireActivity(), "Client location not found")
        }
    }

    override fun onViewItemItemClicked(data: VisitListResponse.Data) {
        if(data.visitType == "Unscheduled") {
            val direction = VisitsFragmentDirections.actionBottomVisitsToUnscheduledVisitsDetailsFragmentFragment(
                visitDetailsId = data.visitDetailsId
            )
            findNavController().navigate(direction)
        } else {
            val direction = VisitsFragmentDirections.actionBottomVisitsToOngoingVisitsDetailsFragment(
                visitDetailsId = data.visitDetailsId
            )
            findNavController().navigate(direction)
        }
    }

}