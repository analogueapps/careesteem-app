package com.aits.careesteem.view.visits.view

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogTodoEditBinding
import com.aits.careesteem.databinding.FragmentToDoBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.visits.adapter.TodoListAdapter
import com.aits.careesteem.view.visits.model.TodoListResponse
import com.aits.careesteem.view.visits.viewmodel.ToDoViewModel
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ToDoFragment : Fragment(), TodoListAdapter.OnItemItemClick {

    private var _binding: FragmentToDoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ToDoViewModel by viewModels()
    private lateinit var todoAdapter: TodoListAdapter

    private var visitId: String? = null
    private var clientId: String? = null
    private var allowChanges: Boolean = true

    companion object {
        private const val ARG_VISIT_ID = "ARG_VISIT_ID"
        private const val ARG_CLIENT_ID = "ARG_CLIENT_ID"
        private const val ARG_CHANGES = "ARG_CHANGES"

        @JvmStatic
        fun newInstance(visitId: String, clientId: String, allowChanges: Boolean) =
            ToDoFragment().apply {
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
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentToDoBinding.inflate(inflater, container, false)
        setupUi()
        setupRecyclerView()
        setupViewModel()
        setupSwipeRefresh()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (isVisible) {
            visitId?.let { viewModel.getToDoList(requireActivity(), it) }
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
        // No add button for ToDo currently, but if needed add here similarly to VisitNotesFragment
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoListAdapter(requireContext(), this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = todoAdapter
        }
    }

    private fun setupSwipeRefresh() {
        val coroutineScope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            coroutineScope.launch {
                delay(2000)
                binding.swipeRefresh.isRefreshing = false
                visitId?.let { viewModel.getToDoList(requireActivity(), it) }
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

        viewModel.completeCount.observe(viewLifecycleOwner) {
            binding.submitCount.text = it?.toString() ?: "0"
        }

        viewModel.totalCount.observe(viewLifecycleOwner) {
            binding.totalCount.text = it?.toString() ?: "0"
        }

        viewModel.toDoList.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                showEmptyState()
            } else {
                showToDoList(list)
            }
        }

        binding.searchView.queryHint = "Search"
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                todoAdapter.filter(newText ?: "")
                return true
            }
        })
    }

    private fun showEmptyState() = with(binding) {
        recyclerView.visibility = View.GONE
        headerView.visibility = View.GONE
        emptyLayout.visibility = View.VISIBLE
        Glide.with(this@ToDoFragment)
            .asGif()
            .load(R.drawable.no_todo)
            .into(gifImageView)
    }

    private fun showToDoList(list: List<TodoListResponse.Data>) = with(binding) {
        emptyLayout.visibility = View.GONE
        headerView.visibility = View.VISIBLE
        recyclerView.visibility = View.VISIBLE
        todoAdapter.updateList(list)
    }

    // -------------------------------
    // ToDo Item Actions
    // -------------------------------

    override fun onItemItemClicked(data: TodoListResponse.Data) {
        if (!allowChanges) {
            AlertUtils.showToast(requireActivity(), "Changes not allowed", ToastyType.WARNING)
            return
        }
        showEditTodoDialog(data)
    }

    private fun showEditTodoDialog(data: TodoListResponse.Data) {
        val dialog = BottomSheetDialog(requireContext())
        val binding: DialogTodoEditBinding =
            DialogTodoEditBinding.inflate(layoutInflater)
        dialog.window?.setDimAmount(0.8f)
        dialog.setContentView(binding.root)
        dialog.setCancelable(true) // Same as AppConstant.FALSE

//        // Set max height for the bottom sheet
//        val maxHeight = (resources.displayMetrics.heightPixels * 0.7).toInt()
//        binding.root.layoutParams = binding.root.layoutParams?.apply {
//            height = maxHeight
//        }
        binding.closeButton.setOnClickListener { dialog.dismiss() }
        // Setup views
        binding.apply {
            todoName.text = AppConstant.checkNull(data.todoName)
            val addNote = AppConstant.checkNull(data.additionalNotes)
            additionalNotes.text = addNote
            if (addNote != "N/A") {
                addNoteMain.visibility = View.VISIBLE
            }

            carerNotes.text = Editable.Factory.getInstance().newEditable(data.carerNotes)

            btnCompleted.setOnClickListener {
                // check carerNotes empty or not
                if (carerNotes.text.toString().trim().isEmpty()) {
                    AlertUtils.showToast(
                        requireActivity(),
                        "Please enter carer notes",
                        ToastyType.WARNING
                    )
                    return@setOnClickListener
                }
                dialog.dismiss()
                updateTodoStatus(data, 1, carerNotes.text.toString().trim())
            }
            btnNotCompleted.setOnClickListener {
                // check carerNotes empty or not
                if (carerNotes.text.toString().trim().isEmpty()) {
                    AlertUtils.showToast(
                        requireActivity(),
                        "Please enter carer notes",
                        ToastyType.WARNING
                    )
                    return@setOnClickListener
                }
                dialog.dismiss()
                updateTodoStatus(data, 0, carerNotes.text.toString().trim())
            }
        }

        // Optional: make the background transparent
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Show the dialog
        dialog.show()

    }

    private fun updateTodoStatus(data: TodoListResponse.Data, outcome: Int, notes: String) {
        viewModel.updateTodo(
            activity = requireActivity(),
            todoOutcome = outcome,
            clientId = clientId.orEmpty(),
            visitDetailsId = visitId.orEmpty(),
            todoDetailsId = data.todoDetailsId,
            carerNotes = notes,
            todoEssential = data.todoEssential
        )
    }
}
