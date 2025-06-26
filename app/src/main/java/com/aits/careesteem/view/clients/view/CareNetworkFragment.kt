package com.aits.careesteem.view.clients.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.databinding.FragmentCareNetworkBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.clients.adapter.CareNetworkAdapter
import com.aits.careesteem.view.clients.model.ClientDetailsResponse
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.clients.viewmodel.ClientDetailsViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CareNetworkFragment : Fragment(), CareNetworkAdapter.OnMyCareNetworkItemClick {
    private var _binding: FragmentCareNetworkBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: ClientDetailsViewModel by viewModels()

    // Adapter
    private lateinit var myCareNetworkAdapter: CareNetworkAdapter

    private var stringData: String? = null

    // Selected client object
    private lateinit var clientData: ClientsList.Data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the ID from the arguments
        stringData = arguments?.getString(ARG_DATA)
        val gson = Gson()
        clientData = gson.fromJson(stringData, ClientsList.Data::class.java)
    }

    companion object {
        private const val ARG_DATA = "ARG_DATA"

        @JvmStatic
        fun newInstance(param1: String) =
            CareNetworkFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DATA, param1)
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
            viewModel.getClientDetails(requireActivity(), clientData.id.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCareNetworkBinding.inflate(inflater, container, false)
        setupAdapter()
        setupViewModel()
        return binding.root
    }

    private fun setupViewModel() {
        viewModel.clientMyCareNetwork.observe(viewLifecycleOwner) {
            if (it != null) {
                myCareNetworkAdapter.updateList(it)
            }
        }
    }

    private fun setupAdapter() {
        myCareNetworkAdapter = CareNetworkAdapter(requireContext(), this@CareNetworkFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = myCareNetworkAdapter
    }

    override fun onMyCareNetworkItemClicked(data: ClientDetailsResponse.Data.MyCareNetworkData) {
        if (AppConstant.checkNull(data.contact_number) == "N/A") {
            AlertUtils.showToast(
                requireActivity(),
                "Contact number not available",
                ToastyType.ERROR
            )
        } else {
            val intent = Intent(Intent.ACTION_DIAL, "tel:${data.contact_number}".toUri())
            requireContext().startActivity(intent)
        }
    }


}