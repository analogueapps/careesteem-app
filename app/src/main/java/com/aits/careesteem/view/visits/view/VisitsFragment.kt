package com.aits.careesteem.view.visits.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.BuildConfig
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentVisitsBinding
import com.aits.careesteem.network.GoogleApiService
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.notification.model.NotificationListResponse
import com.aits.careesteem.view.notification.viewmodel.NotificationViewModel
import com.aits.careesteem.view.profile.model.UserDetailsResponse
import com.aits.careesteem.view.profile.viewmodel.ProfileViewModel
import com.aits.careesteem.view.unscheduled_visits.model.VisitItem
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
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
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

    @Inject
    lateinit var sharedPreferences: SharedPreferences
    @Inject
    lateinit var editor: SharedPreferences.Editor

    private val viewModel: VisitsViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val notificationViewModel: NotificationViewModel by activityViewModels()

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
        notificationViewModel.getNotificationList(requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitsBinding.inflate(inflater, container, false)
        setupAdapters()
        setupUI()
        observeViewModels()
        setupSwipeRefresh()
        return binding.root
    }

    private fun setupAdapters() {
        with(binding) {
            ongoingAdapter =
                OngoingVisitsAdapter(requireContext(), this@VisitsFragment, this@VisitsFragment)
            rvOngoingVisits.layoutManager = LinearLayoutManager(requireContext())
            rvOngoingVisits.adapter = ongoingAdapter

            upcomingAdapter =
                UpcomingVisitsAdapter(requireContext(), this@VisitsFragment, this@VisitsFragment)
            rvUpcomingVisits.layoutManager = LinearLayoutManager(requireContext())
            rvUpcomingVisits.adapter = upcomingAdapter

            completeAdapter = CompleteVisitsAdapter(requireContext(), this@VisitsFragment)
            rvCompletedVisits.layoutManager = LinearLayoutManager(requireContext())
            rvCompletedVisits.adapter = completeAdapter

            notCompleteAdapter =
                NotCompleteVisitsAdapter(requireContext(), this@VisitsFragment, this@VisitsFragment)
            rvNotCompletedVisits.layoutManager = LinearLayoutManager(requireContext())
            rvNotCompletedVisits.adapter = notCompleteAdapter
        }
    }

    private fun setupSwipeRefresh() {
        val scope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            scope.launch {
                delay(2000)
                _binding?.let {
                    it.swipeRefresh.isRefreshing = false
                    callApi()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUI() {
        //observeClock()
        handleWeekNavigation()
        toggleVisitSections()
        addCheckedDate(currentWeekStart)
        updateCalendar(currentWeekStart)
    }

    @SuppressLint("SetTextI18n")
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
                AlertUtils.showToast(
                    requireActivity(),
                    "Cannot navigate beyond the previous 3 weeks",
                    ToastyType.WARNING
                )
            } else {
                currentWeekStart = previousWeek
                addCheckedDate(currentWeekStart)
                updateCalendar(currentWeekStart)
            }
        }

        binding.arrowRight.setOnClickListener {
            val nextWeek = currentWeekStart.plusWeeks(1)
            if (nextWeek.isAfter(initialWeekStart.plusWeeks(maxWeeksAhead.toLong()))) {
                AlertUtils.showToast(
                    requireActivity(),
                    "Cannot navigate beyond the next 3 weeks",
                    ToastyType.WARNING
                )
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
                requireContext().getDrawable(if (isVisible) R.drawable.ic_keyboard_arrow_down_small else R.drawable.ic_keyboard_arrow_up_small),
                null
            )
        }

        with(binding) {
            tvOngoingVisits.setOnClickListener { toggle(tvOngoingVisits, rvOngoingVisits) }
            tvUpcomingVisits.setOnClickListener { toggle(tvUpcomingVisits, rvUpcomingVisits) }
            tvCompletedVisits.setOnClickListener { toggle(tvCompletedVisits, rvCompletedVisits) }
            tvNotCompletedVisits.setOnClickListener {
                toggle(
                    tvNotCompletedVisits,
                    rvNotCompletedVisits
                )
            }
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
        val formatter = DateTimeFormatter.ofPattern("MMMM d", Locale.UK)
        val yearFormat = DateTimeFormatter.ofPattern("yyyy", Locale.UK)

        binding.dateRangeText.text =
            "${weekStart.format(formatter)} to ${weekEnd.format(formatter)} ${
                weekEnd.format(yearFormat)
            }"
        binding.calendarContainer.removeAllViews()

        for (i in 0..6) {
            val date = weekStart.plusDays(i.toLong())
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_day, binding.calendarContainer, false)
            val dayName = view.findViewById<TextView>(R.id.dayName)
            val dayNumber = view.findViewById<TextView>(R.id.dayNumber)

            //dayName.text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.UK)
            dayName.text = date.dayOfWeek
                .getDisplayName(TextStyle.SHORT, Locale.UK)
                .take(2)
                .lowercase()
                .replaceFirstChar { it.uppercase() }
            dayNumber.text = date.dayOfMonth.toString()

            if (date == selectedDate) {
                view.setBackgroundResource(R.drawable.ic_day_select_bg)
                dayName.setTextColor(requireContext().getColor(R.color.white))
                dayNumber.setTextColor(requireContext().getColor(R.color.white))
            } else {
                view.setBackgroundResource(R.drawable.ic_day_unselect_bg)
                dayName.setTextColor(requireContext().getColor(R.color.black))
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

//    private suspend fun buildVisitItemListSuspending(visits: List<VisitListResponse.Data>): List<VisitItem> = withContext(Dispatchers.Default) {
//        val itemList = mutableListOf<VisitItem>()
//
//        if (visits.isEmpty()) return@withContext itemList
//        visits.forEach { itemList.add(VisitItem.VisitCard(it)) }
//        if (visits.size == 1) return@withContext itemList
//
//        for (i in 0 until visits.size - 1) {
//            val originPlaceId = visits[i].placeId
//            val destinationPlaceId = visits[i+1].placeId
//
//            // print orgin and destination place id
//            println("Origin Place ID: $originPlaceId")
//            println("Destination Place ID: $destinationPlaceId")
//
//            val travelTime = if (originPlaceId.isNotBlank() && destinationPlaceId.isNotBlank()) {
//                fetchTravelTime("place_id:$originPlaceId", "place_id:$destinationPlaceId") ?: "Unknown"
//            } else "Unknown"
//
//            val insertIndex = (i * 2) + 1
//            itemList.add(insertIndex, VisitItem.TravelTimeIndicator(travelTime))
//        }
//
//        return@withContext itemList
//    }
//
//    private suspend fun fetchTravelTime(origin: String, destination: String): String? = withContext(Dispatchers.IO) {
//        try {
//            val logging = HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.BODY
//            }
//
//            val client = OkHttpClient.Builder()
//                .addInterceptor(logging)
//                .build()
//
//            val retrofit = Retrofit.Builder()
//                .baseUrl("https://maps.googleapis.com/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(client)
//                .build()
//
//            val apiService = retrofit.create(GoogleApiService::class.java)
//            val response = apiService.getDistanceMatrix(origin, destination, BuildConfig.GOOGLE_MAP_PLACES_API_KEY)
//
//            if (response.isSuccessful) {
//                val body = response.body()
//                if (body?.status == "OK" && body.rows.isNotEmpty()) {
//                    val element = body.rows[0].elements.firstOrNull()
//                    if (element?.status == "OK") {
//                        return@withContext element.duration?.text
//                    }
//                }
//                return@withContext null
//            } else null
//        } catch (e: Exception) {
//            null
//        }
//    }

    private suspend fun buildVisitItemListSuspend(visits: List<VisitListResponse.Data>): List<VisitItem> {
        val itemList = mutableListOf<VisitItem>()

        if (visits.isEmpty()) return itemList

        for (i in visits.indices) {
            itemList.add(VisitItem.VisitCard(visits[i]))

            if (i < visits.size - 1) {
                val originId = visits[i].placeId.toString()
                val destId = visits[i + 1].placeId.toString()

                val travelTime = fetchTravelTimeSuspend(originId, destId) ?: "Unknown"
                itemList.add(VisitItem.TravelTimeIndicator(travelTime))
            }
        }

        return itemList
    }

    private suspend fun fetchTravelTimeSuspend(
        originPlaceId: String,
        destinationPlaceId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val origin = "place_id:$originPlaceId"
            val destination = "place_id:$destinationPlaceId"

            // Optional logging
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(GoogleApiService::class.java)
            val response =
                apiService.getTravelTime(origin, destination, BuildConfig.GOOGLE_MAP_PLACES_API_KEY)
            if (response.isSuccessful) {
                val body = response.body()
                body?.routes!!.firstOrNull()?.legs?.firstOrNull()?.duration?.text
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    private fun observeViewModels() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            ProgressLoader.toggle(requireActivity(), it)
        }

        viewModel.scheduledVisits.observe(viewLifecycleOwner) {
            val visits = it ?: emptyList()
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                val visitItems = buildVisitItemListSuspend(visits)
                upcomingAdapter.updateList(visitItems)
                if (!isAdded || view == null) return@launchWhenStarted
                // Update count after full list is built (including travel time items)
                binding.tvUpcomingVisits.text =
                    getString(R.string.upcoming_visits) + " (${visitItems.count { item -> item is VisitItem.VisitCard }})"
                // set auto open
                binding.tvUpcomingVisits.tag = "Visible"
                binding.rvUpcomingVisits.visibility = View.VISIBLE
                binding.tvUpcomingVisits.setCompoundDrawablesWithIntrinsicBounds(
                    null, null,
                    requireContext().getDrawable(R.drawable.ic_keyboard_arrow_up_small),
                    null
                )
            }
        }

        viewModel.inProgressVisits.observe(viewLifecycleOwner) {
            val visits = it ?: emptyList()
            upcomingAdapter.updatedUpcomingList(it ?: emptyList())
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                //val visitItems = buildVisitItemListSuspend(visits)
                val itemList = mutableListOf<VisitItem>()
                for (i in visits.indices) {
                    itemList.add(VisitItem.VisitCard(visits[i]))
                }
                ongoingAdapter.updateList(itemList)
                if (!isAdded || view == null) return@launchWhenStarted
                // Update count after full list is built (including travel time items)
                binding.tvOngoingVisits.text =
                    getString(R.string.ongoing_visits) + " (${itemList.count { item -> item is VisitItem.VisitCard }})"
            }
            if (visits.isEmpty()) {
                binding.tvOngoingVisits.visibility = View.GONE
                //binding.rvOngoingVisits.visibility = View.GONE
            } else {
                binding.tvOngoingVisits.visibility = View.VISIBLE
                //binding.rvOngoingVisits.visibility = View.VISIBLE
            }
        }

        viewModel.completedVisits.observe(viewLifecycleOwner) {
            val visits = it ?: emptyList()
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                //val visitItems = buildVisitItemListSuspend(visits)
                val itemList = mutableListOf<VisitItem>()
                for (i in visits.indices) {
                    itemList.add(VisitItem.VisitCard(visits[i]))
                }
                completeAdapter.updateList(itemList)
                if (!isAdded || view == null) return@launchWhenStarted
                // Update count after full list is built (including travel time items)
                binding.tvCompletedVisits.text =
                    getString(R.string.completed_visits) + " (${itemList.count { item -> item is VisitItem.VisitCard }})"
            }
            if (visits.isEmpty()) {
                binding.tvCompletedVisits.visibility = View.GONE
                //binding.rvCompletedVisits.visibility = View.GONE
            } else {
                binding.tvCompletedVisits.visibility = View.VISIBLE
                //binding.rvCompletedVisits.visibility = View.VISIBLE
            }
        }

        viewModel.notCompletedVisits.observe(viewLifecycleOwner) {
            val visits = it ?: emptyList()
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                //val visitItems = buildVisitItemListSuspend(visits)
                val itemList = mutableListOf<VisitItem>()
                for (i in visits.indices) {
                    itemList.add(VisitItem.VisitCard(visits[i]))
                }
                notCompleteAdapter.updateList(itemList)
                if (!isAdded || view == null) return@launchWhenStarted
                // Update count after full list is built (including travel time items)
                binding.tvNotCompletedVisits.text =
                    getString(R.string.not_completed_visits) + " (${itemList.count { item -> item is VisitItem.VisitCard }})"
            }
            if (visits.isEmpty()) {
                binding.tvNotCompletedVisits.visibility = View.GONE
                //binding.rvNotCompletedVisits.visibility = View.GONE
            } else {
                binding.tvNotCompletedVisits.visibility = View.VISIBLE
                //binding.rvNotCompletedVisits.visibility = View.VISIBLE
            }
        }

        viewModel.visitsList.observe(viewLifecycleOwner) {
            binding.dataLayout.visibility = if (!it.isNullOrEmpty()) View.VISIBLE else View.GONE
            binding.emptyLayout.visibility = if (it.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        profileViewModel.userDetails.observe(viewLifecycleOwner) { it?.let(::updateProfileDetails) }
        notificationViewModel.notificationList.observe(viewLifecycleOwner) { it?.let(::updateNotificationCount) }
    }

    private fun updateNotificationCount(list: List<NotificationListResponse.Data>) {
        editor.putString(SharedPrefConstant.NOTIFICATION_COUNT, list.size.toString()).apply()
        //editor.putString(SharedPrefConstant.NOTIFICATION_COUNT, "10").apply()
        requireActivity().invalidateOptionsMenu()
    }

    private fun updateProfileDetails(data: UserDetailsResponse.Data) {
        if (data.profile_image_url == null) return
        if (data.profile_image_url.isNotEmpty()) {
            editor.putString(SharedPrefConstant.PROFILE_IMAGE, data.profile_image_url).apply()
        }
        requireActivity().invalidateOptionsMenu()
    }

    override fun onItemItemClicked(data: VisitListResponse.Data) {
//        val action =
//            VisitsFragmentDirections.actionBottomVisitsToCheckOutFragment(data.visitDetailsId, 0)
//        findNavController().navigate(action)
        viewModel.checkInEligible(requireActivity(), data, 0,findNavController())
    }

    override fun ongoingItemItemClicked(data: VisitListResponse.Data) {
        val action = if (data.visitType == "Unscheduled") {
            VisitsFragmentDirections.actionBottomVisitsToUnscheduledVisitsDetailsFragmentFragment(
                data.visitDetailsId
            )
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
        } ?: return AlertUtils.showToast(
            requireActivity(),
            "Client location not found",
            ToastyType.ERROR
        )

        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            AlertUtils.showToast(
                requireActivity(),
                "Google Maps is not installed",
                ToastyType.ERROR
            )
        }
    }

    override fun onViewItemItemClicked(data: VisitListResponse.Data) {
        val action = if (data.visitType == "Unscheduled") {
            VisitsFragmentDirections.actionBottomVisitsToUnscheduledVisitsDetailsFragmentFragment(
                data.visitDetailsId
            )
        } else {
            VisitsFragmentDirections.actionBottomVisitsToOngoingVisitsDetailsFragment(data.visitDetailsId)
        }
        findNavController().navigate(action)
    }
}
