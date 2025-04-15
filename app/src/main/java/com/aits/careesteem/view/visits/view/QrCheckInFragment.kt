package com.aits.careesteem.view.visits.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogForceCheckBinding
import com.aits.careesteem.databinding.FragmentQrCheckInBinding
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.home.view.HomeActivity
import com.aits.careesteem.view.visits.model.VisitDetailsResponse
import com.aits.careesteem.view.visits.viewmodel.CheckoutViewModel
import com.aits.careesteem.view.visits.viewmodel.OngoingVisitsDetailsViewModel
import com.google.android.gms.vision.CameraSource
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.camera.CameraSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class QrCheckInFragment : Fragment() {
    private var _binding: FragmentQrCheckInBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CheckoutViewModel by viewModels()
    private val ongoingVisitsDetailsViewModel: OngoingVisitsDetailsViewModel by viewModels()

    private var visitsDetails: VisitDetailsResponse.Data? = null
    private var visitDetailsString: String? = null
    private var action: Int? = 0

    private var cameraSource: CameraSource? = null
    private var qrScanning = false

    companion object {
        private const val REQUEST_CAMERA_PERMISSION_CODE = 1100
        private const val QR_SCAN_TIMEOUT = 5000L

        private const val ARG_VISIT_DETAILS = "ARG_VISIT_DETAILS"
        private const val ARG_ACTION = "ARG_ACTION"
        @JvmStatic
        fun newInstance(visitDetails: String, action: Int) =
            QrCheckInFragment().apply {
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
        _binding = FragmentQrCheckInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        showQrView()
    }

    private fun showQrView() {
        if (!isCameraPermissionGranted()) {
            requestCameraPermission()
            return
        }

        binding.layoutQr.visibility = View.VISIBLE
        startQrScanning()
    }

    private fun setupViewModel() {
        setupLoadingObservers()
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
        when (action) {
            0 -> handleCheckIn()
            1 -> handleCheckOut()
        }
    }

    private fun handleCheckIn() {
        visitsDetails?.let { data ->
            if (data.visitStatus == "Unscheduled") {
                viewModel.createUnscheduledVisit(requireActivity(), data.clientId, true)
            } else {
                showCheckInPopup(data)
            }
        }
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

    private fun handleCheckOut() {
        visitsDetails?.let { data ->
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

        val direction = if (visitsDetails?.visitStatus == "Unscheduled") {
            CheckOutFragmentDirections.actionCheckOutFragmentToUnscheduledVisitsDetailsFragmentFragment(
                visitsDetails!!.visitDetailsId
            )
        } else {
            CheckOutFragmentDirections.actionCheckOutFragmentToOngoingVisitsDetailsFragment(
                visitsDetails!!.visitDetailsId
            )
        }
        findNavController().navigate(direction, navOptions)
    }

    private fun navigateToHome() {
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
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
        visitsDetails?.let { data ->
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
        if(_binding == null) return

        val dialog = Dialog(requireContext()).apply {
            val binding = DialogForceCheckBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setCancelable(false)

            if (action == 0)
                binding.imgPopup.setImageResource(R.drawable.ic_force_check_in)
            else if (action == 0)
                binding.imgPopup.setImageResource(R.drawable.ic_force_check_out)

            binding.dialogTitle.text = if (action == 0)
                "Force Check In" else "Force Check Out"
            binding.dialogBody.text = if (action == 0)
                "GPS and QR Code didn’t work Do you want to force check-in?"
            else
                "GPS and QR Code didn’t work Do you want to force check-out?"

            binding.btnPositive.setOnClickListener {
                dismiss()
                //performForceCheck()

                val startTime = DateTimeUtils.getCurrentTimeGMT()
                val alertType = try {
                    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                    val actualTime = LocalTime.parse(startTime, formatter)

                    val plannedTime = when (action) {
                        0 -> visitsDetails?.plannedStartTime
                        1 -> visitsDetails?.plannedEndTime
                        else -> null
                    }?.let { LocalTime.parse(it, formatter) }

                    if (plannedTime != null) {
                        when (action) {
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

            binding.btnNegative.setOnClickListener { dismiss() }

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
                    "Early Check In" -> binding.dialogBody.text = "You’re checking in earlier than planned time. Do you want to continue?"
                    "Late Check In" -> binding.dialogBody.text = "You’re checking in later than planned time. Do you want to continue?"
                    "Early Check Out" -> binding.dialogBody.text = "You’re checking out earlier than planned time. Do you want to continue?"
                    "Late Check Out" -> binding.dialogBody.text = "You’re checking out later than planned time. Do you want to continue?"
                }

                binding.btnPositive.setOnClickListener {
                    dismiss()

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
        visitsDetails?.let { data ->
            when (action) {
                0 -> viewModel.addVisitCheckIn(requireActivity(), data, false,"")
                1 -> viewModel.updateVisitCheckOut(requireActivity(), data, false, "")
            }
        }
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
                    showQrView()
                } else {
                    showPermissionDeniedDialog()
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

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopQrScanning()
        _binding = null
    }

}