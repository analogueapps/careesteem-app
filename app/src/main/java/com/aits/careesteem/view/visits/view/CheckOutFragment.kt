package com.aits.careesteem.view.visits.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.net.Uri.*
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aits.careesteem.BuildConfig
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentCheckOutBinding
import com.aits.careesteem.network.GoogleApiService
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.home.view.HomeActivity
import com.aits.careesteem.view.visits.model.DirectionsResponse
import com.aits.careesteem.view.visits.model.PlaceDetailsResponse
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.aits.careesteem.view.visits.viewmodel.CheckoutViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.camera.CameraSettings
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@AndroidEntryPoint
class CheckOutFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentCheckOutBinding? = null
    private val binding get() = _binding!!
    private val args: CheckOutFragmentArgs by navArgs()

    // Viewmodel
    private val viewModel: CheckoutViewModel by viewModels()

    private var visitData: VisitListResponse.Data? = null

    private lateinit var googleMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var destinationLatLng: LatLng

    private lateinit var startForResult: ActivityResultLauncher<Intent>

    companion object {
        private const val REQUEST_LOCATION_PERMISSION_CODE = 5555
        private const val REQUEST_CAMERA_PERMISSION_CODE = 1100
    }


    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        if(visitData?.placeId != null) {
            requestLocationPermissions()
        }
    }

    private fun requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION_CODE
            )
        } else {
            checkLocationServices()
        }
    }

    private fun checkLocationServices() {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            showLocationSettingsDialog()
        } else {
            viewModel.fetchCurrentLocation()
            getDestinationLatLng()
        }
    }

    private fun getDestinationLatLng() {
        // Create a logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY  // Log full request & response body
        }

        // Configure OkHttpClient
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(GoogleApiService::class.java)
        val call = apiService.getPlaceDetails(
            visitData?.placeId.toString(),
            BuildConfig.GOOGLE_MAP_PLACES_API_KEY
        )

        call.enqueue(object : Callback<PlaceDetailsResponse> {
            override fun onResponse(
                call: Call<PlaceDetailsResponse>,
                response: Response<PlaceDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()?.result
                    destinationLatLng = LatLng(
                        result?.geometry?.location?.lat ?: 0.0,
                        result?.geometry?.location?.lng ?: 0.0
                    )

                    //googleMap.addMarker(MarkerOptions().position(destinationLatLng).title("Destination"))
                    addMarkerAndRadius(
                        googleMap = googleMap,
                        destinationLatLng = destinationLatLng,
                        radius = visitData?.radius.toString().toDouble()
                    )
                    drawRoute(destinationLatLng)
                }
            }

            override fun onFailure(call: Call<PlaceDetailsResponse>, t: Throwable) {
                AlertUtils.showToast(requireActivity(), "Failed to fetch place details")
            }
        })
    }

    fun addMarkerAndRadius(googleMap: GoogleMap, destinationLatLng: LatLng, radius: Double) {
        // Add marker at destination
        googleMap.addMarker(
            MarkerOptions().position(destinationLatLng).title(visitData?.clientName).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)
            )
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

        // Move the marker **outside the radius**
        val outsideLatLng =
            moveMarkerOutsideRadius(destinationLatLng, radius + 50) // 50m beyond radius

