package com.aits.careesteem.view.clients.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogCurrentGoingOnBinding
import com.aits.careesteem.databinding.DialogUnscheduledVisitBinding
import com.aits.careesteem.databinding.FragmentAboutMeBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.GooglePlaceHolder
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.clients.model.ClientDetailsResponse
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.clients.viewmodel.ClientDetailsViewModel
import com.aits.careesteem.view.visits.viewmodel.VisitsViewModel
import com.bumptech.glide.Glide
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.getValue

@AndroidEntryPoint
class AboutMeFragment : Fragment() {
    private var _binding: FragmentAboutMeBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: ClientDetailsViewModel by viewModels()

    private var stringData: String? = null

    // Selected client object
    private lateinit var clientData: ClientsList.Data


    private val visitViewModel: VisitsViewModel by activityViewModels()
    private var isRedirect = AppConstant.FALSE
    private var shouldHandleVisitCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the ID from the arguments
        stringData = arguments?.getString(ARG_DATA)
        val gson = Gson()
        clientData = gson.fromJson(stringData, ClientsList.Data::class.java)
    }

    companion object {
        private const val ARG_DATA = "ARG_DATA"

        @JvmStatic
        fun newInstance(param1: String) =
            AboutMeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DATA, param1)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (isVisible) {
            viewModel.getClientDetails(requireActivity(), clientData.id.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAboutMeBinding.inflate(inflater, container, false)
        setupWidget()
        setupViewModel()
        return binding.root
    }

    @SuppressLint("NewApi")
    private fun setupWidget() {
//        binding.btnCreateUnscheduledVisit.setOnClickListener {
//            shouldHandleVisitCheck = true
//            visitViewModel.getVisits(
//                requireActivity(),
//                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
//            )
//        }
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

        viewModel.aboutClient.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                setupData(data)
            }
        }

//        visitViewModel.visitCreated.observe(viewLifecycleOwner) { event ->
//            event?.getContentIfNotHandled()?.let { isSuccess ->
//                if (shouldHandleVisitCheck) {
//                    shouldHandleVisitCheck = false
//                    if (isSuccess) {
//                        showUnscheduledConfirmDialog()
//                    } else {
//                        //AlertUtils.showToast(requireActivity(), "You have ongoing visits", ToastyType.WARNING)
//                        val dialog = Dialog(requireContext()).apply {
//                            val binding = DialogCurrentGoingOnBinding.inflate(layoutInflater)
//                            setContentView(binding.root)
//                            setCancelable(false)
//
//                            binding.btnPositive.setOnClickListener {
//                                dismiss()
//                            }
//
//                            binding.closeButton.setOnClickListener {
//                                dismiss()
//                            }
//
//                            window?.setBackgroundDrawableResource(android.R.color.transparent)
//                            window?.setLayout(
//                                WindowManager.LayoutParams.MATCH_PARENT,
//                                WindowManager.LayoutParams.WRAP_CONTENT
//                            )
//                            window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
//                            window?.setDimAmount(0.8f)
//                        }
//                        dialog.show()
//                    }
//                }
//            }
//        }
    }

    private fun showUnscheduledConfirmDialog() {
        val dialog = Dialog(requireContext())
        val binding: DialogUnscheduledVisitBinding =
            DialogUnscheduledVisitBinding.inflate(layoutInflater)
        dialog.window?.setDimAmount(0.8f)
        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Handle button clicks
        binding.btnPositive.setOnClickListener {
            isRedirect = AppConstant.FALSE
            dialog.dismiss()
            //viewModel.createUnscheduledVisit(requireActivity(), clientData.id)

            val gson = Gson()
            val dataString = gson.toJson(clientData)
            val action =
                ClientsDetailsFragmentDirections.actionClientsDetailsFragmentToUvCheckInFragment(
                    clinetData = dataString
                )
            findNavController().navigate(action)
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
    }

    private fun setupData(data: ClientDetailsResponse.Data.AboutData) {
        try {
            binding.apply {
                tvGender.text = AppConstant.checkNull(data.gender)
                tvReligion.text = AppConstant.checkNull(data.religion)
                tvEthnicity.text = AppConstant.checkNull(data.ethnicity)
                profileName.text = AppConstant.checkClientName(clientData.full_name)
                dateOfBirth.text = AppConstant.checkNull(data.date_of_birth)
                profileAge.text = AppConstant.checkNull(data.age)
                nhsNumber.text = AppConstant.checkNull(data.nhs_number)
                maritalStatus.text = AppConstant.checkNull(data.marital_status)

                if (clientData.profile_image_url != null && clientData.profile_image_url.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(clientData.profile_image_url)
                        .override(400, 300)
                        
                        .error(R.drawable.logo_preview)
                        .circleCrop() // Makes the image circular
                        .into(profileImage)
                } else {
                    val initials = GooglePlaceHolder().getInitialsSingle(clientData.full_name)
                    val initialsBitmap =
                        GooglePlaceHolder().createInitialsAvatar(requireContext(), initials)
                    profileImage.setImageBitmap(initialsBitmap)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}