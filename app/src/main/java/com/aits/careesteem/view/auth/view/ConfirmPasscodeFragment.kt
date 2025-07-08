package com.aits.careesteem.view.auth.view

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentConfirmPasscodeBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.auth.model.CreateHashToken
import com.aits.careesteem.view.auth.viewmodel.PasscodeViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmPasscodeFragment : Fragment() {
    private var _binding: FragmentConfirmPasscodeBinding? = null
    private val binding get() = _binding!!
    private val args: ConfirmPasscodeFragmentArgs by navArgs()
    private val viewModel: PasscodeViewModel by viewModels()
    private var userData: CreateHashToken.Data? = null

    @Inject
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gson = Gson()
        userData = gson.fromJson(args.response, CreateHashToken.Data::class.java)
        viewModel.userData = userData
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfirmPasscodeBinding.inflate(inflater, container, false)
        setupViewmodel()
        setupWidgets()
        return binding.root
    }

    private fun setupWidgets() {
        binding.pinView.setOnCompletedListener = { passcode ->
            if (args.passcode == passcode) {
                addPinToServer(passcode)
            } else {
                AlertUtils.showToast(
                    requireActivity(),
                    "Passcode does not match.",
                    ToastyType.WARNING
                )
            }
        }
    }

    private fun addPinToServer(passcode: String) {
        viewModel.createPasscode(
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

        viewModel.createPasscodeResponse.observe(viewLifecycleOwner) { response ->
            if (response == true) {
                editor.putBoolean(SharedPrefConstant.IS_LOGGED, true)
                editor.putBoolean(SharedPrefConstant.LOCK_ENABLE, true)
                editor.putBoolean(SharedPrefConstant.NOTIFICATION_ENABLE, true)
                editor.apply()
                findNavController().navigate(R.id.preloaderFragment)
//                val intent = Intent(requireActivity(), HomeActivity::class.java)
//                startActivity(intent)
//                activity?.finish()
            }
        }
    }
}