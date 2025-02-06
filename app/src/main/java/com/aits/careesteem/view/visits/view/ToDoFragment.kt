package com.aits.careesteem.view.visits.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.databinding.DialogLogoutBinding
import com.aits.careesteem.databinding.DialogTodoEditBinding
import com.aits.careesteem.databinding.FragmentToDoBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.view.auth.view.AuthActivity
import com.aits.careesteem.view.clients.adapter.ClientAdapter
import com.aits.careesteem.view.visits.adapter.TodoListAdapter
import com.aits.careesteem.view.visits.model.TodoListResponse
import com.aits.careesteem.view.visits.viewmodel.ToDoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ToDoFragment : Fragment(), TodoListAdapter.OnItemItemClick {
    private var _binding: FragmentToDoBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: ToDoViewModel by viewModels()

    // Adapter
    private lateinit var todoListAdapter: TodoListAdapter

    companion object {
        private const val ARG_ID = "ARG_ID"
        fun newInstance(id: String): ToDoFragment {
            return ToDoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, id)
                }
            }
        }
    }

    private var id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the ID from the arguments
        id = arguments?.getString(ARG_ID)
        viewModel.getToDoList(requireActivity(), id.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentToDoBinding.inflate(inflater, container, false)
        setupAdapter()
        setupSwipeRefresh()
        setupViewModel()
        return binding.root
    }

    private fun setupAdapter() {
        todoListAdapter = TodoListAdapter(requireContext(), this@ToDoFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = todoListAdapter
    }

    private fun setupSwipeRefresh() {
        val coroutineScope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            coroutineScope.launch {
                try {
                    delay(2000)
                    binding.swipeRefresh.isRefreshing = AppConstant.FALSE
                    viewModel.getToDoList(requireActivity(), id.toString())
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
        viewModel.completeCount.observe(viewLifecycleOwner) { count ->
            if (count != null) {
                binding.submitCount.text = count.toString()
            }
        }

        // Data visibility
        viewModel.toDoList.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                binding.totalCount.text = data.size.toString()
                todoListAdapter.updatedList(data)
            }
        }
    }

    override fun onItemItemClicked(data: TodoListResponse.Data) {
        val dialog = Dialog(requireContext())
        val binding: DialogTodoEditBinding =
            DialogTodoEditBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Add data
        binding.headTodoName.text = data.todoName
        binding.todoName.text = data.todoName
        binding.additionalNotes.text = data.additionalNotes
        binding.carerNotes.text = Editable.Factory.getInstance().newEditable(data.carerNotes)

        // Handle button clicks
        binding.closeButton.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnCompleted.setOnClickListener {
            dialog.dismiss()
            viewModel.updateTodo(
                activity = requireActivity(),
                todoOutcome = 1,
                visitDetailsId = id.toString(),
                todoDetailsId = data.todoDetailsId,
                carerNotes = binding.carerNotes.text.toString().trim()
            )
        }
        binding.btnNotCompleted.setOnClickListener {
            dialog.dismiss()
            viewModel.updateTodo(
                activity = requireActivity(),
                todoOutcome = 0,
                visitDetailsId = id.toString(),
                todoDetailsId = data.todoDetailsId,
                carerNotes = binding.carerNotes.text.toString().trim()
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