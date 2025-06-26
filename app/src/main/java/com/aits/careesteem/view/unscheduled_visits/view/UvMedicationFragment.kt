package com.aits.careesteem.view.unscheduled_visits.view

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
import com.aits.careesteem.databinding.FragmentUvMedicationBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.alerts.adapter.BodyMapImageAdapter
import com.aits.careesteem.view.alerts.adapter.BodyMapItem
import com.aits.careesteem.view.recyclerview.adapter.MedicationStatusAdapter
import com.aits.careesteem.view.unscheduled_visits.adapter.UvMedicationListAdapter
import com.aits.careesteem.view.unscheduled_visits.viewmodel.UvMedicationViewModel
import com.aits.careesteem.view.visits.adapter.MedicationListAdapter
import com.aits.careesteem.view.bottomsheet.MedicationBottomSheetFragment
import com.aits.careesteem.view.bottomsheet.MedicationPrnBottomSheetFragment
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
class UvMedicationFragment : Fragment(),
    MedicationListAdapter.OnItemItemClick,
    UvMedicationListAdapter.OnPnrItemItemClick,
    MedicationBottomSheetFragment.OnMedicationUpdateListener,
    MedicationPrnBottomSheetFragment.OnPrnStatusUpdateListener,
    MedicationPrnBottomSheetFragment.OnPrnMedicationUpdateListener
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
        if (isVisible) {
            //viewModel.getUvMedicationList(requireActivity(), id.toString())
            viewModel.getMedicationDetails(requireActivity(), visitData?.visitDetailsId.toString())
            viewModel.getMedicationPrnList(
                requireActivity(),
                visitData?.clientId.toString(),
                visitData?.visitDate.toString()
            )
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

        uvMedicationListAdapter =
            UvMedicationListAdapter(requireContext(), this@UvMedicationFragment)
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
                    viewModel.getMedicationDetails(
                        requireActivity(),
                        visitData?.visitDetailsId.toString()
                    )
                    viewModel.getMedicationPrnList(
                        requireActivity(),
                        visitData?.clientId.toString(),
                        visitData?.visitDate.toString()
                    )
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
        if (!isChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
            return
        }

        val bottomSheet = MedicationPrnBottomSheetFragment.newInstance(data, visitData?.visitDetailsId, MedicationPrnBottomSheetFragment.ACTION_STATUS_UPDATE)
        bottomSheet.show(childFragmentManager, MedicationPrnBottomSheetFragment.TAG)
    }

    @SuppressLint("SetTextI18n")
    override fun onItemItemClicked(data: MedicationDetailsListResponse.Data) {
        if (!isChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
            return
        }

        when (data.medication_type?.trim()?.lowercase()) {
            "blister pack" -> {
                val bottomSheet = MedicationBottomSheetFragment.newInstance(data,
                    visitData?.visitDetailsId
                )
                bottomSheet.show(childFragmentManager, MedicationBottomSheetFragment.TAG)
            }

            "scheduled" -> {
                val bottomSheet = MedicationBottomSheetFragment.newInstance(data, visitData?.visitDetailsId)
                bottomSheet.show(childFragmentManager, MedicationBottomSheetFragment.TAG)
            }

            "prn" -> {
                val bottomSheet = MedicationPrnBottomSheetFragment.newInstance(
                    data,
                    visitData?.visitDetailsId,
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

    override fun onPrnStatusChanged(
        status: String,
        notes: String,
        data: MedicationDetailsListResponse.Data,
        visitDetailsId: String
    ) {
        viewModel.medicationPrn(
            activity = requireActivity(),
            visitDetailsId = id.toString(),
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
            visitDetailsId = id.toString(),
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
        viewModel.medicationPrnUpdate(
            activity = requireActivity(),
            visitDetailsId = id.toString(),
            prnDetailsId = data.prn_details_id,
            status = status.toString(),
            carerNotes = notes.toString()
        )
    }
}