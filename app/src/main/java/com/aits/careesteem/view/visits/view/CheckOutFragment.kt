package com.aits.careesteem.view.visits.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aits.careesteem.BuildConfig
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogForceCheckBinding
import com.aits.careesteem.databinding.FragmentCheckOutBinding
import com.aits.careesteem.network.GoogleApiService
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.DateTimeUtils
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
import com.google.android.gms.vision.CameraSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.tabs.TabLayout
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.camera.CameraSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@AndroidEntryPoint
class CheckOutFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentCheckOutBinding? = null
    private val binding get() = _binding!!
    private val args: CheckOutFragmentArgs by navArgs()

    private val viewModel: CheckoutViewModel by viewModels()
    private val ongoingVisitsDetailsViewModel: OngoingVisitsDetailsViewModel by viewModels()

    private lateinit var googleMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private var destinationLatLng: LatLng? = null

    private var cameraSource: CameraSource? = null
    private var qrScanning = false

    companion object {
        private const val REQUEST_LOCATION_PERMISSION_CODE = 5555
        private const val REQUEST_CAMERA_PERMISSION_CODE = 1100
        private const val QR_SCAN_TIMEOUT = 5000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_api_key))
        }
        ongoingVisitsDetailsViewModel.getVisitDetails(requireActivity(), args.visitDetailsId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckOutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupUI()
    }

    private fun setupUI() {
        setupTabLayout()
        setupMap()
    }

    private fun setupTabLayout() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Geo Location"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("QR-Code"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showMapView()
                    1 -> showQrView()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showMapView() {
        binding.layoutMap.visibility = View.VISIBLE
        binding.layoutQr.visibility = View.GONE
        stopQrScanning()
    }

    private fun showQrView() {
        if (!isCameraPermissionGranted()) {
            requestCameraPermission()
            binding.tabLayout.getTabAt(0)?.select()
            return
        }

        binding.layoutMap.visibility = View.GONE
        binding.layoutQr.visibility = View.VISIBLE
        startQrScanning()
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun setupViewModel() {
        setupLoadingObservers()
        setupVisitDetailsObserver()
        setupLocationObservers()
        setupQrVerificationObservers()
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
        ongoingVisitsDetailsViewModel.visitsDetails.observe(viewLifecycleOwner) { data ->
            data?.let {
                updateUIForVisitDetails(it)
                requestLocationPermissions()
            }
        }
    }

    private fun updateUIForVisitDetails(data: VisitDetailsResponse.Data) {
//        binding.btnCheckIn.visibility = if (args.action == 0 && data.placeId.isEmpty()) View.GONE else View.VISIBLE
//        binding.btnCheckOut.visibility = if (args.action == 1 && data.placeId.isEmpty()) View.GONE else View.VISIBLE
        if (args.action == 0) {
            if (data.placeId.toString().isEmpty()) {
                binding.btnCheckIn.visibility = View.GONE
            } else {
                binding.btnCheckIn.visibility = View.VISIBLE
            }
        } else if (args.action == 1) {
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
            true,
            ""
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
            true,
            ""
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

    private fun setupQrVerificationObservers() {
        viewModel.qrVerified.observe(viewLifecycleOwner) { verified ->
            if (verified) {
                handleVerifiedQr()
            }
        }

        viewModel.isCheckOutEligible.observe(viewLifecycleOwner) { eligible ->
            if (eligible) {
                ongoingVisitsDetailsViewModel.visitsDetails.value?.let { data ->
                    viewModel.updateVisitCheckOut(requireActivity(), data, true,"")
                }
            }
        }
    }

    private fun handleVerifiedQr() {
        when (args.action) {
            0 -> handleCheckIn()
            1 -> handleCheckOut()
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
            viewModel.updateVisitCheckOut(requireActivity(), data, true,"")
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

        val direction = if (ongoingVisitsDetailsViewModel.visitsDetails.value?.visitStatus == "Unscheduled") {
            CheckOutFragmentDirections.actionCheckOutFragmentToUnscheduledVisitsDetailsFragmentFragment(
                args.visitDetailsId
            )
        } else {
            CheckOutFragmentDirections.actionCheckOutFragmentToOngoingVisitsDetailsFragment(
                args.visitDetailsId
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
        checkLocationPermissionAndEnable()
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
        }

        ongoingVisitsDetailsViewModel.visitsDetails.value?.let {
            if (it.placeId.isNotEmpty()) {
                requestLocationPermissions()
            }
        }
    }

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                enableMyLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkLocationPermissionAndEnable() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun enableMyLocation() {
        try {
            googleMap.isMyLocationEnabled = true
            googleMap.isMyLocationEnabled = true // This shows the blue dot for current location
            googleMap.uiSettings.isMyLocationButtonEnabled = false // Enable the button to move to the current location
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }


    private fun updateMarkerOnMap(latLng: LatLng) {
        if (!::googleMap.isInitialized) return

        googleMap.clear()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun startQrScanning() {
        if (qrScanning) return

        qrScanning = true
        val cameraSettings = CameraSettings().apply {
            requestedCameraId = 0
        }

        binding.qrView.barcodeView.cameraSettings = cameraSettings
        binding.qrView.decodeSingle(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                handleQrResult(result.text)
            }

            override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
        })
        binding.qrView.resume()

        viewLifecycleOwner.lifecycleScope.launch {
            delay(QR_SCAN_TIMEOUT)
            if (viewModel.isAutoCheckIn.value == true) {
                stopQrScanning()
                showCheckPopup()
            }
        }
    }

    private fun handleQrResult(qrText: String) {
        ongoingVisitsDetailsViewModel.visitsDetails.value?.let { data ->
            viewModel.verifyQrCode(requireActivity(), data.clientId, qrText)
        }
        stopQrScanning()
    }

    private fun stopQrScanning() {
        if (!qrScanning) return

        qrScanning = false
        binding.qrView.barcodeView.pause()
        binding.qrView.barcodeView.stopDecoding()
    }

    private fun showCheckPopup() {
        if (!isAdded) return
        if(binding.tabLayout.selectedTabPosition == 0) return

        val dialog = Dialog(requireContext()).apply {
            val binding = DialogForceCheckBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setCancelable(false)

            /*binding.dialogTitle.text = if (args.action == 0) "Force Check In" else "Force Check Out"
            binding.dialogBody.text = if (args.action == 0)
                "Are you sure want to force check in?"
            else
                "Are you sure want to force check out?"

            binding.btnPositive.setOnClickListener {
                dismiss()
                performForceCheck()
            }

            binding.btnNegative.setOnClickListener { dismiss() }*/

            if (args.action == 0)
                binding.imgPopup.setImageResource(R.drawable.ic_force_check_in)
            else if (args.action == 0)
                binding.imgPopup.setImageResource(R.drawable.ic_force_check_out)

            binding.dialogTitle.text = if (args.action == 0)
                "Force Check In" else "Force Check Out"
            binding.dialogBody.text = if (args.action == 0)
                "GPS and QR Code didn’t work\nDo you want to force check-in?"
            else
                "GPS and QR Code didn’t work\nDo you want to force check-out?"

            binding.btnPositive.setOnClickListener {
                dismiss()
                //performForceCheck()

                val startTime = DateTimeUtils.getCurrentTimeGMT()
                val alertType = try {
                    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                    val actualTime = LocalTime.parse(startTime, formatter)

                    val plannedTime = when (args.action) {
                        0 -> ongoingVisitsDetailsViewModel.visitsDetails.value?.plannedStartTime
                        1 -> ongoingVisitsDetailsViewModel.visitsDetails.value?.plannedEndTime
                        else -> null
                    }?.let { LocalTime.parse(it, formatter) }

                    if (plannedTime != null) {
                        when (args.action) {
                            0 -> when {
                                actualTime.isBefore(plannedTime) -> "Early Check In"
                                actualTime.isAfter(plannedTime)  -> "Late Check In"
                                else                              -> ""
                            }
                            1 -> when {
                                actualTime.isBefore(plannedTime) -> "Early Check Out"
                                actualTime.isAfter(plannedTime)  -> "Late Check Out"
                                else                              -> ""
                            }
                            else -> ""
                        }
                    } else {
                        ""
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    ""
                }

                showAnotherDialog(alertType)
            }

            binding.btnNegative.setOnClickListener {
                dismiss()
                navigateToHome()
            }

            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showAnotherDialog(alertType: String) {
        if(alertType.isNotEmpty()) {
            if (!isAdded) return

            val dialog = Dialog(requireContext()).apply {
                val binding = DialogForceCheckBinding.inflate(layoutInflater)
                setContentView(binding.root)
                setCancelable(false)

                binding.dialogTitle.text = alertType.toString()

                when (alertType) {
                    "Early Check In" -> binding.imgPopup.setImageResource(R.drawable.ic_early_check_in)
                    "Late Check In" -> binding.imgPopup.setImageResource(R.drawable.ic_late_check_in)
                    "Early Check Out" -> binding.imgPopup.setImageResource(R.drawable.ic_early_check_out)
                    "Late Check Out" -> binding.imgPopup.setImageResource(R.drawable.ic_late_check_out)
                }

                when (alertType) {
                    "Early Check In" -> binding.dialogBody.text = "You’re checking in earlier than planned time.\nDo you want to continue?"
                    "Late Check In" -> binding.dialogBody.text = "You’re checking in later than planned time.\nDo you want to continue?"
                    "Early Check Out" -> binding.dialogBody.text = "You’re checking out earlier than planned time.\nDo you want to continue?"
                    "Late Check Out" -> binding.dialogBody.text = "You’re checking out later than planned time.\nDo you want to continue?"
                }

                binding.btnPositive.setOnClickListener {
                    dismiss()
                    when (alertType) {
                        "Early Check In" -> viewModel.addVisitCheckIn(
                            requireActivity(),
                            ongoingVisitsDetailsViewModel.visitsDetails.value!!,
                            false,
                            "Early Check In"
                        )
                        "Late Check In" -> viewModel.addVisitCheckIn(
                            requireActivity(),
                            ongoingVisitsDetailsViewModel.visitsDetails.value!!,
                            false,
                            "Late Check In"
                        )
                        "Early Check Out" -> viewModel.updateVisitCheckOut(
                            requireActivity(),
                            ongoingVisitsDetailsViewModel.visitsDetails.value!!,
                            false,
                            "Early Check Out"
                        )
                        "Late Check Out" -> viewModel.updateVisitCheckOut(
                            requireActivity(),
                            ongoingVisitsDetailsViewModel.visitsDetails.value!!,
                            false,
                            "Late Check Out"
                        )
                    }
                }

                binding.btnNegative.setOnClickListener {
                    dismiss()
                    navigateToHome()
                }

                window?.setBackgroundDrawableResource(android.R.color.transparent)
                window?.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
            }
            dialog.show()
        } else {
            handleVerifiedQr()
        }
    }

    private fun performForceCheck() {
        ongoingVisitsDetailsViewModel.visitsDetails.value?.let { data ->
            when (args.action) {
                0 -> viewModel.addVisitCheckIn(requireActivity(), data, false,"")
                1 -> viewModel.updateVisitCheckOut(requireActivity(), data, false,"")
            }
        }
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
        val placeId = ongoingVisitsDetailsViewModel.visitsDetails.value?.placeId ?: return
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
                            ongoingVisitsDetailsViewModel.visitsDetails.value?.radius?.let { radius ->
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
                .title(ongoingVisitsDetailsViewModel.visitsDetails.value?.clientName)
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

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            showPermissionExplanationDialog()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Needed")
            .setMessage("Camera permission is needed to scan barcodes. Please grant the permission.")
            .setPositiveButton("OK") { _, _ ->
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION_CODE
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    binding.tabLayout.getTabAt(1)?.select()
                } else {
                    showPermissionDeniedDialog()
                }
            }
            REQUEST_LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkLocationServices()
                } else {
                    showPermissionDeniedLocationDialog()
                }
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Needed")
            .setMessage("Camera permission is needed to take photos. Please grant the permission in your device settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
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
                ongoingVisitsDetailsViewModel.visitsDetails.value?.radius?.let { radius ->
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
        stopQrScanning()
        _binding = null
    }
}