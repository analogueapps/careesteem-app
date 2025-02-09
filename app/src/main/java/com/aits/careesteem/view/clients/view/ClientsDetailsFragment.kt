package com.aits.careesteem.view.clients.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogAboutClientBinding
import com.aits.careesteem.databinding.DialogMyCareNetworkBinding
import com.aits.careesteem.databinding.DialogTodoEditBinding
import com.aits.careesteem.databinding.DialogUnscheduledVisitBinding
import com.aits.careesteem.databinding.FragmentClientsBinding
import com.aits.careesteem.databinding.FragmentClientsDetailsBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.clients.adapter.MyCareNetworkAdapter
import com.aits.careesteem.view.clients.model.ClientDetailsResponse
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.clients.viewmodel.ClientDetailsViewModel
import com.aits.careesteem.view.clients.viewmodel.ClientsViewModel
import com.aits.careesteem.view.visits.adapter.OngoingVisitsAdapter
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClientsDetailsFragment : Fragment(), MyCareNetworkAdapter.OnMyCareNetworkItemClick {
    private var _binding: FragmentClientsDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: ClientsDetailsFragmentArgs by navArgs()

    // Viewmodel
    private val viewModel: ClientDetailsViewModel by viewModels()

    // Selected client object
    private lateinit var clientData: ClientsList.Data

    // Adapter
    private lateinit var myCareNetworkAdapter: MyCareNetworkAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gson = Gson()
        clientData = gson.fromJson(args.clientData, ClientsList.Data::class.java)
        viewModel.getClientDetails(requireActivity(), clientData.id)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientsDetailsBinding.inflate(inflater, container, false)
        setupAdapter()
        setupWidget()
        setupViewModel()
        return binding.root
    }

    private fun setupAdapter() {
        myCareNetworkAdapter = MyCareNetworkAdapter(requireContext(), this@ClientsDetailsFragment)
        binding.rvMyCareNetwork.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyCareNetwork.adapter = myCareNetworkAdapter
    }

    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    private fun setupWidget() {
        binding.apply {
            aboutName.text = "About ${clientData.full_name}"

            myCareLayout.setOnClickListener {
                if(myCareName.tag == "Invisible") {
                    myCareName.tag = "Visible"
                    myCareName.setCompoundDrawablesWithIntrinsicBounds(null, null, requireContext().getDrawable(R.drawable.ic_keyboard_arrow_up), null)
                    rvMyCareNetwork.visibility = View.VISIBLE
                } else {
                    myCareName.tag = "Invisible"
                    myCareName.setCompoundDrawablesWithIntrinsicBounds(null, null, requireContext().getDrawable(R.drawable.ic_keyboard_arrow_down), null)
                    rvMyCareNetwork.visibility = View.GONE
                }
            }

            aboutLayout.setOnClickListener {
                showAboutClient(viewModel.aboutClient.value)
            }
        }

        binding.btnCreateUnscheduledVisit.setOnClickListener {
            val dialog = Dialog(requireContext())
            val binding: DialogUnscheduledVisitBinding =
                DialogUnscheduledVisitBinding.inflate(layoutInflater)

            dialog.setContentView(binding.root)
            dialog.setCancelable(AppConstant.FALSE)

            // Handle button clicks
            binding.btnPositive.setOnClickListener {
                dialog.dismiss()
            }
            binding.btnNegative.setOnClickListener {
                dialog.dismiss()
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

    @SuppressLint("SetTextI18n")
    private fun showAboutClient(value: ClientDetailsResponse.Data.AboutData?) {
        val dialog = Dialog(requireContext())
        val binding: DialogAboutClientBinding =
            DialogAboutClientBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Add data
        binding.aboutClientName.text = "About ${clientData.full_name}"
        binding.tvDob.text = value?.date_of_birth ?: "N/A"
        binding.tvAge.text = value?.age ?: "N/A"
        binding.tvNhsNo.text = value?.nhs_number ?: "N/A"
        binding.tvGender.text = value?.gender ?: "N/A"
        binding.tvReligion.text = value?.religion ?: "N/A"
        binding.tvMaritalStatus.text = value?.marital_status ?: "N/A"
        binding.tvEthnicity.text = value?.ethnicity ?: "N/A"

        // Handle button clicks
        binding.closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val window = dialog.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
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
        viewModel.clientMyCareNetwork.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                myCareNetworkAdapter.updatedList(data)
            }
        }
    }

    override fun onMyCareNetworkItemClicked(data: ClientDetailsResponse.Data.MyCareNetworkData) {
        val dialog = Dialog(requireContext())
        val binding: DialogMyCareNetworkBinding =
            DialogMyCareNetworkBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Add data
        binding.occupationType.text = data.occupation_type
        binding.tvName.text = data.name
        binding.tvAge.text = data.age
        binding.tvContactNumber.text = data.contact_number
        binding.tvEmail.text = data.email
        binding.tvAddress.text = data.address
        binding.tvCity.text = data.city
        binding.tvPostCode.text = data.post_code

        // Handle button clicks
        binding.closeButton.setOnClickListener {
            dialog.dismiss()
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