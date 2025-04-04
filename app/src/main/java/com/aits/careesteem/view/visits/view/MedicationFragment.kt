package com.aits.careesteem.view.visits.view

import android.R
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.databinding.DialogMedicationUpdateBinding
import com.aits.careesteem.databinding.DialogTodoEditBinding
import com.aits.careesteem.databinding.FragmentMedicationBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.view.visits.adapter.MedicationListAdapter
import com.aits.careesteem.view.visits.adapter.MedicationPrnListAdapter
import com.aits.careesteem.view.visits.model.MedicationDetailsListResponse
import com.aits.careesteem.view.visits.viewmodel.MedicationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MedicationFragment : Fragment(), MedicationListAdapter.OnItemItemClick, MedicationPrnListAdapter.OnItemItemClick {
    private var _binding: FragmentMedicationBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: MedicationViewModel by viewModels()

    // Adapter
    private lateinit var medicationListAdapter: MedicationListAdapter
    private lateinit var medicationPnrListAdapter: MedicationPrnListAdapter

    private var id: String? = null
    private var clientId: String? = null
    private var isChanges = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the ID from the arguments
        id = arguments?.getString(ARG_VISIT_ID)
        clientId = arguments?.getString(ARG_CLIENT_ID)
        isChanges = arguments?.getBoolean(ARG_CHANGES)!!
    }

    companion object {
        private const val ARG_VISIT_ID = "ARG_VISIT_ID"
        private const val ARG_CLIENT_ID = "ARG_CLIENT_ID"
        private const val ARG_CHANGES = "ARG_CHANGES"
        @JvmStatic
        fun newInstance(paramVisitId: String, paramClientId: String, paramChanges: Boolean) =
            MedicationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_VISIT_ID, paramVisitId)
                    putString(ARG_CLIENT_ID, paramClientId)
                    putBoolean(ARG_CHANGES, paramChanges)
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
            viewModel.getMedicationDetails(requireActivity(), id.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMedicationBinding.inflate(inflater, container, false)
        setupAdapter()
        setupSwipeRefresh()
        setupViewModel()
        return binding.root
    }

    private fun setupAdapter() {
        medicationListAdapter = MedicationListAdapter(requireContext(), this@MedicationFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = medicationListAdapter

        medicationPnrListAdapter = MedicationPrnListAdapter(requireContext(), this@MedicationFragment)
        binding.recyclerViewPrn.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPrn.adapter = medicationPnrListAdapter
    }

    private fun setupSwipeRefresh() {
        val coroutineScope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            coroutineScope.launch {
                try {
                    delay(2000)
                    binding.swipeRefresh.isRefreshing = AppConstant.FALSE
                    viewModel.getMedicationDetails(requireActivity(), id.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
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
        viewModel.completeCount.observe(viewLifecycleOwner) { count ->
            if (count != null) {
                binding.submitCount.text = count.toString()
            }
        }

        // Data visibility
        viewModel.medicationList.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                binding.totalCount.text = data.size.toString()
                medicationListAdapter.updatedList(data)
            }
        }

        // Data visibility
        viewModel.prnMedicationList.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                binding.prnLayout.visibility = View.VISIBLE
                medicationPnrListAdapter.updatedList(data)
            } else {
                binding.prnLayout.visibility = View.GONE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onItemItemClicked(data: MedicationDetailsListResponse.Data) {
        if(!isChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed")
            return
        }

        val dialog = Dialog(requireContext())
        val binding: DialogMedicationUpdateBinding =
            DialogMedicationUpdateBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Add data
        binding.nhsMedicineName.text = data.nhs_medicine_name
        binding.medicationType.text = data.medication_type
        binding.medicationSupport.text = data.medication_support
        binding.doseQty.text = data.quantity_each_dose.toString()
        binding.medicationRoute.text = data.medication_route_name
        binding.frequencyMedication.text = data.day_name
        binding.medicationStatus.text = ""
        binding.medicationType.text = data.medication_type

        // Retrieve the list of statuses
        val statuses = AppConstant.getStatusesFromJson(requireContext())
        // Create ArrayAdapter
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, statuses)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        // Set adapter to Spinner
        binding.spinner.adapter = adapter

        // Retrieve previously selected status from SharedPreferences (or ViewModel)
        val selectedStatus = data.status

        // Find index of previously selected status
        val selectedIndex = statuses.indexOf(selectedStatus)

        // Set selected item in Spinner
        if (selectedIndex != -1) {
            binding.spinner.setSelection(selectedIndex)
        }

        binding.medicationStatus.setOnClickListener {
            binding.spinner.performClick()
        }

        // Handle selection
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                binding.medicationStatus.text = adapter.getItem(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Handle button clicks
        binding.closeButton.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnSave.setOnClickListener {
            if(binding.medicationStatus.text == "Select") {
                AlertUtils.showToast(requireActivity(), "Please select status")
                return@setOnClickListener
            }

            dialog.dismiss()
            when (data.medication_type) {
                "Blister Pack" -> {
                    viewModel.medicationBlisterPack(
                        activity = requireActivity(),
                        clientId = clientId.toString(),
                        visitDetailsId = id.toString(),
                        blisterPackDetailsId = data.blister_pack_details_id,
                        status = binding.medicationStatus.text.toString(),
                        carerNotes = binding.medicationNotes.text.toString()
                    )
                }
                "Scheduled" -> {
                    viewModel.medicationScheduled(
                        activity = requireActivity(),
                        clientId = clientId.toString(),
                        visitDetailsId = id.toString(),
                        scheduledDetailsId = data.scheduled_details_id,
                        status = binding.medicationStatus.text.toString(),
                        carerNotes = binding.medicationNotes.text.toString()
                    )
                }
                "PRN" -> {
                    viewModel.medicationPrn(
                        activity = requireActivity(),
                        visitDetailsId = id.toString(),
                        medicationDetails = data,
                        status = binding.medicationStatus.text.toString(),
                        carerNotes = binding.medicationNotes.text.toString()
                    )
                }
                else -> {
                    AlertUtils.showToast(requireActivity(), "Something went wrong")
                }
            }
        }
        binding.btnCancel.setOnClickListener {
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