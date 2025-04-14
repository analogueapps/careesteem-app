package com.aits.careesteem.view.visits.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.aits.careesteem.BuildConfig
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentMapCheckInBinding
import com.aits.careesteem.network.GoogleApiService
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.home.view.HomeActivity
import com.aits.careesteem.view.visits.model.PlaceDetailsResponse
import com.aits.careesteem.view.visits.model.VisitDetailsResponse
import com.aits.careesteem.view.visits.viewmodel.CheckoutViewModel
import com.aits.careesteem.view.visits.viewmodel.OngoingVisitsDetailsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class MapCheckInFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapCheckInBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CheckoutViewModel by viewModels()
    private val ongoingVisitsDetailsViewModel: OngoingVisitsDetailsViewModel by viewModels()

    private var visitsDetails: VisitDetailsResponse.Data? = null
    private var visitDetailsString: String? = null
    private var action: Int? = 0
    private var isChanges = true

    private lateinit var googleMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private var destinationLatLng: LatLng? = null

    companion object {
        private const val REQUEST_LOCATION_PERMISSION_CODE = 5555

        private const val ARG_VISIT_DETAILS = "ARG_VISIT_DETAILS"
        private const val ARG_ACTION = "ARG_ACTION"
        @JvmStatic
        fun newInstance(visitDetails: String, action: Int) =
            MapCheckInFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_VISIT_DETAILS, visitDetails)
                    putInt(ARG_ACTION, action)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_api_key))
        }

        visitDetailsString = arguments?.getString(ARG_VISIT_DETAILS)
        action = arguments?.getInt(ARG_ACTION)

        val gson = Gson()
        visitsDetails = gson.fromJson(visitDetailsString, VisitDetailsResponse.Data::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapCheckInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupUI()
    }

    private fun setupUI() {
        setupMap()
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun setupViewModel() {
        setupLoadingObservers()
        setupVisitDetailsObserver()
        setupLocationObservers()
        setupCheckInOutObservers()
    }

    private fun setupLoadingObservers() {
        ongoingVisitsDetailsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) ProgressLoader.showProgress(requireActivity())
            else ProgressLoader.dismissProgress()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) ProgressLoader.showProgress(requireActivity())
            else ProgressLoader.dismissProgress()
        }
    }

    private fun setupVisitDetailsObserver() {
        updateUIForVisitDetails(visitsDetails!!)
        requestLocationPermissions()
    }

    private fun updateUIForVisitDetails(data: VisitDetailsResponse.Data) {
        if (action == 0) {
            if (data.placeId.toString().isEmpty()) {
                binding.btnCheckIn.visibility = View.GONE
            } else {
                binding.btnCheckIn.visibility = View.VISIBLE
            }
        } else if (action == 1) {
            if (data.placeId.toString().isEmpty()) {
                binding.btnCheckOut.visibility = View.GONE
            } else {
                binding.btnCheckOut.visibility = View.VISIBLE
            }
        }

        binding.btnCheckIn.setOnClickListener {
            if(viewModel.markerPosition.value == null) {
                AlertUtils.showToast(requireActivity(), "Unfortunately, we are unable to detect your current location. Please enable your location manually and try again, or opt for QR verification.")
                return@setOnClickListener
            }
            checkLocationAndProceed { ->
                if (data?.visitStatus == "Unscheduled") {
                    viewModel.createUnscheduledVisit(requireActivity(), data?.clientId!!, true)
                } else {
                    showCheckInPopup(data)
                }
            }
        }

        binding.btnCheckOut.setOnClickListener {
            if(viewModel.markerPosition.value == null) {
                AlertUtils.showToast(requireActivity(), "Unfortunately, we are unable to detect your current location. Please enable your location manually and try again, or opt for QR verification.")
                return@setOnClickListener
            }
            checkLocationAndProceed { ->
                showCheckOutPopup(data)
            }
        }
    }

    private fun showCheckOutPopup(data: VisitDetailsResponse.Data) {
        viewModel.updateVisitCheckOut(
            requireActivity(),
            data,
            true
        )
        /*val startTime = DateTimeUtils.getCurrentTimeGMT()
//        val alertType =  {
//            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
//            val givenTime = LocalTime.parse(data.plannedEndTime.toString(), formatter)
//            val currentUtcTime = LocalTime.parse(startTime, formatter)
//            when {
//                currentUtcTime.isBefore(givenTime) -> "Early Check Out"
//                currentUtcTime.isAfter(givenTime) -> "Late Check Out"
//                else -> ""
//            }
//        }
        val alertType = try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

            // Parse the given time and current time
            val givenTime = LocalTime.parse(data.plannedEndTime.toString(), formatter)
            val currentUtcTime = LocalTime.parse(startTime, formatter)

            // Check the comparison between the current time and planned end time
            when {
                currentUtcTime.isBefore(givenTime) -> "Early Check Out"
                currentUtcTime.isAfter(givenTime) -> "Late Check Out"
                else -> ""
            }
        } catch (e: Exception) {
            // Handle the exception here (log it, show a message, etc.)
            e.printStackTrace()  // You can replace this with proper logging or message display
            "" // Return a default error message or handle it as needed
        }

        if(alertType.isNotEmpty()) {
            if (!isAdded) return

            val dialog = Dialog(requireContext()).apply {
                val binding = DialogForceCheckBinding.inflate(layoutInflater)
                setContentView(binding.root)
                setCancelable(false)

                binding.dialogTitle.text = alertType.toString()
                binding.dialogBody.text = if (alertType.toString() == "Early Check Out")
                    "You’re checking out earlier than planned time. Do you want to continue?"
                else
                    "You’re checking out later than planned time. Do you want to continue?\n"

                binding.btnPositive.setOnClickListener {
                    dismiss()
                    viewModel.updateVisitCheckOut(
                        requireActivity(),
                        data,
                        false
                    )
                }

                binding.btnNegative.setOnClickListener { dismiss() }

                window?.setBackgroundDrawableResource(android.R.color.transparent)
                window?.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
            }
            dialog.show()
        } else {
            viewModel.updateVisitCheckOut(
                requireActivity(),
                data,
                true
            )
        }*/
    }


    private fun showCheckInPopup(data: VisitDetailsResponse.Data) {
        viewModel.addVisitCheckIn(
            requireActivity(),
            data,
            true
        )
        /*val startTime = DateTimeUtils.getCurrentTimeGMT()
//        val alertType =  {
//            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
//            val givenTime = LocalTime.parse(data.plannedStartTime, formatter)
//            val currentUtcTime = LocalTime.parse(startTime, formatter)
//            when {
//                currentUtcTime.isBefore(givenTime) -> "Early Check In"
//                currentUtcTime.isAfter(givenTime) -> "Late Check In"
//                else -> ""
//            }
//        }
        val alertType = try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

            // Parse the given time and current time
            val givenTime = LocalTime.parse(data.plannedStartTime.toString(), formatter)
            val currentUtcTime = LocalTime.parse(startTime, formatter)

            // Check the comparison between the current time and planned end time
            when {
                currentUtcTime.isBefore(givenTime) -> "Early Check In"
                currentUtcTime.isAfter(givenTime) -> "Late Check In"
                else -> ""
            }
        } catch (e: Exception) {
            // Handle the exception here (log it, show a message, etc.)
            e.printStackTrace()  // You can replace this with proper logging or message display
            "" // Return a default error message or handle it as needed
        }

        if(alertType.isNotEmpty()) {
            if (!isAdded) return

            val dialog = Dialog(requireContext()).apply {
                val binding = DialogForceCheckBinding.inflate(layoutInflater)
                setContentView(binding.root)
                setCancelable(false)

                binding.dialogTitle.text = alertType.toString()
                binding.dialogBody.text = if (alertType.toString() == "Late Check In")
                    "You’re checking in later than planned time. Do you want to continue?"
                else
                    "You’re checking in earlier than planned time. Do you want to continue?"

                binding.btnPositive.setOnClickListener {
                    dismiss()
                    viewModel.addVisitCheckIn(
                        requireActivity(),
                        data,
                        false
                    )
                }

                binding.btnNegative.setOnClickListener { dismiss() }

                window?.setBackgroundDrawableResource(android.R.color.transparent)
                window?.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
            }
            dialog.show()
        } else {
            viewModel.addVisitCheckIn(
                requireActivity(),
                data,
                true
            )
        }*/
    }

    private fun setupLocationObservers() {
        viewModel.markerPosition.observe(viewLifecycleOwner) { latLng ->
            latLng?.let {
                updateMarkerOnMap(it)
                getDestinationLatLng()
            }
        }
    }

    private fun handleCheckIn() {
        ongoingVisitsDetailsViewModel.visitsDetails.value?.let { data ->
            if (data.visitStatus == "Unscheduled") {
                viewModel.createUnscheduledVisit(requireActivity(), data.clientId, true)
            } else {
                showCheckInPopup(data)
            }
        }
    }

    private fun handleCheckOut() {
        ongoingVisitsDetailsViewModel.visitsDetails.value?.let { data ->
            viewModel.updateVisitCheckOut(requireActivity(), data, true)
        }
    }

    private fun setupCheckInOutObservers() {
        viewModel.addVisitCheckInResponse.observe(viewLifecycleOwner) { data ->
            data?.let { navigateAfterCheckIn() }
        }

        viewModel.updateVisitCheckoutResponse.observe(viewLifecycleOwner) { data ->
            data?.let { navigateToHome() }
        }
    }

    private fun navigateAfterCheckIn() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.checkOutFragment, true)
            .build()

        val direction = if (visitsDetails?.visitStatus == "Unscheduled") {
            CheckOutFragmentDirections.actionCheckOutFragmentToUnscheduledVisitsDetailsFragmentFragment(
                visitDetailsId = visitsDetails!!.visitDetailsId
            )
        } else {
            CheckOutFragmentDirections.actionCheckOutFragmentToOngoingVisitsDetailsFragment(
                visitDetailsId = visitsDetails!!.visitDetailsId
            )
        }
        findNavController().navigate(direction, navOptions)
    }

    private fun navigateToHome() {
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
        }
        googleMap.isMyLocationEnabled = true // This shows the blue dot for current location
        googleMap.uiSettings.isMyLocationButtonEnabled = false // Enable the button to move to the current location

        visitsDetails?.let {
            if (it.placeId.isNotEmpty()) {
                requestLocationPermissions()
            }
        }
    }

    private fun updateMarkerOnMap(latLng: LatLng) {
        if (!::googleMap.isInitialized) return

        googleMap.clear()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun requestLocationPermissions() {
        if (hasLocationPermission()) {
            checkLocationServices()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION_CODE
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationServices() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            showLocationSettingsDialog()
        } else {
            viewModel.fetchCurrentLocation()
        }
    }

    private fun showLocationSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Enable Location")
            .setMessage("Location services are required to use this feature. Please enable GPS.")
            .setPositiveButton("Open Settings") { dialog, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                dialog.dismiss()
            }
            .show()
    }

    private fun getDestinationLatLng() {
        val placeId = visitsDetails?.placeId ?: return
        if (placeId.isEmpty()) return

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(GoogleApiService::class.java)
        apiService.getPlaceDetails(placeId, BuildConfig.GOOGLE_MAP_PLACES_API_KEY)
            .enqueue(object : Callback<PlaceDetailsResponse> {
                override fun onResponse(
                    call: Call<PlaceDetailsResponse>,
                    response: Response<PlaceDetailsResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.result?.geometry?.location?.let { location ->
                            destinationLatLng = LatLng(location.lat, location.lng)
                            visitsDetails?.radius?.let { radius ->
                                addMarkerAndRadius(
                                    googleMap,
                                    destinationLatLng!!,
                                    radius.toString().toDouble()
                                )
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<PlaceDetailsResponse>, t: Throwable) {
                    showToast("Failed to fetch place details")
                }
            })
    }

    private fun addMarkerAndRadius(googleMap: GoogleMap, destinationLatLng: LatLng, radius: Double) {

        googleMap.addMarker(
            MarkerOptions()
                .position(destinationLatLng)
                .title(visitsDetails?.clientName)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        // Draw radius
        googleMap.addCircle(
            CircleOptions()
                .center(destinationLatLng)
                .radius(radius) // Radius in meters
                .strokeColor(Color.MAGENTA)
                .fillColor(0x44FF4081) // Transparent fill
                .strokeWidth(3f)
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkLocationServices()
                } else {
                    showPermissionDeniedLocationDialog()
                }
            }
        }
    }

    private fun showPermissionDeniedLocationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Denied")
            .setMessage("Location permission is required to access this feature. Please enable it in settings.")
            .setPositiveButton("Open Settings") { dialog, _ ->
                openAppSettings()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
        startActivity(intent)
    }

    private fun checkLocationAndProceed(action: () -> Unit) {
        viewModel.markerPosition.value?.let { currentLatLng ->
            destinationLatLng?.let { destLatLng ->
                visitsDetails?.radius?.let { radius ->
                    if (isWithinRadius(
                            LatLng(currentLatLng.latitude, currentLatLng.longitude),
                            destLatLng,
                            radius.toString().toFloat()
                        )) {
                        action()
                    } else {
                        showLocationOutOfRangeMessage()
                    }
                } ?: showDestinationNotAvailableMessage()
            } ?: showDestinationNotAvailableMessage()
        } ?: showLocationNotAvailableMessage()
    }

    private fun isWithinRadius(currentLatLng: LatLng, destinationLatLng: LatLng, radiusMeters: Float): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(
            currentLatLng.latitude, currentLatLng.longitude,
            destinationLatLng.latitude, destinationLatLng.longitude,
            results
        )
        return results[0] <= radiusMeters
    }

    private fun showLocationOutOfRangeMessage() {
        showToast("Your current location is outside the client's designated radius. Please visit the client's location for assistance or try checking in/out using QR code verification.")
    }

    private fun showDestinationNotAvailableMessage() {
        showToast("Please check destination location and try again.")
    }

    private fun showLocationNotAvailableMessage() {
        showToast("Unfortunately, we are unable to detect your current location. Please enable your location manually and try again, or opt for QR verification.")
    }

    private fun showToast(message: String) {
        AlertUtils.showToast(requireActivity(), message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}