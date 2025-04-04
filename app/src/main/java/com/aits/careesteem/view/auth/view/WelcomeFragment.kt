package com.aits.careesteem.view.auth.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentWelcomeBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.auth.viewmodel.WelcomeViewModel
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.gson.Gson
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WelcomeViewModel by viewModels()

    private val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

//    override fun onResume() {
//        super.onResume()
//        if(isVisible) {
//            initAutoDetect()
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        setupViewmodel()
        setupWidget()
        return binding.root
    }

    private fun initAutoDetect() {
        val request: GetPhoneNumberHintIntentRequest =
            GetPhoneNumberHintIntentRequest.builder().build()

        Identity.getSignInClient(requireActivity())
            .getPhoneNumberHintIntent(request)
            .addOnSuccessListener {
                phoneNumberHintIntentResultLauncher.launch(
                    IntentSenderRequest.Builder(it.intentSender).build()
                )
            }
            .addOnFailureListener {
                AlertUtils.showLog("Identity addOnFailureListener", "" + it.message)
            }
    }

    private val phoneNumberHintIntentResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            try {
                val phoneNumber = Identity.getSignInClient(requireActivity())
                    .getPhoneNumberFromIntent(result.data)
                //Do more stuff with phoneNumber
                val number = extractLocalPhoneNumber(phoneNumber)
                binding.etMobile.setText(number)
            } catch (e: Exception) {
                AlertUtils.showLog("phoneNumberHintIntentResultLauncher", "" + e.message.toString())
            }
        }

    private fun extractLocalPhoneNumber(phoneNumber: String?): String {
        val parsedNumber: Phonenumber.PhoneNumber = phoneNumberUtil.parse(phoneNumber, null)
        var localNumber =
            phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)

        // Remove any non-digit characters
        localNumber = localNumber.replace("\\D".toRegex(), "")

        // Remove leading '0'
        if (localNumber.startsWith("0")) {
            localNumber = localNumber.substring(1)
        }

        return localNumber
    }

    private fun setupWidget() {
        // Retrieve the list of statuses
        val statuses = AppConstant.getCountryList(requireContext())
        val spinnerList = ArrayList<String>()
        for (data in statuses) {
            spinnerList.add(data.country +" +"+ data.country_code)
        }
        // Create ArrayAdapter
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set adapter to Spinner
        binding.spinner.adapter = adapter

        // Find the country with ID 219 (for example)
        val countryId = 219

        // Find the index of the item corresponding to country ID 219
        val selectedItemPosition = statuses.indexOfFirst { it.id == countryId }

        // Check if the country was found and set it as the selected item in the Spinner
        if (selectedItemPosition != -1) {
            binding.spinner.setSelection(selectedItemPosition)
        }

        binding.spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                @SuppressLint("SetTextI18n")
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    binding.tvCountryCode.text = "+"+statuses[position].country_code
                    viewModel.setCountryCode(statuses[position].id)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        binding.tvCountryCode.setOnClickListener {
            binding.spinner.performClick()
        }
    }

    private fun setupViewmodel() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.ccp.setTypeFace(ResourcesCompat.getFont(requireContext(), R.font.lora_regular))
        binding.ccp.setOnCountryChangeListener {
            val countryCode = binding.ccp.selectedCountryNameCode // Example: "IN"
            //viewModel.setCountryCode(countryCode)
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                ProgressLoader.showProgress(requireActivity())
            } else {
                ProgressLoader.dismissProgress()
            }
        }

        viewModel.isRequestOtpApiCall.observe(viewLifecycleOwner, Observer { isSuccessful ->
            if (isSuccessful) {
                viewModel.callRequestOtpApi(requireActivity())
            } else {
                val errorMessage = viewModel.phoneNumberError.value
                errorMessage?.let {
                    AlertUtils.showToast(requireActivity(), it)
                }
            }
        })

        viewModel.sendOtpUserLoginResponse.observe(viewLifecycleOwner, Observer { response ->
            if (response != null) {
                val gson = Gson()
                val dataString = gson.toJson(response.data)
                viewLifecycleOwner.lifecycleScope.launch {
                    val direction = WelcomeFragmentDirections.actionWelcomeFragmentToVerifyOtpFragment(
                        response = dataString
                    )
                    findNavController().navigate(direction)
                }
            }
        })
    }
}