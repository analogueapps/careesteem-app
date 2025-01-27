package com.aits.careesteem.view.clients.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.databinding.FragmentClientsBinding
import com.aits.careesteem.view.clients.adapter.ClientAdapter
import com.aits.careesteem.view.clients.model.Client
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClientsFragment : Fragment(), ClientAdapter.OnItemClick {
    private var _binding: FragmentClientsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientsBinding.inflate(inflater, container, false)
        setupWidget()
        return binding.root
    }

    private fun setupWidget() {
        // Set up RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = ClientAdapter(getClientList(), this@ClientsFragment)
    }

    private fun getClientList(): List<Client> {
        return listOf(
            Client("Leslie Alexander", "+44 6715550110", "138 Lewd Road, Middlewich, SSI 3SW", "Moderate"),
            Client("Cillian Mckay", "+44 7474825285", "218 Gigantic Close, Tandragee, WV10 5E", "High"),
            Client("Mylo Clements", "+44 7744632445", "135 Mammoth Road, Dummurry, HSI1 7WF", "Moderate"),
            Client("Subhaan Salinas", "+44 7818844643", "229 Poor Crescent, Dingestow, WS35 7BB", null)
        )
    }

    override fun onItemClicked(item: String) {
        val direction = ClientsFragmentDirections.actionBottomClientsToClientsDetailsFragment()
        findNavController().navigate(direction)
    }
}