//        googleMap.addMarker(
//            MarkerOptions()
//                .position(outsideLatLng)
//                .title("Outside Radius")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
//        )

        // Adjust map view
        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 15f))
    }

    // Function to move marker outside the radius
    private fun moveMarkerOutsideRadius(center: LatLng, distance: Double): LatLng {
        val earthRadius = 6371000.0 // meters
        val newLat = center.latitude + (distance / earthRadius) * (180 / Math.PI)
        val newLng = center.longitude
        return LatLng(newLat, newLng)
    }

    private fun drawRoute(destination: LatLng) {
        val origin =
            "${viewModel.markerPosition.value!!.latitude},${viewModel.markerPosition.value!!.longitude}"
        val dest = "${destination.latitude},${destination.longitude}"
        // Create a logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY  // Log full request & response body
        }

        // Configure OkHttpClient
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(GoogleApiService::class.java)
        val call =
            apiService.getDirections(origin, dest, BuildConfig.GOOGLE_MAP_PLACES_API_KEY, "driving")

        call.enqueue(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                if (response.isSuccessful) {
                    val route = response.body()?.routes?.firstOrNull()
                    if (route == null) {
                        return
                    }
                    val polylinePoints = route?.overview_polyline?.points


                    if (!polylinePoints.isNullOrEmpty()) {
                        val decodedPath = PolyUtil.decode(polylinePoints)

                        if (::googleMap.isInitialized) {
                            googleMap.addPolyline(
                                PolylineOptions()
                                    .addAll(decodedPath)
                                    .width(10f)
                                    .color(Color.BLUE)
                            )
                        }
                    } else {
                        AlertUtils.showLog("MapsActivity", "Polyline points are empty")
                    }
                } else {
                    AlertUtils.showLog("MapsActivity", "API Response Failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
//                Toast.makeText(requireContext(), "Failed to fetch directions", Toast.LENGTH_SHORT)
//                    .show()
                AlertUtils.showToast(requireActivity(), "Failed to fetch directions")
            }
        })
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

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openBarCodeScanner()
            } else {
                showPermissionDeniedDialog()
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationServices()
            } else {
                showPermissionDeniedLocationDialog()
            }
        }

    }

    private fun showPermissionDeniedLocationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Denied")
            .setMessage("Location permission is required to access this feature. Please enable it in settings.")
            .setPositiveButton("Open Settings") { dialog, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_api_key))
        }
        // Retrieve the JSON string from Safe Args.
        val dataString = args.visitData
        // Convert the JSON string back to your data model.
        visitData = Gson().fromJson(dataString, VisitListResponse.Data::class.java)
        //visitData?.placeId = "ChIJKyf0UR7ezTsRixgCwr0pnWE"
        visitData?.placeId = "ChIJic-rKAmayzsRmSLYGIIZtdE"
        visitData?.radius = 5000
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCheckOutBinding.inflate(inflater, container, false)
        setupIntentResult()
        setupWidget()
        setupViewModel()
        return binding.root
    }

    private fun setupIntentResult() {
        try {
            startForResult =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val data: Intent? = result.data
                        val scanResult = data?.getStringExtra("SCAN_RESULT")
                        if (scanResult != null) {
                            AlertUtils.showLog("scanResult", scanResult)
                            viewModel.verifyQrCode(requireActivity(), scanResult)
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("InflateParams")
    private fun setupWidget() {


        if (args.action == 0) {
            if (visitData?.placeId.toString().isEmpty()) {
                binding.btnCheckIn.visibility = View.GONE
            } else {
                binding.btnCheckIn.visibility = View.VISIBLE
            }
        } else if (args.action == 1) {
            if (visitData?.placeId.toString().isEmpty()) {
                binding.btnCheckOut.visibility = View.GONE
            } else {
                binding.btnCheckOut.visibility = View.VISIBLE
            }
        }

        val tabLayout: TabLayout = binding.tabLayout

        val tab1 = tabLayout.newTab().setText("Geo Location")
        val tab2 = tabLayout.newTab().setText("QR-Code")

        // Add tabs to TabLayout
        tabLayout.addTab(tab1)
        tabLayout.addTab(tab2)

        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            val textView = LayoutInflater.from(requireContext())
                .inflate(R.layout.lyt_tab_title, null) as TextView
            textView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Set tab text correctly
            textView.text = tab?.text
            tab?.customView = textView
        }

        // Set a listener for tab selection events
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Handle tab selection
                tab?.let {
                    when (it.position) {
                        0 -> {
                            // Handle Tab 1 selection
                            binding.apply {
                                layoutMap.visibility = View.VISIBLE
                                layoutQr.visibility = View.GONE
                            }
                        }

                        1 -> {
                            // Handle Tab 2 selection
                            //openBarCodeScanner()
                            binding.apply {
                                layoutMap.visibility = View.GONE
                                layoutQr.visibility = View.VISIBLE
                            }
                            val s = CameraSettings()
                            s.requestedCameraId = 0 // front/back/etc
                            binding.qrView.barcodeView.cameraSettings = s
                            binding.qrView.resume()

                            binding.qrView.decodeSingle(object : BarcodeCallback {
                                override fun barcodeResult(result: BarcodeResult) {
                                    Log.d("barcode result:", "$result")
                                    // do your thing with result
                                }

                                override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
                            })
                        }

                        else -> {}
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselection
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselection
            }
        })

        placesClient = Places.createClient(requireContext())

        // Load Google Map
        val mapFragment =
            childFragmentManager.findFragmentById(binding.map.id) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnCheckIn.setOnClickListener {
            val currentLocation = LatLng(
                viewModel.markerPosition.value!!.latitude,
                viewModel.markerPosition.value!!.longitude
            )  // Example: Your current location
            //val destinationLocation = LatLng(18.7858015, 77.9852853)  // Example: API response destination
            //val radius = 5000f  // Example: 5 km radius
            if (isWithinRadius(currentLocation, destinationLatLng, visitData?.radius.toString().toFloat())) {
                AlertUtils.showLog("LocationCheck", "Current location is within the radius!")
                if (visitData?.visitStatus == "Unscheduled") {
                    viewModel.createUnscheduledVisit(requireActivity(), visitData?.clientId!!)
                } else {
                    viewModel.addVisitCheckIn(
                        requireActivity(),
                        visitData?.clientId!!,
                        visitData?.visitDetailsId!!
                    )
                }
            } else {
                AlertUtils.showLog("LocationCheck", "Current location is OUTSIDE the radius.")
                //AlertUtils.showToast(requireActivity(), "Current location is OUTSIDE the radius.")
                AlertUtils.showToast(
                    requireActivity(),
                    "Your current location is not within the client's radius. Please reach out to the client's location for further assistance."
                )
            }

        }

        binding.btnCheckOut.setOnClickListener {
            val currentLocation = LatLng(
                viewModel.markerPosition.value!!.latitude,
                viewModel.markerPosition.value!!.longitude
            )  // Example: Your current location
            //val destinationLocation = LatLng(18.7858015, 77.9852853)  // Example: API response destination
            //val radius = 5000f  // Example: 5 km radius
            if (isWithinRadius(currentLocation, destinationLatLng, visitData?.radius.toString().toFloat())) {
                AlertUtils.showLog("LocationCheck", "Current location is within the radius!")
                viewModel.updateVisitCheckOut(
                    requireActivity(),
                    visitData?.actualEndTime!![0],
                )
            } else {
                AlertUtils.showLog("LocationCheck", "Current location is OUTSIDE the radius.")
                //AlertUtils.showToast(requireActivity(), "Current location is OUTSIDE the radius.")
                AlertUtils.showToast(
                    requireActivity(),
                    "Your current location is not within the client's radius. Please reach out to the client's location for further assistance."
                )
            }

        }
    }

    private fun isWithinRadius(
        currentLatLng: LatLng,
        destinationLatLng: LatLng,
        radiusMeters: Float
    ): Boolean {
        val results = FloatArray(1)

        // Calculate distance between current location and destination
        Location.distanceBetween(
            currentLatLng.latitude, currentLatLng.longitude,
            destinationLatLng.latitude, destinationLatLng.longitude,
            results
        )

        val distance = results[0] // Distance in meters
        AlertUtils.showLog("DistanceCheck", "Distance: $distance meters, Radius: $radiusMeters meters")

        return distance <= radiusMeters
    }

    private fun openBarCodeScanner() {
        if (isCameraPermissionGranted()) {
            val intent = Intent(requireContext(), CaptureActivity::class.java)
            startForResult.launch(intent)
        } else {
            requestCameraPermission()
        }
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
            showPermissionExplanationDialog()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Needed")
            .setMessage("Camera permission is needed to scan barcodes. Please grant the permission.")
            .setPositiveButton("OK") { _, _ ->
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION_CODE
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Needed")
            .setMessage("Camera permission is needed to take photos. Please grant the permission in your device settings.")
            .setPositiveButton("OK") { _, _ ->
                // Optionally open app settings
                openAppSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        //fetchPlaceDetails()
        // Observe marker position and update the map
        viewModel.markerPosition.observe(viewLifecycleOwner) { latLng ->
            // Enable blue dot (Current Location)
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                googleMap.isMyLocationEnabled = true
            }
            updateMarkerOnMap(latLng)
        }

        // Enable zoom controls & gestures
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isZoomGesturesEnabled = true
    }

    private fun updateMarkerOnMap(latLng: LatLng) {
        googleMap.clear()  // Clear existing markers
        googleMap.addMarker(MarkerOptions().position(latLng).draggable(true))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun fetchPlaceDetails() {
        val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)
        val request = FetchPlaceRequest.newInstance(visitData?.placeId.toString(), placeFields)
        //val request = FetchPlaceRequest.newInstance("ChIJKyf0UR7ezTsRixgCwr0pnWE", placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                val latLng = place.latLng

                if (latLng != null) {
                    googleMap.addMarker(MarkerOptions().position(latLng).title(place.name))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }

    }


    private fun setupViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                ProgressLoader.showProgress(requireActivity())
            } else {
                ProgressLoader.dismissProgress()
            }
        }

        viewModel.qrVerified.observe(viewLifecycleOwner) { verified ->
            if (verified) {
                if(args.action == 0) {
                    if (visitData?.visitStatus == "Unscheduled") {
                        viewModel.createUnscheduledVisit(requireActivity(), visitData?.clientId!!)
                    } else {
                        viewModel.addVisitCheckIn(
                            requireActivity(),
                            visitData?.clientId!!,
                            visitData?.visitDetailsId!!
                        )
                    }
                } else if(args.action == 1) {
                    viewModel.updateVisitCheckOut(
                        requireActivity(),
                        visitData?.actualEndTime!![0],
                    )
                }
            }
        }

        // add uv visit
        viewModel.userActualTimeData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                visitData?.visitDetailsId = data.visit_details_id
                visitData?.visitDate = data.created_at.substring(0, 10)
                visitData?.userId = "[${data.user_id}]"
                visitData?.clientId = data.client_id
                viewModel.addVisitCheckIn(requireActivity(), data.client_id, data.visit_details_id)
            }
        }

        viewModel.addVisitCheckInResponse.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(
                        R.id.checkOutFragment,
                        true
                    ) // This removes CheckOutFragment from the back stack
                    .build()

                if (visitData?.visitStatus == "Unscheduled") {
                    val direction =
                        CheckOutFragmentDirections.actionCheckOutFragmentToUnscheduledVisitsDetailsFragmentFragment(
                            visitData = Gson().toJson(visitData)
                        )
                    findNavController().navigate(direction, navOptions)
                } else {
                    val direction =
                        CheckOutFragmentDirections.actionCheckOutFragmentToOngoingVisitsDetailsFragment(
                            visitData = Gson().toJson(visitData)
                        )
                    findNavController().navigate(direction, navOptions)
                }
            }
        }

        viewModel.updateVisitCheckoutResponse.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                val intent = Intent(requireActivity(), HomeActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

}