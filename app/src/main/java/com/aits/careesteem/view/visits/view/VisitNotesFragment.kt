package com.aits.careesteem.view.visits.view

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogVisitNotesBinding
import com.aits.careesteem.databinding.FragmentVisitNotesBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.bottomsheet.VisitNotesBottomSheetFragment
import com.aits.careesteem.view.visits.adapter.VisitNotesAdapter
import com.aits.careesteem.view.visits.model.ClientVisitNotesDetails
import com.aits.careesteem.view.visits.viewmodel.VisitNotesViewModel
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VisitNotesFragment : Fragment(),
    VisitNotesAdapter.OnItemItemClick,
    VisitNotesBottomSheetFragment.OnVisitNotesUpdateListener
{

    private var _binding: FragmentVisitNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VisitNotesViewModel by viewModels()
    private lateinit var visitNotesAdapter: VisitNotesAdapter

    private var visitId: String? = null
    private var clientId: String? = null
    private var allowChanges: Boolean = true

    private var mainList: List<ClientVisitNotesDetails.Data>? = null

    companion object {
        private const val ARG_VISIT_ID = "ARG_VISIT_ID"
        private const val ARG_CLIENT_ID = "ARG_CLIENT_ID"
        private const val ARG_CHANGES = "ARG_CHANGES"

        @JvmStatic
        fun newInstance(visitId: String, clientId: String, allowChanges: Boolean) =
            VisitNotesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_VISIT_ID, visitId)
                    putString(ARG_CLIENT_ID, clientId)
                    putBoolean(ARG_CHANGES, allowChanges)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            visitId = it.getString(ARG_VISIT_ID)
            clientId = it.getString(ARG_CLIENT_ID)
            allowChanges = it.getBoolean(ARG_CHANGES, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitNotesBinding.inflate(inflater, container, false)
        setupUi()
        setupRecyclerView()
        setupViewModel()
        setupSwipeRefresh()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (isVisible) {
            visitId?.let { viewModel.getVisitNotesList(requireActivity(), it) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupUi() = with(binding) {
        val visibility = if (allowChanges) View.VISIBLE else View.GONE
        btnAddVisitNotes.visibility = visibility
        btnTopAddVisitNotes.visibility = visibility

        btnAddVisitNotes.setOnClickListener { showAddNoteDialog() }
        btnTopAddVisitNotes.setOnClickListener { showAddNoteDialog() }
    }

    private fun setupRecyclerView() {
        visitNotesAdapter = VisitNotesAdapter(requireContext(), this, false, isChanges = allowChanges)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = visitNotesAdapter
        }
    }

    private fun setupSwipeRefresh() {
        val coroutineScope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            coroutineScope.launch {
                delay(2000)
                binding.swipeRefresh.isRefreshing = false
                visitId?.let { viewModel.getVisitNotesList(requireActivity(), it) }
            }
        }
    }

    private fun setupViewModel() {
        var hasLoadedData = false

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                ProgressLoader.showProgress(requireActivity())
            } else {
                ProgressLoader.dismissProgress()
                if (hasLoadedData) {
                    updateUI()
                }
            }
        }

        viewModel.visitNotesList.observe(viewLifecycleOwner) { data ->
            mainList = data
            hasLoadedData = true
        }
    }

    private fun updateUI() {
        if (mainList.isNullOrEmpty()) {
            showEmptyState()
        } else {
            showNotes(mainList!!)
        }
    }

    private fun showEmptyState() = with(binding) {
        headerView.visibility = View.GONE
        recyclerView.visibility = View.GONE
        emptyLayout.visibility = View.VISIBLE
        Glide.with(this@VisitNotesFragment)
            .asGif()
            .load(R.drawable.no_notes)
            .into(gifImageView)
    }

    private fun showNotes(data: List<ClientVisitNotesDetails.Data>) = with(binding) {
        emptyLayout.visibility = View.GONE
        headerView.visibility = View.VISIBLE
        recyclerView.visibility = View.VISIBLE
        visitNotesAdapter.updateList(data)
    }

    override fun onItemItemClicked(data: ClientVisitNotesDetails.Data) {
        if (!allowChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
            return
        }
        showEditNoteDialog(data)
    }

    private fun showAddNoteDialog() {
        if (!allowChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
            return
        }

        val bottomSheet = VisitNotesBottomSheetFragment.newInstance("",visitId,0,"", "")
        bottomSheet.show(childFragmentManager, VisitNotesBottomSheetFragment.TAG)
    }

    private fun showEditNoteDialog(data: ClientVisitNotesDetails.Data) {
        if (!allowChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
            return
        }

        val bottomSheet = VisitNotesBottomSheetFragment.newInstance(data.visitNotes, visitId,1,data.id, data.createdByUserId)
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
            viewModel.addVisitNotes(
                activity = requireActivity(),
                visitDetailsId = visitDetailsId,
                visitNotes = visitNotes
            )
        } else {
            viewModel.updateVisitNotes(
                activity = requireActivity(),
                visitDetailsId = visitDetailsId,
                visitNotesId = visitNotesId,
                visitNotes = visitNotes,
                createdByUserid = createdByUserid
            )
        }
    }
}
