package com.aits.careesteem.view.auth.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentWelcomeBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.auth.viewmodel.WelcomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WelcomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        setupViewmodel()
        return binding.root
    }

    private fun setupViewmodel() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

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
                //viewModel.callRequestOtpApi(requireActivity())
                ProgressLoader.showProgress(requireActivity())
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    ProgressLoader.dismissProgress()
                    viewLifecycleOwner.lifecycleScope.launch {
                        val direction = WelcomeFragmentDirections.actionWelcomeFragmentToVerifyOtpFragment(
                            binding.etMobile.text.toString().trim()
                        )
                        findNavController().navigate(direction)
                    }
                }
            } else {
                val errorMessage = viewModel.phoneNumberError.value
                errorMessage?.let {
                    AlertUtils.showGlobalPositiveAlert(requireContext(), "Alert!",
                        it
                    )
                }
            }
        })

        viewModel.isRequestOtpSuccessful.observe(viewLifecycleOwner, Observer { isSuccessful ->
            if (isSuccessful) {

            }
        })
    }
}