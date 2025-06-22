package com.aits.careesteem.view.unscheduled_visits.view

import android.annotation.SuppressLint
import android.app.Dialog
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
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogMedicationUpdateBinding
import com.aits.careesteem.databinding.DialogVisitNotesBinding
import com.aits.careesteem.databinding.FragmentUvMedicationBinding
import com.aits.careesteem.databinding.FragmentUvToDoBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.alerts.adapter.BodyMapImageAdapter
import com.aits.careesteem.view.alerts.adapter.BodyMapItem
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.unscheduled_visits.adapter.UvMedicationListAdapter
import com.aits.careesteem.view.unscheduled_visits.adapter.UvTodoListAdapter
import com.aits.careesteem.view.unscheduled_visits.model.UvMedicationListResponse
import com.aits.careesteem.view.unscheduled_visits.model.UvTodoListResponse
import com.aits.careesteem.view.unscheduled_visits.viewmodel.UvMedicationViewModel
import com.aits.careesteem.view.unscheduled_visits.viewmodel.UvToDoViewModel
import com.aits.careesteem.view.visits.adapter.MedicationListAdapter
import com.aits.careesteem.view.visits.model.MedicationDetailsListResponse
import com.aits.careesteem.view.visits.model.VisitDetailsResponse
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UvMedicationFragment : Fragment(), MedicationListAdapter.OnItemItemClick, UvMedicationListAdapter.OnPnrItemItemClick
{
    private var _binding: FragmentUvMedicationBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: UvMedicationViewModel by viewModels()

    // Adapter
    private lateinit var medicationListAdapter: MedicationListAdapter
    private lateinit var uvMedicationListAdapter: UvMedicationListAdapter

    private var mainList: List<MedicationDetailsListResponse.Data>? = null
    private var prnList: List<MedicationDetailsListResponse.Data>? = null

    private var visitDataString: String? = null
    private var isChanges = true

    private var visitData: VisitDetailsResponse.Data? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the ID from the arguments
        visitDataString = arguments?.getString(ARG_DATA)
        isChanges = arguments?.getBoolean(ARG_CHANGES)!!

        val gson = Gson()
        visitData = gson.fromJson(visitDataString, VisitDetailsResponse.Data::class.java)
    }

    companion object {
        private const val ARG_DATA = "ARG_DATA"
        private const val ARG_CHANGES = "ARG_CHANGES"
        @JvmStatic
        fun newInstance(param1: String, param2: Boolean) =
            UvMedicationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DATA, param1)
                    putBoolean(ARG_CHANGES, param2)
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
            //viewModel.getUvMedicationList(requireActivity(), id.toString())
            viewModel.getMedicationDetails(requireActivity(), visitData?.visitDetailsId.toString())
            viewModel.getMedicationPrnList(requireActivity(), visitData?.clientId.toString(), visitData?.visitDate.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUvMedicationBinding.inflate(inflater, container, false)
        setupWidget()
        setupAdapter()
        setupSwipeRefresh()
        setupViewModel()
        setupUi()
        return binding.root
    }

    private fun setupUi() {
//        binding.apply {
//            if(isChanges) {
//                //btnAddVisitNotes.visibility = View.VISIBLE
//                btnTopAddVisitNotes.visibility = View.VISIBLE
//            } else {
//                //btnAddVisitNotes.visibility = View.GONE
//                btnTopAddVisitNotes.visibility = View.GONE
//            }
//        }
    }

    private fun setupWidget() {
//        binding.apply {
//            btnAddVisitNotes.setOnClickListener {
//                addNotes()
//            }
//            btnTopAddVisitNotes.setOnClickListener {
//                addNotes()
//            }
//        }
    }

    private fun setupAdapter() {
        medicationListAdapter = MedicationListAdapter(requireContext(), this@UvMedicationFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = medicationListAdapter

        uvMedicationListAdapter = UvMedicationListAdapter(requireContext(), this@UvMedicationFragment)
        binding.recyclerViewPrn.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewPrn.adapter = uvMedicationListAdapter
    }

    private fun setupSwipeRefresh() {
        val coroutineScope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            coroutineScope.launch {
                try {
                    delay(2000)
                    binding.swipeRefresh.isRefreshing = AppConstant.FALSE
                    viewModel.getMedicationDetails(requireActivity(), visitData?.visitDetailsId.toString())
                    viewModel.getMedicationPrnList(requireActivity(), visitData?.clientId.toString(), visitData?.visitDate.toString())
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

//        // Data visibility
//        viewModel.medicationList.observe(viewLifecycleOwner) { data ->
//            if (data.isNotEmpty()) {
//                binding.apply {
//                    headerView.visibility = View.VISIBLE
//                    emptyLayout.visibility = View.GONE
//                    recyclerView.visibility = View.VISIBLE
//                }
//                //uvMedicationListAdapter.updateList(data)
//            } else {
//                binding.apply {
//                    headerView.visibility = View.GONE
//                    emptyLayout.visibility = View.VISIBLE
//                    recyclerView.visibility = View.GONE
//                    Glide.with(this@UvMedicationFragment)
//                        .asGif()
//                        .load(R.drawable.no_tablet) // Replace with your GIF resource
//                        .into(gifImageView)
//                }
//            }
//        }

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
                medicationListAdapter.updateList(it)
            }

            prnList?.let {
                uvMedicationListAdapter.updateList(it)
            }
        }
    }

    private fun showEmptyState() = with(binding) {
        recyclerView.visibility = View.GONE
        emptyLayout.visibility = View.VISIBLE
        Glide.with(this@UvMedicationFragment)
            .asGif()
            .load(com.aits.careesteem.R.drawable.no_tablet)
            .into(gifImageView)
    }

    @SuppressLint("SetTextI18n")
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

        if(data.body_image != null && data.body_image.isNotEmpty() && data.body_part_names != null && data.body_part_names.isNotEmpty()) {
            binding.tvBodyMap.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.VISIBLE

            // Map body parts with corresponding images
            val bodyMapItems = data.body_part_names.mapIndexed { index, name ->
                BodyMapItem(
                    partName = name,
                    imageUrl = data.body_image.getOrNull(index).orEmpty()
                )
            }

            // Setup nested image recycler
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = BodyMapImageAdapter(requireContext(), bodyMapItems)
        } else {
            binding.tvBodyMap.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
        }

        binding.medicationStatus.text = ""
        binding.medicationType.text = data.medication_type

        // Retrieve the list of statuses
        val statuses = AppConstant.getStatusesFromJson(requireContext())
        // Create ArrayAdapter
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
                AlertUtils.showToast(requireActivity(), "Please select status", ToastyType.WARNING)
                return@setOnClickListener
            }

            dialog.dismiss()
            viewModel.medicationPrn(
                activity = requireActivity(),
                visitDetailsId = visitData?.visitDetailsId.toString(),
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
        dialog.show()
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

        if(data.body_image != null && data.body_image.isNotEmpty() && data.body_part_names != null && data.body_part_names.isNotEmpty()) {
            binding.tvBodyMap.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.VISIBLE

            // Map body parts with corresponding images
            val bodyMapItems = data.body_part_names.mapIndexed { index, name ->
                BodyMapItem(
                    partName = name,
                    imageUrl = data.body_image.getOrNull(index).orEmpty()
                )
            }

            // Setup nested image recycler
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = BodyMapImageAdapter(requireContext(), bodyMapItems)
        } else {
            binding.tvBodyMap.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
        }

        binding.medicationStatus.text = ""
        binding.medicationType.text = data.medication_type

        // Retrieve the list of statuses
        val statuses = AppConstant.getStatusesFromJson(requireContext())
        // Create ArrayAdapter
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
                AlertUtils.showToast(requireActivity(), "Please select status", ToastyType.WARNING)
                return@setOnClickListener
            }

            dialog.dismiss()
            viewModel.medicationPrnUpdate(
                activity = requireActivity(),
                visitDetailsId = id.toString(),
                prnDetailsId = data.prn_details_id,
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
        dialog.show()
    }

