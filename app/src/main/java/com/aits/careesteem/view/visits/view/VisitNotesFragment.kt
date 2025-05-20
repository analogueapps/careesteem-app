package com.aits.careesteem.view.visits.view

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.R
import com.aits.careesteem.databinding.*
import com.aits.careesteem.utils.*
import com.aits.careesteem.view.visits.adapter.VisitNotesAdapter
import com.aits.careesteem.view.visits.model.ClientVisitNotesDetails
import com.aits.careesteem.view.visits.viewmodel.VisitNotesViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class VisitNotesFragment : Fragment(), VisitNotesAdapter.OnItemItemClick {

    private var _binding: FragmentVisitNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VisitNotesViewModel by viewModels()
    private lateinit var visitNotesAdapter: VisitNotesAdapter

    private var visitId: String? = null
    private var clientId: String? = null
    private var allowChanges: Boolean = true

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

    // -------------------------------
    // UI Setup
    // -------------------------------

    private fun setupUi() = with(binding) {
        val visibility = if (allowChanges) View.VISIBLE else View.GONE
        btnAddVisitNotes.visibility = visibility
        btnTopAddVisitNotes.visibility = visibility

        btnAddVisitNotes.setOnClickListener { showAddNoteDialog() }
        btnTopAddVisitNotes.setOnClickListener { showAddNoteDialog() }
    }

    private fun setupRecyclerView() {
        visitNotesAdapter = VisitNotesAdapter(requireContext(), this)
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

    // -------------------------------
    // ViewModel Observers
    // -------------------------------

    private fun setupViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            ProgressLoader.toggle(requireActivity(), it)
        }

        viewModel.visitNotesList.observe(viewLifecycleOwner) { data ->
            if (data.isNullOrEmpty()) {
                showEmptyState()
            } else {
                showNotes(data)
            }
        }
    }

    private fun showEmptyState() = with(binding) {
        recyclerView.visibility = View.GONE
        emptyLayout.visibility = View.VISIBLE
        Glide.with(this@VisitNotesFragment)
            .asGif()
            .load(R.drawable.no_notes)
            .into(gifImageView)
    }

    private fun showNotes(data: List<ClientVisitNotesDetails.Data>) = with(binding) {
        emptyLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        visitNotesAdapter.updateList(data)
    }

    // -------------------------------
    // Note Actions
    // -------------------------------

    override fun onItemItemClicked(data: ClientVisitNotesDetails.Data) {
        if (!allowChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed")
            return
        }
        showEditNoteDialog(data)
    }

    private fun showAddNoteDialog() {
        if (!allowChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed")
            return
        }

        val dialog = createNoteDialog()
        val dialogBinding = DialogVisitNotesBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.closeButton.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnUpdate.setOnClickListener {
            if(dialogBinding.visitNotes.text.toString().isEmpty()) {
                AlertUtils.showToast(requireActivity(), "Please enter visit notes")
                return@setOnClickListener
            }
            dialog.dismiss()
            viewModel.addVisitNotes(
                requireActivity(),
                visitDetailsId = visitId.orEmpty(),
                visitNotes = dialogBinding.visitNotes.text.toString().trim()
            )
        }

        dialog.show()
    }

    private fun showEditNoteDialog(data: ClientVisitNotesDetails.Data) {
        val dialog = createNoteDialog()
        val dialogBinding = DialogVisitNotesBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.visitNotes.text = Editable.Factory.getInstance().newEditable(data.visitNotes)

        dialogBinding.closeButton.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnUpdate.setOnClickListener {
            // empty block for visit notes text
            if(dialogBinding.visitNotes.text.toString().isEmpty()) {
                AlertUtils.showToast(requireActivity(), "Please enter visit notes")
                return@setOnClickListener
            }
            dialog.dismiss()
            viewModel.updateVisitNotes(
                activity = requireActivity(),
                visitDetailsId = visitId.orEmpty(),
                createdByUserid = data.createdByUserId,
                visitNotesId = data.id,
                visitNotes = dialogBinding.visitNotes.text.toString().trim()
            )
        }

        dialog.show()
    }

    private fun createNoteDialog(): Dialog {
        return Dialog(requireContext()).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setCancelable(false)
        }
    }
}
