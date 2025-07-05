package com.aits.careesteem.view.visits.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
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
import com.aits.careesteem.view.bottomsheet.MedicationBottomSheetFragment
import com.aits.careesteem.view.bottomsheet.MedicationPrnBottomSheetFragment
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
class MedicationFragment : Fragment(),
    MedicationListAdapter.OnItemItemClick,
    MedicationPrnListAdapter.OnPnrItemItemClick,
    MedicationBottomSheetFragment.OnMedicationUpdateListener,
    MedicationPrnBottomSheetFragment.OnPrnStatusUpdateListener,
    MedicationPrnBottomSheetFragment.OnPrnMedicationUpdateListener
{
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
        if (isVisible) {
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

        medicationPnrListAdapter =
            MedicationPrnListAdapter(requireContext(), this@MedicationFragment)
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

        // Loading state observer
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) ProgressLoader.showProgress(requireActivity())
            else ProgressLoader.dismissProgress()
        }

        // Completed count observer
        viewModel.completeCount.observe(viewLifecycleOwner) { count ->
            binding.submitCount.text = count?.toString() ?: "0"
        }

        // Medication list observer
        viewModel.medicationList.observe(viewLifecycleOwner) { list ->
            mainList = list
            updateUI()
        }

        // PRN medication list observer
        viewModel.prnMedicationList.observe(viewLifecycleOwner) { list ->
            prnList = list
            updateUI()
        }

        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        binding.includedHeader.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.includedHeader.ivClear.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                // i want to take this one and parse to adapter
                medicationListAdapter.filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.includedHeader.ivClear.setOnClickListener {
            binding.includedHeader.etSearch.text.clear()
            binding.includedHeader.etSearch.clearFocus()
            binding.includedHeader.ivClear.visibility = View.GONE

            // Hide keyboard
            inputMethodManager.hideSoftInputFromWindow(binding.includedHeader.etSearch.windowToken, 0)
        }
    }

    private fun updateUI() = with(binding) {
        val isMainEmpty = mainList.isNullOrEmpty()
        val isPrnEmpty = prnList.isNullOrEmpty()

        // Show empty state if both lists are empty
        if (isMainEmpty && isPrnEmpty) {
            showEmptyState()
        } else {
            emptyLayout.visibility = View.GONE
        }

        // Main medication section
        recyclerView.visibility = if (isMainEmpty) View.GONE else View.VISIBLE
        headerView.visibility = if (isMainEmpty) View.GONE else View.VISIBLE

        mainList?.takeIf { it.isNotEmpty() }?.let {
            totalCount.text = it.size.toString()
            medicationListAdapter.updateList(it)
        }

        // PRN medication section
        prnLayout.visibility = if (isPrnEmpty) View.GONE else View.VISIBLE

        prnList?.takeIf { it.isNotEmpty() }?.let {
            medicationPnrListAdapter.updateList(it)
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
        if (!isChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
            return
        }

        when (data.medication_type?.trim()?.lowercase()) {
            "blister pack" -> {
                val bottomSheet = MedicationBottomSheetFragment.newInstance(data, id)
                bottomSheet.show(childFragmentManager, MedicationBottomSheetFragment.TAG)
            }

            "scheduled" -> {
                val bottomSheet = MedicationBottomSheetFragment.newInstance(data, id)
                bottomSheet.show(childFragmentManager, MedicationBottomSheetFragment.TAG)
            }

            "prn" -> {
                val bottomSheet = MedicationPrnBottomSheetFragment.newInstance(
                    data,
                    id,
                    MedicationPrnBottomSheetFragment.ACTION_MEDICATION_UPDATE
                )
                bottomSheet.show(childFragmentManager, MedicationPrnBottomSheetFragment.TAG)
            }

            else -> {
                AlertUtils.showToast(
                    requireActivity(),
                    "Something went wrong",
                    ToastyType.ERROR
                )
            }
        }
        AlertUtils.showLog("MedicationType", "Received type: '${data.medication_type}'")
    }

    override fun onPnrItemItemClicked(data: MedicationDetailsListResponse.Data) {
        if (!isChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
            return
        }

        val bottomSheet = MedicationPrnBottomSheetFragment.newInstance(data, id, MedicationPrnBottomSheetFragment.ACTION_STATUS_UPDATE)
        bottomSheet.show(childFragmentManager, MedicationPrnBottomSheetFragment.TAG)
    }

    override fun onPrnStatusChanged(
        status: String,
        notes: String,
        data: MedicationDetailsListResponse.Data,
        visitDetailsId: String
    ) {
        viewModel.medicationPrn(
            activity = requireActivity(),
            visitDetailsId = visitDetailsId,
            medicationDetails = data,
            status = status.toString(),
            carerNotes = notes.toString()
        )
    }

    override fun onPrnMedicationUpdated(
        status: String,
        notes: String,
        data: MedicationDetailsListResponse.Data,
        visitDetailsId: String
    ) {
        viewModel.medicationPrnUpdate(
            activity = requireActivity(),
            visitDetailsId = visitDetailsId,
            prnDetailsId = data.prn_details_id,
            status = status.toString(),
            carerNotes = notes.toString()
        )
    }

    override fun onMedicationUpdated(
        status: String,
        notes: String,
        data: MedicationDetailsListResponse.Data,
        visitDetailsId: String
    ) {
        when (data.medication_type) {
            "Blister Pack" -> {
                viewModel.medicationBlisterPack(
                    activity = requireActivity(),
                    clientId = clientId.toString(),
                    visitDetailsId = visitDetailsId,
                    blisterPackDetailsId = data.blister_pack_details_id,
                    status = status.toString(),
                    carerNotes = notes.toString()
                )
            }

            "Scheduled" -> {
                viewModel.medicationScheduled(
                    activity = requireActivity(),
                    clientId = clientId.toString(),
                    visitDetailsId = visitDetailsId,
                    scheduledDetailsId = data.scheduled_details_id,
                    status = status.toString(),
                    carerNotes = notes.toString()
                )
            }

            "PRN" -> {
                viewModel.medicationPrnUpdate(
                    activity = requireActivity(),
                    visitDetailsId = visitDetailsId,
                    prnDetailsId = data.prn_details_id,
                    status = status.toString(),
                    carerNotes = notes.toString()
                )
            }

            else -> {
                AlertUtils.showToast(
                    requireActivity(),
                    "Something went wrong",
                    ToastyType.ERROR
                )
            }
        }
    }
}