//    @SuppressLint("SetTextI18n")
//    override fun onItemItemClicked(data: UvMedicationListResponse.Data) {
//        if(!isChanges) {
//            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
//            return
//        }
//
//        val dialog = BottomSheetDialog(requireContext())
//        val binding = DialogVisitNotesBinding.inflate(layoutInflater)
//        dialog.window?.setDimAmount(0.8f)
//        dialog.setContentView(binding.root)
//        dialog.setCancelable(AppConstant.TRUE)
//
//        // add data
//        binding.tvTopHeading.text = "Medication Notes"
//        binding.tvBelowHeading.text = "Medication Notes"
//        binding.visitNotes.hint = "Enter medication notes"
//        binding.visitNotes.text = Editable.Factory.getInstance().newEditable(data.medication_notes)
//
//        // Handle button clicks
//        binding.closeButton.setOnClickListener {
//            dialog.dismiss()
//        }
//        binding.btnCancel.setOnClickListener {
//            dialog.dismiss()
//        }
//        binding.btnSave.setOnClickListener {
//            if(binding.visitNotes.text.toString().isEmpty()) {
//                AlertUtils.showToast(requireActivity(), "Please enter medication notes", ToastyType.WARNING)
//                return@setOnClickListener
//            }
//            dialog.dismiss()
//            viewModel.updateNotes(
//                activity = requireActivity(),
//                visitDetailsId = id.toString(),
//                medicationId = data.id,
//                medicationNotes = binding.visitNotes.text.toString().trim()
//            )
//        }
//
//        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//        val window = dialog.window
//        window?.setLayout(
//            WindowManager.LayoutParams.MATCH_PARENT,
//            WindowManager.LayoutParams.WRAP_CONTENT
//        )
//        dialog.show()
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun addNotes() {
//        if(!isChanges) {
//            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
//            return
//        }
//
//        val dialog = BottomSheetDialog(requireContext())
//        val binding = DialogVisitNotesBinding.inflate(layoutInflater)
//        dialog.window?.setDimAmount(0.8f)
//        dialog.setContentView(binding.root)
//        dialog.setCancelable(AppConstant.TRUE)
//
//        // add data
//        binding.tvTopHeading.text = "Medication Notes"
//        binding.tvBelowHeading.text = "Medication Notes"
//        binding.visitNotes.hint = "Enter medication notes"
//
//        // Handle button clicks
//        binding.closeButton.setOnClickListener {
//            dialog.dismiss()
//        }
//        binding.btnCancel.setOnClickListener {
//            dialog.dismiss()
//        }
//        binding.btnSave.setOnClickListener {
//            if(binding.visitNotes.text.toString().isEmpty()) {
//                AlertUtils.showToast(requireActivity(), "Please enter medication notes", ToastyType.WARNING)
//                return@setOnClickListener
//            }
//            dialog.dismiss()
//            viewModel.addNotes(
//                activity = requireActivity(),
//                visitDetailsId = id.toString(),
//                medicationNotes = binding.visitNotes.text.toString().trim()
//            )
//        }
//
//        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//        val window = dialog.window
//        window?.setLayout(
//            WindowManager.LayoutParams.MATCH_PARENT,
//            WindowManager.LayoutParams.WRAP_CONTENT
//        )
//        dialog.show()
//    }


}