package com.aits.careesteem.view.clients.view

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.databinding.FragmentClientsBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.view.clients.adapter.ClientAdapter
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.clients.viewmodel.ClientsViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ClientsFragment : Fragment(),
    ClientAdapter.OnItemClick {
    private var _binding: FragmentClientsBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: ClientsViewModel by viewModels()

    // Adapter
    private lateinit var clientAdapter: ClientAdapter

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getClientsList(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientsBinding.inflate(inflater, container, false)
        setupAdapter()
        setupSwipeRefresh()
        setupViewModel()
        return binding.root
    }

    private fun setupAdapter() {
        clientAdapter = ClientAdapter(requireContext(), this@ClientsFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = clientAdapter
    }

    private fun setupSwipeRefresh() {
        val coroutineScope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            coroutineScope.launch {
                try {
                    delay(2000)
                    binding.swipeRefresh.isRefreshing = AppConstant.FALSE
                    viewModel.getClientsList(requireActivity())
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
        viewModel.clientsList.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                clientAdapter.updateList(data)
            }
        }

        binding.searchView.queryHint = "Search"
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                clientAdapter.filter(newText ?: "")
                return true
            }
        })

        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        binding.includedHeader.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.includedHeader.ivClear.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                // i want to take this one and parse to adapter
                clientAdapter.filter(s.toString())
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

    override fun onItemClicked(data: ClientsList.Data) {
        val gson = Gson()
        val dataString = gson.toJson(data)
        val direction = ClientsFragmentDirections.actionBottomClientsToClientsDetailsFragment(
            clientData = dataString
        )
        findNavController().navigate(direction)
    }
}