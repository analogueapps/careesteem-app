package com.aits.careesteem.view.visits.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.Uri.*
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentCheckOutBinding
import com.aits.careesteem.databinding.FragmentOngoingVisitsDetailsBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.clients.view.ClientsDetailsFragmentDirections
import com.aits.careesteem.view.clients.viewmodel.ClientDetailsViewModel
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.aits.careesteem.view.visits.viewmodel.CheckoutViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.journeyapps.barcodescanner.CaptureActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class CheckOutFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentCheckOutBinding? = null
    private val binding get() = _binding!!
    private val args: CheckOutFragmentArgs by navArgs()

    // Viewmodel
    private val viewModel: CheckoutViewModel by viewModels()

    private lateinit var googleMap: GoogleMap
    private lateinit var placesClient: PlacesClient

    private lateinit var startForResult: ActivityResultLauncher<Intent>

    companion object {
        private const val REQUEST_CAMERA_PERMISSION_CODE = 1100
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private var visitData : VisitListResponse.Data? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the JSON string from Safe Args.
        val dataString = args.visitData
        // Convert the JSON string back to your data model.
        visitData = Gson().fromJson(dataString, VisitListResponse.Data::class.java)
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

//        if(visitData?.placeId.toString().isEmpty()) {
//            binding.btnCheckIn.visibility = View.GONE
//        } else {
//            binding.btnCheckIn.visibility = View.VISIBLE
//        }

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

                        }

                        1 -> {
                            // Handle Tab 2 selection
                            openBarCodeScanner()
                        }

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


        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_api_key))
        }
        placesClient = Places.createClient(requireContext())

        // Load Google Map
        val mapFragment =
            childFragmentManager.findFragmentById(binding.map.id) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnCheckIn.setOnClickListener {
            if(visitData?.visitStatus == "Unscheduled") {
                viewModel.createUnscheduledVisit(requireActivity(), visitData?.clientId!!)
            } else {
                viewModel.addVisitCheckIn(requireActivity(), visitData?.clientId!!, visitData?.visitDetailsId!!)
            }
        }
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
        }
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
        fetchPlaceDetails()
    }

    private fun fetchPlaceDetails() {
        val placeFields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)
        //val request = FetchPlaceRequest.newInstance(visitData?.placeId.toString(), placeFields)
        val request = FetchPlaceRequest.newInstance("ChIJmZp5zItte0gRQu0jLYmirb8", placeFields)

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
                if(visitData?.visitStatus == "Unscheduled") {
                    viewModel.createUnscheduledVisit(requireActivity(), visitData?.clientId!!)
                } else {
                    viewModel.addVisitCheckIn(requireActivity(), visitData?.clientId!!, visitData?.visitDetailsId!!)
                }
            }
        }

        // add uv visit
        viewModel.userActualTimeData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
//                val convertVisit = listOf(
//                    VisitListResponse.Data(
//                        clientId = data.client_id,
//                        visitDetailsId = data.visit_details_id,
//                        clientAddress = visitData?.clientAddress!!,
//                        clientName = visitData?.clientName!!,
//                        plannedEndTime = "",
//                        plannedStartTime = "",
//                        totalPlannedTime = "",
//                        userId = data.user_id,
//                        usersRequired = 1,
//                        visitDate = data.created_at.substring(0, 10),
//                        latitude = 0,
//                        longitude = 0,
//                        radius = 0,
//                        placeId = "",
//                        visitStatus = "Unscheduled"
//                    )
//                )

//                val action = ClientsDetailsFragmentDirections.actionClientsDetailsFragmentToUnscheduledVisitsDetailsFragmentFragment(Gson().toJson(convertVisit[0]))
//                findNavController().navigate(action)
                visitData?.visitDetailsId = data.visit_details_id
                visitData?.visitDate = data.created_at.substring(0, 10)
                visitData?.userId = "[${data.user_id}]"
                visitData?.clientId = data.client_id
                viewModel.addVisitCheckIn(requireActivity(), data.client_id, data.visit_details_id)
            }
        }

        viewModel.addVisitCheckInResponse.observe(viewLifecycleOwner) { data ->
            if (data != null) {
//                val convertVisit = listOf(
//                    VisitListResponse.Data(
//                        clientId = data[0].client_id,
//                        visitDetailsId = data[0].visit_details_id,
//                        clientAddress = visitData?.clientAddress!!,
//                        clientName = visitData?.clientName!!,
//                        plannedEndTime = visitData?.plannedEndTime!!,
//                        plannedStartTime = visitData?.plannedStartTime!!,
//                        totalPlannedTime = visitData?.totalPlannedTime!!,
//                        userId = data[0].user_id,
//                        usersRequired = visitData?.usersRequired!!,
//                        visitDate = visitData?.visitDate!!,
//                        latitude = visitData?.latitude!!,
//                        longitude = visitData?.longitude!!,
//                        radius = visitData?.radius!!,
//                        placeId = visitData?.placeId!!,
//                        visitStatus = visitData?.visitStatus!!
//                    )
//                )

                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.checkOutFragment, true) // This removes CheckOutFragment from the back stack
                    .build()

                if(visitData?.visitStatus == "Unscheduled") {
                    val direction = CheckOutFragmentDirections.actionCheckOutFragmentToUnscheduledVisitsDetailsFragmentFragment(
                        visitData = Gson().toJson(visitData)
                    )
                    findNavController().navigate(direction, navOptions)
                } else {
                    val direction = CheckOutFragmentDirections.actionCheckOutFragmentToOngoingVisitsDetailsFragment(
                        visitData = Gson().toJson(visitData)
                    )
                    findNavController().navigate(direction, navOptions)
                }
            }
        }
    }

}