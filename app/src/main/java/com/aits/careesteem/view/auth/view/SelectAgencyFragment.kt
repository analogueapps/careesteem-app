package com.aits.careesteem.view.auth.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.databinding.DialogForceCheckBinding
import com.aits.careesteem.databinding.FragmentSelectAgencyBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.view.auth.adapter.SelectAgencyAdapter
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.auth.viewmodel.VerifyOtpViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectAgencyFragment : Fragment(), SelectAgencyAdapter.OnItemItemClick {
    private var _binding: FragmentSelectAgencyBinding? = null
    private val binding get() = _binding!!
    private val args: SelectAgencyFragmentArgs by navArgs()
    private val viewModel: VerifyOtpViewModel by viewModels()

    @Inject
    lateinit var editor: SharedPreferences.Editor

    private var dbList: List<OtpVerifyResponse.DbList>? = null

    private lateinit var selectAgencyAdapter: SelectAgencyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gson = Gson()
        // Assuming OtpVerifyResponse.DbList is the class you want to map to
        dbList = gson.fromJson(args.response, Array<OtpVerifyResponse.DbList>::class.java).toList()
        selectAgencyAdapter = SelectAgencyAdapter(requireContext(), this@SelectAgencyFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelectAgencyBinding.inflate(inflater, container, false)
        setupViewmodel()
        setupWidgets()
        return binding.root
    }

    private fun setupWidgets() {
        selectAgencyAdapter.updateList(dbList!!)
        binding.apply {
            recyclerView.adapter = selectAgencyAdapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
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

        viewModel.createHashToken.observe(viewLifecycleOwner, Observer { response ->
            if (response != null) {
                val gson = Gson()
                val dataString = gson.toJson(response.data[0])
                editor.putString(SharedPrefConstant.USER_DATA, dataString)
                editor.apply()
                viewLifecycleOwner.lifecycleScope.launch {
                    val direction =
                        SelectAgencyFragmentDirections.actionSelectAgencyFragmentToSetupPasscodeFragment(
                            response = dataString
                        )
                    findNavController().navigate(direction)
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    override fun onItemItemClicked(data: OtpVerifyResponse.DbList) {
        val dialog = Dialog(requireContext())
        val binding: DialogForceCheckBinding =
            DialogForceCheckBinding.inflate(layoutInflater)
        dialog.window?.setDimAmount(0.8f)
        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        binding.dialogTitle.text = "Agency Selection"
        binding.dialogBody.text =
            "Are you sure you want to log in with the ${data.agency_name} agency?"

        // Handle button clicks
        binding.btnPositive.setOnClickListener {
            dialog.dismiss()
            viewModel.onAgencySelected(requireActivity(), data)
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
}