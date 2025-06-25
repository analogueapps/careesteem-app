package com.aits.careesteem.view.visits.view

import com.aits.careesteem.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.databinding.DialogMedicationUpdateBinding
import com.aits.careesteem.databinding.FragmentMedicationBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.alerts.adapter.BodyMapImageAdapter
import com.aits.careesteem.view.alerts.adapter.BodyMapItem
import com.aits.careesteem.view.recyclerview.adapter.MedicationStatusAdapter
import com.aits.careesteem.view.visits.adapter.MedicationListAdapter
import com.aits.careesteem.view.visits.adapter.MedicationPrnListAdapter
import com.aits.careesteem.view.visits.model.MedicationDetailsListResponse
import com.aits.careesteem.view.visits.viewmodel.MedicationViewModel
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MedicationFragment : Fragment(), MedicationListAdapter.OnItemItemClick, MedicationPrnListAdapter.OnPnrItemItemClick {
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

    private var mainList: List<MedicationDetailsListResponse.Data>? = null
    private var prnList: List<MedicationDetailsListResponse.Data>? = null

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

        // Observe main list
        viewModel.medicationList.observe(viewLifecycleOwner) { list ->
            mainList = list
            updateUI()
        }

        // Observe PRN list
        viewModel.prnMedicationList.observe(viewLifecycleOwner) { list ->
            prnList = list
            updateUI()
        }
    }


    private fun updateUI() = with(binding) {
        val isMainEmpty = mainList.isNullOrEmpty()
        val isPrnEmpty = prnList.isNullOrEmpty()

        if (isMainEmpty && isPrnEmpty) {
            showEmptyState()
            recyclerView.visibility = if (isMainEmpty) View.GONE else View.VISIBLE
            headerView.visibility = if (isMainEmpty) View.GONE else View.VISIBLE
            prnLayout.visibility = if (isPrnEmpty) View.GONE else View.VISIBLE
        } else {
            emptyLayout.visibility = View.GONE
            recyclerView.visibility = if (isMainEmpty) View.GONE else View.VISIBLE
            headerView.visibility = if (isMainEmpty) View.GONE else View.VISIBLE
            prnLayout.visibility = if (isPrnEmpty) View.GONE else View.VISIBLE

            mainList?.let {
                totalCount.text = it.size.toString()
                medicationListAdapter.updateList(it)
            }

            prnList?.let {
                medicationPnrListAdapter.updateList(it)
            }
        }
    }

    private fun showEmptyState() = with(binding) {
        recyclerView.visibility = View.GONE
        emptyLayout.visibility = View.VISIBLE
        Glide.with(this@MedicationFragment)
            .asGif()
            .load(com.aits.careesteem.R.drawable.no_tablet)
            .into(gifImageView)
    }

    private fun showToDoList(list: List<MedicationDetailsListResponse.Data>) = with(binding) {
        emptyLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        binding.totalCount.text = list.size.toString()
        medicationListAdapter.updateList(list)
    }

    @SuppressLint("SetTextI18n")
    override fun onItemItemClicked(data: MedicationDetailsListResponse.Data) {
        if(!isChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
            return
        }

        val dialog = BottomSheetDialog(requireContext())
        val binding: DialogMedicationUpdateBinding =
            DialogMedicationUpdateBinding.inflate(layoutInflater)
        dialog.window?.setDimAmount(0.8f)
        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.TRUE)

