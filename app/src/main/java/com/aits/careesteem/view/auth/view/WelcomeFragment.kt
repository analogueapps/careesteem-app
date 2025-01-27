package com.aits.careesteem.view.auth.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aits.careesteem.databinding.FragmentWelcomeBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.auth.model.UserData
import com.aits.careesteem.view.auth.viewmodel.WelcomeViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
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
                        dataString
                    )
                    findNavController().navigate(direction)
                }
            }
        })
    }
}