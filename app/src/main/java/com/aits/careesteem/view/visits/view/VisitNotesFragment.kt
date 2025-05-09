package com.aits.careesteem.view.visits.view

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogTodoEditBinding
import com.aits.careesteem.databinding.DialogVisitNotesBinding
import com.aits.careesteem.databinding.FragmentVisitNotesBinding
import com.aits.careesteem.databinding.FragmentVisitsBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.view.visits.adapter.TodoListAdapter
import com.aits.careesteem.view.visits.adapter.VisitNotesAdapter
import com.aits.careesteem.view.visits.model.ClientVisitNotesDetails
import com.aits.careesteem.view.visits.view.ToDoFragment.Companion
import com.aits.careesteem.view.visits.viewmodel.ToDoViewModel
import com.aits.careesteem.view.visits.viewmodel.VisitNotesViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VisitNotesFragment : Fragment(), VisitNotesAdapter.OnItemItemClick {
    private var _binding: FragmentVisitNotesBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: VisitNotesViewModel by viewModels()

    // Adapter
    private lateinit var visitNotesAdapter: VisitNotesAdapter

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
            VisitNotesFragment().apply {
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
            viewModel.getVisitNotesList(requireActivity(), id.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVisitNotesBinding.inflate(inflater, container, false)
        setupAdapter()
        setupSwipeRefresh()
        setupViewModel()
        setupWidget()
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
                addVisitNotes()
            }
            btnTopAddVisitNotes.setOnClickListener {
                addVisitNotes()
            }
        }
    }

    private fun setupAdapter() {
        visitNotesAdapter = VisitNotesAdapter(requireContext(), this@VisitNotesFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = visitNotesAdapter
    }

    private fun setupSwipeRefresh() {
        val coroutineScope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            coroutineScope.launch {
                try {
                    delay(2000)
                    binding.swipeRefresh.isRefreshing = AppConstant.FALSE
                    viewModel.getVisitNotesList(requireActivity(), id.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

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
        viewModel.visitNotesList.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                binding.apply {
                    emptyLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
                visitNotesAdapter.updatedList(data)
            } else {
                binding.apply {
                    emptyLayout.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    Glide.with(this@VisitNotesFragment)
                        .asGif()
                        .load(R.drawable.no_notes) // Replace with your GIF resource
                        .into(gifImageView)
                }
            }
        }
    }


    override fun onItemItemClicked(data: ClientVisitNotesDetails.Data) {
        if(!isChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed")
            return
        }

        val dialog = Dialog(requireContext())
        val binding: DialogVisitNotesBinding =
            DialogVisitNotesBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Add data
        binding.visitNotes.text = Editable.Factory.getInstance().newEditable(data.visitNotes)

        // Handle button clicks
        binding.closeButton.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnUpdate.setOnClickListener {
            dialog.dismiss()
            viewModel.updateVisitNotes(
                activity = requireActivity(),
                visitDetailsId = id.toString(),
                createdByUserid = data.createdByUserId,
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

    private fun addVisitNotes() {
        if(!isChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed")
            return
        }

        val dialog = Dialog(requireContext())
        val binding: DialogVisitNotesBinding =
            DialogVisitNotesBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Handle button clicks
        binding.closeButton.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnUpdate.setOnClickListener {
            dialog.dismiss()
            viewModel.addVisitNotes(
                activity = requireActivity(),
                visitDetailsId = id.toString(),
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