//        // Set max height
//        val maxHeight = (resources.displayMetrics.heightPixels * 0.7).toInt()
//        binding.root.layoutParams = binding.root.layoutParams?.apply {
//            height = maxHeight
//        }
        binding.closeButton.setOnClickListener { dialog.dismiss() }
        // Add data
        binding.nhsMedicineName.text = data.nhs_medicine_name
        binding.medicationType.text = data.medication_type
        binding.medicationSupport.text = data.medication_support
        binding.doseQty.text = data.quantity_each_dose.toString()
        binding.medicationRoute.text = data.medication_route_name
        when (data.medication_type) {
            "Blister Pack" -> {
                binding.frequencyMedication.text = data.day_name
            }
            "Scheduled" -> {
                binding.frequencyMedication.text = data.day_name
            }
            "PRN" -> {
                binding.frequencyMedication.text = "${data.doses} Doses per ${data.dose_per} ${data.time_frame}"
            }
            else -> {
                binding.frequencyMedication.text = data.day_name
            }
        }

        if(data.body_map_image_url != null && data.body_map_image_url.isNotEmpty() && data.body_part_names != null && data.body_part_names.isNotEmpty()) {
            binding.tvBodyMap.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.VISIBLE

            // Map body parts with corresponding images
            val bodyMapItems = data.body_part_names.mapIndexed { index, name ->
                BodyMapItem(
                    partName = name,
                    imageUrl = data.body_map_image_url.getOrNull(index).orEmpty()
                )
            }

            // Setup nested image recycler
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = BodyMapImageAdapter(requireContext(), bodyMapItems)
        } else {
            binding.tvBodyMap.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
        }



        binding.medicationStatus.text = if(data.status == "Scheduled") "Select" else data.status
        binding.medicationType.text = data.medication_type

