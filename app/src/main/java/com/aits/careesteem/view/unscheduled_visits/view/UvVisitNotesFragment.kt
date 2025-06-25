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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogVisitNotesBinding
import com.aits.careesteem.databinding.FragmentUvMedicationBinding
import com.aits.careesteem.databinding.FragmentUvVisitNotesBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.unscheduled_visits.adapter.UvMedicationListAdapter
import com.aits.careesteem.view.unscheduled_visits.adapter.UvVisitNotesListAdapter
import com.aits.careesteem.view.unscheduled_visits.model.UvMedicationListResponse
import com.aits.careesteem.view.unscheduled_visits.model.UvVisitNotesListResponse
import com.aits.careesteem.view.unscheduled_visits.viewmodel.UvMedicationViewModel
import com.aits.careesteem.view.unscheduled_visits.viewmodel.UvVisitNotesViewModel
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
class UvVisitNotesFragment : Fragment(), UvVisitNotesListAdapter.OnItemItemClick {
    private var _binding: FragmentUvVisitNotesBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: UvVisitNotesViewModel by viewModels()

    // Adapter
    private lateinit var uvVisitNotesListAdapter: UvVisitNotesListAdapter

    private var visitDataString: String? = null
    private var isChanges = true

    private var visitData: VisitDetailsResponse.Data? = null

    private var isLoadingNotes = false

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
            UvVisitNotesFragment().apply {
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
            viewModel.getUvVisitNotesList(requireActivity(), visitData?.visitDetailsId.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUvVisitNotesBinding.inflate(inflater, container, false)
        setupWidget()
        setupAdapter()
        setupSwipeRefresh()
        setupViewModel()
        setupUi()
        return binding.root
    }

    private fun setupUi() {
        binding.apply {
            if(isChanges) {
                btnAddVisitNotes.visibility = View.VISIBLE
                btnTopAddVisitNotes.visibility = View.VISIBLE
            } else {
                btnAddVisitNotes.visibility = View.GONE
                btnTopAddVisitNotes.visibility = View.GONE
            }
        }
    }

    private fun setupWidget() {
        binding.apply {
            btnAddVisitNotes.setOnClickListener {
                addNotes()
            }
            btnTopAddVisitNotes.setOnClickListener {
                addNotes()
            }
        }
    }

    private fun setupAdapter() {
        uvVisitNotesListAdapter = UvVisitNotesListAdapter(requireContext(), this@UvVisitNotesFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = uvVisitNotesListAdapter
    }

    private fun setupSwipeRefresh() {
        val coroutineScope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            coroutineScope.launch {
                try {
                    delay(2000)
                    binding.swipeRefresh.isRefreshing = AppConstant.FALSE
                    viewModel.getUvVisitNotesList(requireActivity(), visitData?.visitDetailsId.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            isLoadingNotes = loading
            ProgressLoader.toggle(requireActivity(), loading)

            // Hide empty layout during loading
            if (loading) {
                binding.emptyLayout.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.headerView.visibility = View.GONE
            }
        }

        // Data visibility
        viewModel.visitNotesList.observe(viewLifecycleOwner) { data ->
            if (!isLoadingNotes) {
                if (data.isNullOrEmpty()) {
                    binding.apply {
                        headerView.visibility = View.GONE
                        emptyLayout.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        Glide.with(this@UvVisitNotesFragment)
                            .asGif()
                            .load(R.drawable.no_notes) // Replace with your GIF resource
                            .into(gifImageView)
                    }
                } else {
                    binding.apply {
                        headerView.visibility = View.VISIBLE
                        emptyLayout.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                    uvVisitNotesListAdapter.updateList(data)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onItemItemClicked(data: UvVisitNotesListResponse.Data) {
        if(!isChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
            return
        }

        val dialog = BottomSheetDialog(requireContext())
        val binding = DialogVisitNotesBinding.inflate(layoutInflater)
        dialog.window?.setDimAmount(0.8f)
        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.TRUE)

        // add data
        binding.tvTopHeading.text = "Visit Notes"
        binding.tvBelowHeading.text = "Visit Notes"
        binding.visitNotes.hint = "Enter visit notes"
        binding.visitNotes.text = Editable.Factory.getInstance().newEditable(data.visit_notes)

        // Handle button clicks
        binding.closeButton.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnSave.setOnClickListener {
            if(binding.visitNotes.text.toString().isEmpty()) {
                AlertUtils.showToast(requireActivity(), "Please enter Visit notes", ToastyType.WARNING)
                return@setOnClickListener
            }
            dialog.dismiss()
            viewModel.updateNotes(
                activity = requireActivity(),
                visitDetailsId = visitData?.visitDetailsId.toString(),
                visitNotesId = data.id,
                visitNotes = binding.visitNotes.text.toString().trim()
            )
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
    private fun addNotes() {
        if(!isChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
            return
        }

        val dialog = BottomSheetDialog(requireContext())
        val binding = DialogVisitNotesBinding.inflate(layoutInflater)
        dialog.window?.setDimAmount(0.8f)
        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.TRUE)

        // add data
        binding.tvTopHeading.text = "Visit Notes"
        binding.tvBelowHeading.text = "Visit Notes"
        binding.visitNotes.hint = "Enter visit notes"

        // Handle button clicks
        binding.closeButton.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnSave.setOnClickListener {
            if(binding.visitNotes.text.toString().isEmpty()) {
                AlertUtils.showToast(requireActivity(), "Please enter Visit notes", ToastyType.WARNING)
                return@setOnClickListener
            }
            dialog.dismiss()
            viewModel.addNotes(
                activity = requireActivity(),
                visitDetailsId = visitData?.visitDetailsId.toString(),
                visitNotes = binding.visitNotes.text.toString().trim()
            )
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