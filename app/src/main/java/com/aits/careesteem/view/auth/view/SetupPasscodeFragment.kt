package com.aits.careesteem.view.auth.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aits.careesteem.databinding.FragmentSetupPasscodeBinding
import com.aits.careesteem.view.auth.model.CreateHashToken
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SetupPasscodeFragment : Fragment() {
    private var _binding: FragmentSetupPasscodeBinding? = null
    private val binding get() = _binding!!
    private val args: SetupPasscodeFragmentArgs by navArgs()
    private var userData: CreateHashToken.Data? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gson = Gson()
        userData = gson.fromJson(args.response, CreateHashToken.Data::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetupPasscodeBinding.inflate(inflater, container, false)
        setupWidgets()
        return binding.root
    }

    private fun setupWidgets() {
        binding.pinView.setOnCompletedListener = { passcode ->
            moveConfirm(passcode)
        }
    }

    private fun moveConfirm(passcode: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val direction =
                SetupPasscodeFragmentDirections.actionSetupPasscodeFragmentToConfirmPasscodeFragment(
                    response = args.response,
                    passcode = passcode
                )
            findNavController().navigate(direction)
        }
    }


}