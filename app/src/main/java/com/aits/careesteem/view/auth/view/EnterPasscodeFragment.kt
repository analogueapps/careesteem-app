package com.aits.careesteem.view.auth.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentEnterPasscodeBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.BiometricAuthListener
import com.aits.careesteem.utils.BiometricUtils
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.auth.viewmodel.PasscodeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EnterPasscodeFragment : Fragment(), BiometricAuthListener {
    private var _binding: FragmentEnterPasscodeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PasscodeViewModel by viewModels()
    private var userData: OtpVerifyResponse.Data? = null

    @Inject
    lateinit var sharedPreferences: SharedPreferences

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
        setupBiometric()
        return binding.root
    }

    private fun setupBiometric() {
        if (sharedPreferences.getBoolean(SharedPrefConstant.LOCK_ENABLE, false)) {
            // Post the biometric dialog to the main thread after fragment transactions finish
            binding.root.post {
                BiometricUtils.showBiometricPrompt(
                    activity = requireActivity() as AppCompatActivity,
                    listener = this,
                    cryptoObject = null,
                )
            }
        }
    }


    private fun setupWidgets() {
        if (sharedPreferences.getBoolean(SharedPrefConstant.LOCK_ENABLE, false)) {
            binding.pinView.fingerVisible = true
        } else {
            binding.pinView.fingerVisible = false
        }

        binding.pinView.setOnCompletedListener = { passcode ->
            addPinToServer(passcode)
        }

        binding.pinView.forgotPasscode.setOnClickListener {
            editor.clear()
            editor.apply()
            val intent = Intent(requireActivity(), AuthActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            editor.clear()
            editor.apply()
            val intent = Intent(requireActivity(), AuthActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        binding.pinView.onFingerprintClickListener = {
            //AlertUtils.showToast(requireActivity(), "Fingerprint clicked")
            BiometricUtils.showBiometricPrompt(
                activity = requireActivity() as AppCompatActivity,
                listener = this,
                cryptoObject = null,
            )
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
                AlertUtils.showToast(
                    requireActivity(),
                    "Passcode verified successfully.",
                    ToastyType.SUCCESS
                )
                editor.putBoolean(SharedPrefConstant.IS_LOGGED, AppConstant.TRUE)
                editor.apply()
//                val intent = Intent(requireActivity(), HomeActivity::class.java)
//                startActivity(intent)
//                activity?.finish()
                findNavController().navigate(R.id.preloaderFragment)
            } else {
                //AlertUtils.showToast(requireActivity(),"Passcode does not match.", ToastyType.WARNING)
            }
        }
    }

    override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
        when (error) {
            BiometricPrompt.ERROR_USER_CANCELED -> {

            }

            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {

            }
        }
    }

    override fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
        editor.putBoolean(SharedPrefConstant.IS_LOGGED, AppConstant.TRUE)
        editor.apply()
//        val intent = Intent(requireActivity(), HomeActivity::class.java)
//        startActivity(intent)
//        activity?.finish()
        findNavController().navigate(R.id.preloaderFragment)
    }

}