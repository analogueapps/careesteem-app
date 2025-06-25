package com.aits.careesteem.view.clients.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentAboutMeBinding
import com.aits.careesteem.databinding.FragmentUvMedicationBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.GooglePlaceHolder
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.clients.model.ClientDetailsResponse
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.clients.viewmodel.ClientDetailsViewModel
import com.aits.careesteem.view.unscheduled_visits.view.UvMedicationFragment
import com.bumptech.glide.Glide
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.notifyAll

@AndroidEntryPoint
class AboutMeFragment : Fragment() {
    private var _binding: FragmentAboutMeBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: ClientDetailsViewModel by viewModels()

    private var stringData: String? = null
    // Selected client object
    private lateinit var clientData: ClientsList.Data

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
        if(isVisible) {
            viewModel.getClientDetails(requireActivity(), clientData.id.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAboutMeBinding.inflate(inflater, container, false)
        setupViewModel()
        return binding.root
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
                        .placeholder(R.drawable.logo_preview)
                        .error(R.drawable.logo_preview)
                        .circleCrop() // Makes the image circular
                        .into(profileImage)
                } else {
                    val initials = GooglePlaceHolder().getInitialsSingle(clientData.full_name)
                    val initialsBitmap = GooglePlaceHolder().createInitialsAvatar(requireContext(), initials)
                    profileImage.setImageBitmap(initialsBitmap)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}