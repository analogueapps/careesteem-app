package com.aits.careesteem.view.auth.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentEnterPasscodeBinding
import com.aits.careesteem.databinding.FragmentWelcomeBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.auth.model.SendOtpUserLoginResponse
import com.aits.careesteem.view.auth.viewmodel.PasscodeViewModel
import com.aits.careesteem.view.auth.viewmodel.WelcomeViewModel
import com.aits.careesteem.view.home.view.HomeActivity
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EnterPasscodeFragment : Fragment() {
    private var _binding: FragmentEnterPasscodeBinding? = null
    private val binding get() = _binding!!
    private val args: EnterPasscodeFragmentArgs by navArgs()
    private val viewModel: PasscodeViewModel by viewModels()
    private var userData: OtpVerifyResponse.Data? = null

    @Inject
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEnterPasscodeBinding.inflate(inflater, container, false)
        setupViewmodel()
        setupWidgets()
        return binding.root
    }

    private fun setupWidgets() {
        binding.pinView.setOnCompletedListener = { passcode ->
            addPinToServer(passcode)
        }

        binding.pinView.forgotPasscode.setOnClickListener {
            viewModel.forgotPasscode(requireActivity())
        }
    }

    private fun addPinToServer(passcode: String) {
        viewModel.loginViaPasscode(
            activity = requireActivity(),
            passcode = passcode
        )
    }

    private fun setupViewmodel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                ProgressLoader.showProgress(requireActivity())
            } else {
                ProgressLoader.dismissProgress()
            }
        }

        viewModel.isPasscodeVerified.observe(viewLifecycleOwner) { isPasscodeVerified ->
            if (isPasscodeVerified) {
                AlertUtils.showToast(requireActivity(),"Passcode verified successfully.")
                editor.putBoolean(SharedPrefConstant.IS_LOGGED, AppConstant.TRUE)
                editor.apply()
                val intent = Intent(requireActivity(), HomeActivity::class.java)
                startActivity(intent)
                activity?.finish()
            } else {
                AlertUtils.showToast(requireActivity(),"Passcode does not match.")
            }
        }

        viewModel.sendOtpUserLoginResponse.observe(viewLifecycleOwner, Observer { response ->
            if (response != null) {
                val gson = Gson()
                val dataString = gson.toJson(response.data)
                viewLifecycleOwner.lifecycleScope.launch {
                    val direction = EnterPasscodeFragmentDirections.actionEnterPasscodeFragmentToVerifyOtpFragment(
                        response = dataString, action = 2
                    )
                    findNavController().navigate(direction)
                }
            }
        })
    }

}