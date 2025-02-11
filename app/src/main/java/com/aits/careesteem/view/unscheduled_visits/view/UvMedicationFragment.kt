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
import com.aits.careesteem.databinding.FragmentUvToDoBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.view.unscheduled_visits.adapter.UvMedicationListAdapter
import com.aits.careesteem.view.unscheduled_visits.adapter.UvTodoListAdapter
import com.aits.careesteem.view.unscheduled_visits.model.UvMedicationListResponse
import com.aits.careesteem.view.unscheduled_visits.model.UvTodoListResponse
import com.aits.careesteem.view.unscheduled_visits.viewmodel.UvMedicationViewModel
import com.aits.careesteem.view.unscheduled_visits.viewmodel.UvToDoViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UvMedicationFragment : Fragment(), UvMedicationListAdapter.OnItemItemClick {
    private var _binding: FragmentUvMedicationBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: UvMedicationViewModel by viewModels()

    // Adapter
    private lateinit var uvMedicationListAdapter: UvMedicationListAdapter

    private var id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the ID from the arguments
        id = arguments?.getString(ARG_ID)
    }

    companion object {
        private const val ARG_ID = "ARG_ID"
        @JvmStatic
        fun newInstance(param1: String) =
            UvMedicationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, param1)
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
            viewModel.getUvMedicationList(requireActivity(), id.toString())
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
        return binding.root
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
        uvMedicationListAdapter = UvMedicationListAdapter(requireContext(), this@UvMedicationFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = uvMedicationListAdapter
    }

    private fun setupSwipeRefresh() {
        val coroutineScope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            coroutineScope.launch {
                try {
                    delay(2000)
                    binding.swipeRefresh.isRefreshing = AppConstant.FALSE
                    viewModel.getUvMedicationList(requireActivity(), id.toString())
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
        viewModel.medicationList.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                binding.apply {
                    emptyLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
                uvMedicationListAdapter.updatedList(data)
            } else {
                binding.apply {
                    emptyLayout.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    Glide.with(this@UvMedicationFragment)
                        .asGif()
                        .load(R.drawable.no_tablet) // Replace with your GIF resource
                        .into(gifImageView)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onItemItemClicked(data: UvMedicationListResponse.Data) {
        val dialog = Dialog(requireContext())
        val binding: DialogVisitNotesBinding =
            DialogVisitNotesBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // add data
        binding.tvTopHeading.text = "Medication Notes"
        binding.tvBelowHeading.text = "Medication Notes"
        binding.visitNotes.hint = "Enter medication notes"
        binding.visitNotes.text = Editable.Factory.getInstance().newEditable(data.medication_notes)

        // Handle button clicks
        binding.closeButton.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnUpdate.setOnClickListener {
            dialog.dismiss()
            viewModel.updateNotes(
                activity = requireActivity(),
                visitDetailsId = id.toString(),
                medicationId = data.id,
                medicationNotes = binding.visitNotes.text.toString().trim()
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
        val dialog = Dialog(requireContext())
        val binding: DialogVisitNotesBinding =
            DialogVisitNotesBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // add data
        binding.tvTopHeading.text = "Medication Notes"
        binding.tvBelowHeading.text = "Medication Notes"
        binding.visitNotes.hint = "Enter medication notes"

        // Handle button clicks
        binding.closeButton.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnUpdate.setOnClickListener {
            dialog.dismiss()
            viewModel.addNotes(
                activity = requireActivity(),
                visitDetailsId = id.toString(),
                medicationNotes = binding.visitNotes.text.toString().trim()
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