package com.aits.careesteem.view.visits.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.aits.careesteem.databinding.FragmentUvCheckInBinding
import com.aits.careesteem.network.GoogleApiService
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.GooglePlaceHolder
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.home.view.HomeActivity
import com.aits.careesteem.view.visits.model.PlaceDetailsResponse
import com.aits.careesteem.view.visits.viewmodel.CheckoutViewModel
import com.bumptech.glide.Glide
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
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.camera.CameraSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

@AndroidEntryPoint
class UvCheckInFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentUvCheckInBinding? = null
    private val binding get() = _binding!!
    private val args: UvCheckInFragmentArgs by navArgs()

    private val viewModel: CheckoutViewModel by viewModels()
    private lateinit var clientData: ClientsList.Data

    private lateinit var googleMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private var destinationLatLng: LatLng? = null
    private var qrScanning = false

    private var qrTimeoutJob: Job? = null

    @Inject
    lateinit var editor: SharedPreferences.Editor

    companion object {
        private const val REQUEST_LOCATION_PERMISSION_CODE = 5555
        private const val REQUEST_CAMERA_PERMISSION_CODE = 1100
        private const val QR_SCAN_TIMEOUT = 5000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializePlacesApi()
        parseClientData()
    }

    private fun initializePlacesApi() {
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_api_key))
        }
    }

    private fun parseClientData() {
        clientData = Gson().fromJson(args.clinetData, ClientsList.Data::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUvCheckInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupViewModel()
        setupMap()
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        binding.qrView.statusView.visibility = View.GONE
        setupTabLayout()
        setupCheckInButton()

        binding.textStatus.text = "Check in"
        binding.textName.text = AppConstant.checkClientName(clientData.full_name)

        if (clientData.profile_image_url != null && clientData.profile_image_url.isNotEmpty()) {
            Glide.with(requireContext())
                .load(clientData.profile_image_url)
                .override(400, 300)
                
                .error(R.drawable.logo_preview)
                .circleCrop() // Makes the image circular
                .into(binding.imageProfile)
        } else {
            val initials = GooglePlaceHolder().getInitialsSingle(clientData.full_name)
            val initialsBitmap =
                GooglePlaceHolder().createInitialsAvatar(requireContext(), initials)
            binding.imageProfile.setImageBitmap(initialsBitmap)
        }

    }

    private fun setupTabLayout() {
        val tabLayout: TabLayout = binding.tabLayout

        val tab1 = tabLayout.newTab().setText("Geo Location")
        val tab2 = tabLayout.newTab().setText("QR-Code")

        // Add tabs to TabLayout
        tabLayout.addTab(tab1)
        tabLayout.addTab(tab2)

        // Set custom views for tabs (no width adjustments needed)
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            val textView = LayoutInflater.from(requireContext())
                .inflate(R.layout.lyt_tab_title, null) as TextView
            textView.text = tab?.text
            tab?.customView = textView

            // Add this to update the initial state
            textView.isSelected = tab?.isSelected ?: false
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.isSelected = true
                when (tab?.position) {
                    0 -> {
                        qrTimeoutJob?.cancel() // Cancel previous
                        showMapView()
                    }

                    1 -> {
                        showQrView()
                        qrTimeoutJob?.cancel() // Cancel previous
                        qrTimeoutJob = viewLifecycleOwner.lifecycleScope.launch {
                            delay(QR_SCAN_TIMEOUT)
                            if (viewModel.isAutoCheckIn.value == true) {
                                stopQrScanning()
                                showCheckPopup()
                            }
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.customView?.isSelected = false
            }
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

    private fun setupCheckInButton() {
        //binding.btnCheckIn.visibility = if (clientData.place_id.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.btnCheckIn.setOnClickListener { handleCheckInClick() }
    }

    @SuppressLint("SetTextI18n")
    private fun handleCheckInClick() {
        viewModel.markerPosition.value?.let { currentLocation ->
            destinationLatLng?.let { destination ->
                clientData.radius.let { radius ->
                    if (isWithinRadius(currentLocation, destination, radius.toString().toFloat())) {
                        val dialog = Dialog(requireContext())
                        val binding: DialogForceCheckBinding =
                            DialogForceCheckBinding.inflate(layoutInflater)
                        dialog.window?.setDimAmount(0.8f)
                        dialog.setContentView(binding.root)
                        dialog.setCancelable(false)

                        binding.imgPopup.setImageResource(R.drawable.check)
                        binding.dialogTitle.text = "Check In"
                        binding.dialogBody.text =
                            "Are you sure you want to check in now?"
                        binding.btnPositive.text = "Yes"
                        binding.btnNegative.text = "No"

                        // Handle button clicks
                        binding.btnPositive.setOnClickListener {
                            dialog.dismiss()
                            proceedWithCheckIn()
                        }
                        binding.btnNegative.setOnClickListener {
                            dialog.dismiss()
                        }

                        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                        val window = dialog.window
                        window?.setLayout(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.WRAP_CONTENT
                        )
                        dialog.show()
                    } else {
                        showLocationOutOfRangeMessage()
                    }
                } ?: showRadiusNotAvailableMessage()
            } ?: showDestinationNotAvailableMessage()
        } ?: showLocationNotAvailableMessage()
    }

    private fun proceedWithCheckIn() {
        viewModel.createUnscheduledVisit(requireActivity(), clientData.id, true)
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun setupViewModel() {
        setupLoadingObserver()
        setupQrVerificationObserver()
        setupCheckInResponseObserver()
        setupLocationObserver()
    }

    private fun setupLoadingObserver() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) ProgressLoader.showProgress(requireActivity())
            else ProgressLoader.dismissProgress()
        }
    }

    private fun setupQrVerificationObserver() {
        viewModel.qrVerified.observe(viewLifecycleOwner) { verified ->
            if (verified) proceedWithCheckIn()
        }
    }

    private fun setupCheckInResponseObserver() {
        viewModel.addVisitCheckInResponse.observe(viewLifecycleOwner) { data ->
            data?.firstOrNull()?.let { navigateToVisitDetails(it.visit_details_id.toString()) }
        }

        viewModel.userActualTimeData.observe(viewLifecycleOwner) { data ->
            data?.let { navigateToVisitDetails(it.visit_details_id.toString()) }
        }
    }

    private fun navigateToVisitDetails(visitDetailsId: String) {
        editor.putBoolean(SharedPrefConstant.SHOW_PREVIOUS_NOTES, true)
        editor.apply()

        val direction = UvCheckInFragmentDirections
            .actionUvCheckInFragmentToUnscheduledVisitsDetailsFragmentFragment(
                visitDetailsId
            )

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.uvCheckInFragment, true)
            .build()

        findNavController().navigate(direction, navOptions)
    }

    private fun setupLocationObserver() {
        viewModel.markerPosition.observe(viewLifecycleOwner) { latLng ->
            latLng?.let {
                updateMarkerOnMap(it)
                if (clientData.place_id?.isNotEmpty() == true) {
                    getDestinationLatLng()
                }
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(map: GoogleMap) {
        googleMap = map.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isZoomGesturesEnabled = true
        }
        checkLocationPermissionAndEnable()
        if (clientData.place_id?.isNotEmpty() == true) {
            requestLocationPermissions()
        }
    }

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                enableMyLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location permission is required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun checkLocationPermissionAndEnable() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
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
            googleMap.uiSettings.isMyLocationButtonEnabled =
                false // Enable the button to move to the current location
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
        binding.qrView.apply {
            barcodeView.cameraSettings = CameraSettings().apply { requestedCameraId = 0 }

            decodeSingle(object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult) {
                    handleQrResult(result.text)
                }

                override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
            })
            resume()
        }

//        viewLifecycleOwner.lifecycleScope.launch {
//            delay(QR_SCAN_TIMEOUT)
//            if (viewModel.isAutoCheckIn.value == true) {
//                stopQrScanning()
//                showCheckPopup()
//            }
//        }
    }

    private fun handleQrResult(qrText: String) {
        viewModel.verifyQrCode(requireActivity(), clientData.id, qrText)
        stopQrScanning()
    }

    private fun stopQrScanning() {
        if (!qrScanning) return

        qrScanning = false
        binding.qrView.barcodeView.apply {
            pause()
            stopDecoding()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showCheckPopup() {
        if (!isAdded) return
        if (binding.tabLayout.selectedTabPosition == 0) return

        Dialog(requireContext()).apply {
            val binding = DialogForceCheckBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setCancelable(false)

            binding.apply {
                dialogTitle.text = "Force Check In"
                dialogBody.text = "Are you sure want to force check in?"

                btnPositive.setOnClickListener {
                    dismiss()
                    viewModel.createUnscheduledVisit(requireActivity(), clientData.id, false)
                }
                btnNegative.setOnClickListener {
                    dismiss()
                    navigateToHome()
                }
            }

            window?.apply {
                setBackgroundDrawableResource(android.R.color.transparent)
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
            }
            show()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun isWithinRadius(
        currentLatLng: LatLng,
        destinationLatLng: LatLng,
        radiusMeters: Float
    ): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(
            currentLatLng.latitude, currentLatLng.longitude,
            destinationLatLng.latitude, destinationLatLng.longitude,
            results
        )
        return results[0] <= radiusMeters
    }

    private fun getDestinationLatLng() {
        clientData.place_id?.takeIf { it.isNotEmpty() }?.let { placeId ->
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
                        response.body()?.result?.geometry?.location?.let { location ->
                            destinationLatLng = LatLng(location.lat, location.lng)
                            clientData.radius.let { radius ->
                                addMarkerAndRadius(
                                    destinationLatLng!!,
                                    radius.toString().toDouble()
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<PlaceDetailsResponse>, t: Throwable) {
                        //showToast("Failed to fetch place details")
                    }
                })
        }
    }

    private fun addMarkerAndRadius(destinationLatLng: LatLng, radius: Double) {
        if (!::googleMap.isInitialized) return

        googleMap.addMarker(
            MarkerOptions()
                .position(destinationLatLng)
                .title(clientData.full_name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        googleMap.addCircle(
            CircleOptions()
                .center(destinationLatLng)
                .radius(radius)
                .strokeColor(Color.MAGENTA)
                .fillColor(0x44FF4081)
                .strokeWidth(3f)
        )

        viewModel.markerPosition.value?.let { currentLatLng ->
            destinationLatLng.let { destLatLng ->
                clientData.radius.let { radius ->
                    if (isWithinRadius(
                            LatLng(currentLatLng.latitude, currentLatLng.longitude),
                            destLatLng,
                            radius.toString().toFloat()
                        )
                    ) {
                        if (binding != null && isAdded && view != null) {
                            binding.btnCheckIn.visibility = View.VISIBLE
                        }
                    } else {
                        if (binding != null && isAdded && view != null) {
                            binding.btnCheckIn.visibility = View.GONE
                        }
                    }
                } ?: showDestinationNotAvailableMessage()
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
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
            !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
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

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Needed")
            .setMessage("Camera permission is needed. Please grant the permission in your device settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPermissionDeniedLocationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Denied")
            .setMessage("Location permission is required. Please enable it in settings.")
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

    private fun showLocationOutOfRangeMessage() {
        showToast("Your current location is outside the client's designated radius. Please visit the client's location for assistance or try checking in using QR code verification.")
    }

    private fun showDestinationNotAvailableMessage() {
        showToast("Please check destination location and try again.")
    }

    private fun showLocationNotAvailableMessage() {
        showToast("Unfortunately, we are unable to detect your current location. Please enable your location manually and try again, or opt for QR verification.")
    }

    private fun showRadiusNotAvailableMessage() {
        showToast("Client radius information is not available.")
    }

    private fun showToast(message: String) {
        AlertUtils.showToast(requireActivity(), message, ToastyType.WARNING)
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        if (clientData.place_id?.isNotEmpty() == true) {
            requestLocationPermissions()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopQrScanning()
        _binding = null
    }
}