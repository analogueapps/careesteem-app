package com.aits.careesteem.view.unscheduled_visits.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentUvVisitNotesBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.bottomsheet.VisitNotesBottomSheetFragment
import com.aits.careesteem.view.unscheduled_visits.adapter.UvVisitNotesListAdapter
import com.aits.careesteem.view.unscheduled_visits.model.UvVisitNotesListResponse
import com.aits.careesteem.view.unscheduled_visits.viewmodel.UvVisitNotesViewModel
import com.aits.careesteem.view.visits.model.VisitDetailsResponse
import com.bumptech.glide.Glide
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UvVisitNotesFragment : Fragment(),
    UvVisitNotesListAdapter.OnItemItemClick,
    VisitNotesBottomSheetFragment.OnVisitNotesUpdateListener
{
    private var _binding: FragmentUvVisitNotesBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: UvVisitNotesViewModel by viewModels()

    // Adapter
    private lateinit var uvVisitNotesListAdapter: UvVisitNotesListAdapter

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
        if (isVisible) {
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
            if (isChanges) {
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
        uvVisitNotesListAdapter =
            UvVisitNotesListAdapter(requireContext(), this@UvVisitNotesFragment, isChanges)
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
                    viewModel.getUvVisitNotesList(
                        requireActivity(),
                        visitData?.visitDetailsId.toString()
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
            if (isLoading) ProgressLoader.showProgress(requireActivity())
            else ProgressLoader.dismissProgress()
        }

        // Data visibility
        viewModel.visitNotesList.observe(viewLifecycleOwner) { data ->
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

    @SuppressLint("SetTextI18n")
    override fun onItemItemClicked(data: UvVisitNotesListResponse.Data) {
        if (!isChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
            return
        }

        val bottomSheet = VisitNotesBottomSheetFragment.newInstance(data.visit_notes,visitData?.visitDetailsId,1,data.id, data.visit_user_id)
        bottomSheet.show(childFragmentManager, VisitNotesBottomSheetFragment.TAG)
    }

    @SuppressLint("SetTextI18n")
    private fun addNotes() {
        if (!isChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
            return
        }

        val bottomSheet = VisitNotesBottomSheetFragment.newInstance("",visitData?.visitDetailsId,0,"", "")
        bottomSheet.show(childFragmentManager, VisitNotesBottomSheetFragment.TAG)
    }

    override fun onVisitNoteUpdated(
        visitNotes: String,
        visitDetailsId: String,
        visitNotesId: String,
        createdByUserid: String,
        action: Int
    ) {
        if(action == 0) {
            viewModel.addNotes(
                activity = requireActivity(),
                visitDetailsId = visitDetailsId,
                visitNotes = visitNotes
            )
        } else {
            viewModel.updateNotes(
                activity = requireActivity(),
                visitDetailsId = visitDetailsId,
                visitNotesId = visitNotesId,
                visitNotes = visitNotes
            )
        }
    }


}