//        // Retrieve the list of statuses
//        val statuses = AppConstant.getStatusesFromJson(requireContext())
//        // Create ArrayAdapter
//        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, statuses)
//        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
//        // Set adapter to Spinner
//        binding.spinner.adapter = adapter
//
//        // Retrieve previously selected status from SharedPreferences (or ViewModel)
//        val selectedStatus = data.status
//
//        // Find index of previously selected status
//        val selectedIndex = statuses.indexOf(selectedStatus)
//
//        // Set selected item in Spinner
//        if (selectedIndex != -1) {
//            binding.spinner.setSelection(selectedIndex)
//        }
//
//        binding.medicationStatus.setOnClickListener {
//            binding.spinner.performClick()
//        }
//
//        // Handle selection
//        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
//                binding.medicationStatus.text = adapter.getItem(position)
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // Do nothing
//            }
//        }

        val statuses = AppConstant.getNewStatuses(requireContext())

        val adapter = MedicationStatusAdapter(statuses) { selected ->
            binding.medicationStatus.text = selected
            binding.rvStatus.visibility = View.GONE
        }

        binding.rvStatus.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStatus.adapter = adapter

        binding.medicationStatus.setOnClickListener {
            if (binding.rvStatus.isVisible) {
                binding.rvStatus.visibility = View.GONE
            } else {
                binding.rvStatus.visibility = View.VISIBLE
            }
        }

        // Handle button clicks
        binding.closeButton.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnSave.setOnClickListener {
            if(binding.medicationStatus.text == "Select") {
                AlertUtils.showToast(requireActivity(), "Please select status", ToastyType.WARNING)
                return@setOnClickListener
            }
            if (binding.medicationNotes.text.toString().trim().isEmpty()) {
                AlertUtils.showToast(requireActivity(), "Please enter notes", ToastyType.WARNING)
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
                    viewModel.medicationPrnUpdate(
                        activity = requireActivity(),
                        visitDetailsId = id.toString(),
                        prnDetailsId = data.prn_details_id,
                        status = binding.medicationStatus.text.toString(),
                        carerNotes = binding.medicationNotes.text.toString()
                    )
                }
                else -> {
                    AlertUtils.showToast(requireActivity(), "Something went wrong", ToastyType.ERROR)
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

        // ✅ Set Bottom Sheet max height to 75% of screen
        val bottomSheet = dialog.delegate.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.layoutParams?.height = (resources.displayMetrics.heightPixels * 0.75).toInt()
        bottomSheet?.requestLayout()

        dialog.show()
    }

    override fun onPnrItemItemClicked(data: MedicationDetailsListResponse.Data) {
        if(!isChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
            return
        }

        val dialog = BottomSheetDialog(requireContext())
        val binding: DialogMedicationUpdateBinding =
            DialogMedicationUpdateBinding.inflate(layoutInflater)
        dialog.window?.setDimAmount(0.8f)
        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.TRUE)

//        // Set max height
//        val maxHeight = (resources.displayMetrics.heightPixels * 0.7).toInt()
//        binding.root.layoutParams = binding.root.layoutParams?.apply {
//            height = maxHeight
//        }
        binding.closeButton.setOnClickListener { dialog.dismiss() }
        // Add data
        binding.nhsMedicineName.text = data.nhs_medicine_name
        binding.medicationType.text = data.medication_type
        binding.medicationSupport.text = data.medication_support
        binding.doseQty.text = data.quantity_each_dose.toString()
        binding.medicationRoute.text = data.medication_route_name
        when (data.medication_type) {
            "Blister Pack" -> {
                binding.frequencyMedication.text = data.day_name
            }
            "Scheduled" -> {
                binding.frequencyMedication.text = data.day_name
            }
            "PRN" -> {
                binding.frequencyMedication.text = "${data.doses} Doses per ${data.dose_per} ${data.time_frame}"
            }
            else -> {
                binding.frequencyMedication.text = data.day_name
            }
        }

        if(data.body_map_image_url != null && data.body_map_image_url.isNotEmpty() && data.body_part_names != null && data.body_part_names.isNotEmpty()) {
            binding.tvBodyMap.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.VISIBLE

            // Map body parts with corresponding images
            val bodyMapItems = data.body_part_names.mapIndexed { index, name ->
                BodyMapItem(
                    partName = name,
                    imageUrl = data.body_map_image_url.getOrNull(index).orEmpty()
                )
            }

            // Setup nested image recycler
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = BodyMapImageAdapter(requireContext(), bodyMapItems)
        } else {
            binding.tvBodyMap.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
        }

        binding.medicationStatus.text = if(data.status == "Scheduled") "Select" else data.status
        binding.medicationType.text = data.medication_type

//        // Retrieve the list of statuses
//        val statuses = AppConstant.getStatusesFromJson(requireContext())
//        // Create ArrayAdapter
//        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, statuses)
//        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
//        // Set adapter to Spinner
//        binding.spinner.adapter = adapter
//
//        // Retrieve previously selected status from SharedPreferences (or ViewModel)
//        val selectedStatus = data.status
//
//        // Find index of previously selected status
//        val selectedIndex = statuses.indexOf(selectedStatus)
//
//        // Set selected item in Spinner
//        if (selectedIndex != -1) {
//            binding.spinner.setSelection(selectedIndex)
//        }
//
//        binding.medicationStatus.setOnClickListener {
//            binding.spinner.performClick()
//        }
//
//        // Handle selection
//        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
//                binding.medicationStatus.text = adapter.getItem(position)
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // Do nothing
//            }
//        }

        val statuses = AppConstant.getNewStatuses(requireContext())

        val adapter = MedicationStatusAdapter(statuses) { selected ->
            binding.medicationStatus.text = selected
            binding.rvStatus.visibility = View.GONE
        }

        binding.rvStatus.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStatus.adapter = adapter

        binding.medicationStatus.setOnClickListener {
            if (binding.rvStatus.isVisible) {
                binding.rvStatus.visibility = View.GONE
            } else {
                binding.rvStatus.visibility = View.VISIBLE
            }
        }

        // Handle button clicks
        binding.closeButton.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnSave.setOnClickListener {
            if(binding.medicationStatus.text == "Select") {
                AlertUtils.showToast(requireActivity(), "Please select status", ToastyType.WARNING)
                return@setOnClickListener
            }
            if (binding.medicationNotes.text.toString().trim().isEmpty()) {
                AlertUtils.showToast(requireActivity(), "Please enter notes", ToastyType.WARNING)
                return@setOnClickListener
            }
            dialog.dismiss()
            viewModel.medicationPrn(
                activity = requireActivity(),
                visitDetailsId = id.toString(),
                medicationDetails = data,
                status = binding.medicationStatus.text.toString(),
                carerNotes = binding.medicationNotes.text.toString()
            )
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

        // ✅ Set Bottom Sheet max height to 75% of screen
        val bottomSheet = dialog.delegate.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.layoutParams?.height = (resources.displayMetrics.heightPixels * 0.75).toInt()
        bottomSheet?.requestLayout()

        dialog.show()
    }
}