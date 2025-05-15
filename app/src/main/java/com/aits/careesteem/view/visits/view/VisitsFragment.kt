package com.aits.careesteem.view.visits.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.BuildConfig
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentVisitsBinding
import com.aits.careesteem.network.GoogleApiService
import com.aits.careesteem.utils.*
import com.aits.careesteem.view.profile.model.UserDetailsResponse
import com.aits.careesteem.view.profile.viewmodel.ProfileViewModel
import com.aits.careesteem.view.unscheduled_visits.model.VisitItem
import com.aits.careesteem.view.visits.adapter.*
import com.aits.careesteem.view.visits.model.DirectionsResponse
import com.aits.careesteem.view.visits.model.PlaceDetailsResponse
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.aits.careesteem.view.visits.viewmodel.VisitsViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class VisitsFragment : Fragment(),
    OngoingVisitsAdapter.OngoingItemItemClick,
    OngoingVisitsAdapter.OngoingCheckoutItemItemClick,
    UpcomingVisitsAdapter.OnItemItemClick,
    UpcomingVisitsAdapter.OnCheckoutItemItemClick,
    CompleteVisitsAdapter.OnViewItemItemClick,
    NotCompleteVisitsAdapter.OnDirectionItemItemClick,
    NotCompleteVisitsAdapter.OnViewItemItemClick {

    private var _binding: FragmentVisitsBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var sharedPreferences: SharedPreferences
    @Inject lateinit var editor: SharedPreferences.Editor

    private val viewModel: VisitsViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    private lateinit var ongoingAdapter: OngoingVisitsAdapter
    private lateinit var upcomingAdapter: UpcomingVisitsAdapter
    private lateinit var completeAdapter: CompleteVisitsAdapter
    private lateinit var notCompleteAdapter: NotCompleteVisitsAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    private var currentWeekStart: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY)
    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate: LocalDate = currentWeekStart
    @RequiresApi(Build.VERSION_CODES.O)
    private var initialWeekStart: LocalDate = currentWeekStart

    private val maxWeeksAhead = 3
    private val maxWeeksBehind = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel.getUserDetailsById(requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVisitsBinding.inflate(inflater, container, false)
        setupAdapters()
        setupUI()
        observeViewModels()
        setupSwipeRefresh()
        return binding.root
    }

    private fun setupAdapters() {
        with(binding) {
            ongoingAdapter = OngoingVisitsAdapter(requireContext(), this@VisitsFragment, this@VisitsFragment)
            rvOngoingVisits.layoutManager = LinearLayoutManager(requireContext())
            rvOngoingVisits.adapter = ongoingAdapter

            upcomingAdapter = UpcomingVisitsAdapter(requireContext(), this@VisitsFragment, this@VisitsFragment)
            rvUpcomingVisits.layoutManager = LinearLayoutManager(requireContext())
            rvUpcomingVisits.adapter = upcomingAdapter

            completeAdapter = CompleteVisitsAdapter(requireContext(), this@VisitsFragment)
            rvCompletedVisits.layoutManager = LinearLayoutManager(requireContext())
            rvCompletedVisits.adapter = completeAdapter

            notCompleteAdapter = NotCompleteVisitsAdapter(requireContext(), this@VisitsFragment, this@VisitsFragment)
            rvNotCompletedVisits.layoutManager = LinearLayoutManager(requireContext())
            rvNotCompletedVisits.adapter = notCompleteAdapter
        }
    }

    private fun setupSwipeRefresh() {
        val scope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            scope.launch {
                delay(2000)
                binding.swipeRefresh.isRefreshing = false
                callApi()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUI() {
        observeClock()
        handleWeekNavigation()
        toggleVisitSections()
        addCheckedDate(currentWeekStart)
        updateCalendar(currentWeekStart)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeClock() {
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val formatter = DateTimeFormatter.ofPattern("hh:mm:ss a")
                while (true) {
                    val now = LocalTime.now().format(formatter)
                    binding.currentDate.text = "Current time in UK : $now"
                    delay(1000)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleWeekNavigation() {
        binding.arrowLeft.setOnClickListener {
            val previousWeek = currentWeekStart.minusWeeks(1)
            if (previousWeek.isBefore(initialWeekStart.minusWeeks(maxWeeksBehind.toLong()))) {
                AlertUtils.showToast(requireActivity(), "Cannot navigate beyond the previous 3 weeks")
            } else {
                currentWeekStart = previousWeek
                addCheckedDate(currentWeekStart)
                updateCalendar(currentWeekStart)
            }
        }

        binding.arrowRight.setOnClickListener {
            val nextWeek = currentWeekStart.plusWeeks(1)
            if (nextWeek.isAfter(initialWeekStart.plusWeeks(maxWeeksAhead.toLong()))) {
                AlertUtils.showToast(requireActivity(), "Cannot navigate beyond the next 3 weeks")
            } else {
                currentWeekStart = nextWeek
                addCheckedDate(currentWeekStart)
                updateCalendar(currentWeekStart)
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun toggleVisitSections() {
        val toggle = { view: TextView, recycler: View ->
            val isVisible = view.tag == "Visible"
            view.tag = if (isVisible) "Invisible" else "Visible"
            recycler.visibility = if (isVisible) View.GONE else View.VISIBLE
            view.setCompoundDrawablesWithIntrinsicBounds(
                null, null,
                requireContext().getDrawable(if (isVisible) R.drawable.ic_keyboard_arrow_down else R.drawable.ic_keyboard_arrow_up),
                null
            )
        }

        with(binding) {
            tvOngoingVisits.setOnClickListener { toggle(tvOngoingVisits, rvOngoingVisits) }
            tvUpcomingVisits.setOnClickListener { toggle(tvUpcomingVisits, rvUpcomingVisits) }
            tvCompletedVisits.setOnClickListener { toggle(tvCompletedVisits, rvCompletedVisits) }
            tvNotCompletedVisits.setOnClickListener { toggle(tvNotCompletedVisits, rvNotCompletedVisits) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addCheckedDate(weekStart: LocalDate) {
        val weekEnd = weekStart.plusDays(6)
        selectedDate = if (LocalDate.now() in weekStart..weekEnd) LocalDate.now() else weekStart
        callApi()
    }

    @SuppressLint("NewApi")
    private fun callApi() {
        viewModel.getVisits(requireActivity(), selectedDate.toString())
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCalendar(weekStart: LocalDate) {
        val weekEnd = weekStart.plusDays(6)
        val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
        val yearFormat = DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH)

        binding.dateRangeText.text = "${weekStart.format(formatter)} to ${weekEnd.format(formatter)} ${weekEnd.format(yearFormat)}"
        binding.calendarContainer.removeAllViews()

        for (i in 0..6) {
            val date = weekStart.plusDays(i.toLong())
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.item_day, binding.calendarContainer, false)
            val dayName = view.findViewById<TextView>(R.id.dayName)
            val dayNumber = view.findViewById<TextView>(R.id.dayNumber)

            dayName.text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            dayNumber.text = date.dayOfMonth.toString()

            if (date == selectedDate) {
                view.setBackgroundResource(R.drawable.ic_day_select_bg)
                dayNumber.setTextColor(requireContext().getColor(R.color.white))
            } else {
                view.setBackgroundResource(R.drawable.ic_day_unselect_bg)
                dayNumber.setTextColor(requireContext().getColor(R.color.black))
            }

            view.setOnClickListener {
                selectedDate = date
                updateCalendar(currentWeekStart)
                callApi()
            }
            binding.calendarContainer.addView(view)
        }
    }

    private fun buildVisitItemList(
        visits: List<VisitListResponse.Data>,
        onComplete: (List<VisitItem>) -> Unit
    ) {
        val itemList = mutableListOf<VisitItem>()
        val pendingCalls = mutableListOf<Pair<Int, String>>() // index and travel time

        if (visits.isEmpty()) {
            onComplete(itemList)
            return
        }

        for (i in visits.indices) {
            itemList.add(VisitItem.VisitCard(visits[i]))

            if (i < visits.size - 1) {
                val originId = visits[i].placeId.toString()
                val destId = visits[i + 1].placeId.toString()

                fetchTravelTime(originId, destId) { travelTime ->
                    val label = travelTime ?: "Unknown"
                    pendingCalls.add(Pair(i, label))

                    // When all travel times have returned
                    if (pendingCalls.size == visits.size - 1) {
                        // Insert travel times into correct positions
                        pendingCalls.sortedBy { it.first }.forEachIndexed { index, pair ->
                            val insertIndex = (index * 2) + 1
                            itemList.add(insertIndex, VisitItem.TravelTimeIndicator(pair.second))
                        }
                        onComplete(itemList)
                    }
                }
            }
        }

        // If there's only 1 visit, complete immediately
        if (visits.size == 1) {
            onComplete(itemList)
        }
    }

    private fun fetchTravelTime(
        originPlaceId: String,
        destinationPlaceId: String,
        onResult: (String?) -> Unit
    ) {
        val origin = "place_id:$originPlaceId"
        val destination = "place_id:$destinationPlaceId"

        // Logging interceptor setup
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // or HEADERS, BASIC
        }

        // OkHttp client with logging
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .client(client) // attach logging-enabled client
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(GoogleApiService::class.java)

        apiService.getTravelTime(origin, destination, BuildConfig.GOOGLE_MAP_PLACES_API_KEY)
            .enqueue(object : Callback<DirectionsResponse> {
                override fun onResponse(
                    call: Call<DirectionsResponse>,
                    response: Response<DirectionsResponse>
                ) {
                    if (response.isSuccessful) {
                        val durationText = response.body()
                            ?.routes?.firstOrNull()
                            ?.legs?.firstOrNull()
                            ?.duration?.text
                        onResult(durationText)
                    } else {
                        onResult(null)
                    }
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    //showToast("Failed to fetch place details")
                    onResult(null)
                }
            })
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModels() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            ProgressLoader.toggle(requireActivity(), it)
        }

        viewModel.scheduledVisits.observe(viewLifecycleOwner) {
            binding.tvUpcomingVisits.text = getString(R.string.upcoming_visits) + " (${it?.size ?: 0})"
            //upcomingAdapter.updateList(it ?: emptyList())
//            val visitItems = buildVisitItemList(it ?: emptyList())
//            upcomingAdapter.updateList(visitItems)
            val visits = it ?: emptyList()
            buildVisitItemList(visits) { visitItems ->
                upcomingAdapter.updateList(visitItems)
            }
        }

        viewModel.inProgressVisits.observe(viewLifecycleOwner) {
            binding.tvOngoingVisits.text = getString(R.string.ongoing_visits) + " (${it?.size ?: 0})"
            ongoingAdapter.updateList(it ?: emptyList())
            upcomingAdapter.updatedUpcomingList(it ?: emptyList())
        }

        viewModel.completedVisits.observe(viewLifecycleOwner) {
            binding.tvCompletedVisits.text = getString(R.string.completed_visits) + " (${it?.size ?: 0})"
            completeAdapter.updateList(it ?: emptyList())
        }

        viewModel.notCompletedVisits.observe(viewLifecycleOwner) {
            binding.tvNotCompletedVisits.text = getString(R.string.not_completed_visits) + " (${it?.size ?: 0})"
            notCompleteAdapter.updateList(it ?: emptyList())
        }

        viewModel.visitsList.observe(viewLifecycleOwner) {
            binding.dataLayout.visibility = if (!it.isNullOrEmpty()) View.VISIBLE else View.GONE
            binding.emptyLayout.visibility = if (it.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        profileViewModel.userDetails.observe(viewLifecycleOwner) { it?.let(::updateProfileDetails) }
    }

    private fun updateProfileDetails(data: UserDetailsResponse.Data) {
        if (data.profile_photo.isNotEmpty()) {
            editor.putString(SharedPrefConstant.PROFILE_IMAGE, data.profile_photo).apply()
        }
        requireActivity().invalidateOptionsMenu()
    }

    override fun onItemItemClicked(data: VisitListResponse.Data) {
        val action = VisitsFragmentDirections.actionBottomVisitsToCheckOutFragment(data.visitDetailsId, 0)
        findNavController().navigate(action)
    }

    override fun ongoingItemItemClicked(data: VisitListResponse.Data) {
        val action = if (data.visitStatus == "Unscheduled") {
            VisitsFragmentDirections.actionBottomVisitsToUnscheduledVisitsDetailsFragmentFragment(data.visitDetailsId)
        } else {
            VisitsFragmentDirections.actionBottomVisitsToOngoingVisitsDetailsFragment(data.visitDetailsId)
        }
        findNavController().navigate(action)
    }

    override fun ongoingCheckoutItemItemClicked(data: VisitListResponse.Data) {
        viewModel.checkOutEligible(requireActivity(), data, findNavController())
    }

    override fun onDirectionItemItemClicked(data: VisitListResponse.Data) {
        val uri = data.placeId.let {
            "https://www.google.com/maps/search/?api=1&query=Google&query_place_id=$it".toUri()
        } ?: return AlertUtils.showToast(requireActivity(), "Client location not found")

        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            AlertUtils.showToast(requireActivity(), "Google Maps is not installed")
        }
    }

    override fun onViewItemItemClicked(data: VisitListResponse.Data) {
        val action = if (data.visitType == "Unscheduled") {
            VisitsFragmentDirections.actionBottomVisitsToUnscheduledVisitsDetailsFragmentFragment(data.visitDetailsId)
        } else {
            VisitsFragmentDirections.actionBottomVisitsToOngoingVisitsDetailsFragment(data.visitDetailsId)
        }
        findNavController().navigate(action)
    }
}
