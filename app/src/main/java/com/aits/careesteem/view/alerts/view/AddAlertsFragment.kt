package com.aits.careesteem.view.alerts.view


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentAddAlertsBinding
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.alerts.viewmodel.AddAlertsViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddAlertsFragment : Fragment() {
    private var _binding: FragmentAddAlertsBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: AddAlertsViewModel by viewModels()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getClientsList(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddAlertsBinding.inflate(inflater, container, false)
        setupWidgets()
        setupViewModel()
        return binding.root
    }

    private fun setupWidgets() {
        binding.clientName.setOnClickListener {
            binding.allClientNameSpinner.performClick()
        }
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

        // Data visibility
        viewModel.clientsList.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                val spinnerList = ArrayList<String>()
                for(client in data) {
                    spinnerList.add(client.full_name)
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.allClientNameSpinner.adapter = adapter

                binding.allClientNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    @SuppressLint("SetTextI18n")
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View,
                        position: Int,
                        id: Long
                    ) {
                        binding.clientName.text = adapter.getItem(position).toString()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }
            }
        }
    }
}