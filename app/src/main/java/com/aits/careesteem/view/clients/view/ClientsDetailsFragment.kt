package com.aits.careesteem.view.clients.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogAboutClientBinding
import com.aits.careesteem.databinding.DialogCarePlanBinding
import com.aits.careesteem.databinding.DialogMyCareNetworkBinding
import com.aits.careesteem.databinding.DialogUnscheduledVisitBinding
import com.aits.careesteem.databinding.FragmentClientsDetailsBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.clients.adapter.ActivityRiskAssessmentAdapter
import com.aits.careesteem.view.clients.adapter.MyCareNetworkAdapter
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList
import com.aits.careesteem.view.clients.model.ClientDetailsResponse
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.clients.viewmodel.ClientDetailsViewModel
import com.aits.careesteem.view.visits.model.VisitListResponse
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

    // const value
    private var isRedirect = AppConstant.FALSE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gson = Gson()
        clientData = gson.fromJson(args.clientData, ClientsList.Data::class.java)
        viewModel.getClientDetails(requireActivity(), clientData.id)
        viewModel.getClientCarePlanRiskAss(requireActivity(), clientData.id)
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
                isRedirect = AppConstant.FALSE
                dialog.dismiss()
                viewModel.createUnscheduledVisit(requireActivity(), clientData.id)
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

        // activityRiskAssessment
        viewModel.activityRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                binding.apply {
                    activityRiskAssessment.visibility = View.VISIBLE
                    activityRiskAssessmentLayout.setOnClickListener {
                        showActivityRiskAssessment(data)
                    }
                }
            } else {
                binding.activityRiskAssessment.visibility = View.GONE
            }
        }

        // behaviourRiskAssessment
        viewModel.behaviourRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                binding.apply {
                    behaviourRiskAssessment.visibility = View.VISIBLE
                    behaviourRiskAssessmentLayout.setOnClickListener {

                    }
                }
            } else {
                binding.behaviourRiskAssessment.visibility = View.GONE
            }
        }

        // selfAdministrationRiskAssessment
        viewModel.selfAdministrationRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                binding.apply {
                    selfAdministrationRiskAssessment.visibility = View.VISIBLE
                    selfAdministrationRiskAssessmentLayout.setOnClickListener {

                    }
                }
            } else {
                binding.selfAdministrationRiskAssessment.visibility = View.GONE
            }
        }

        // medicationRiskAssessment
        viewModel.medicationRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                binding.apply {
                    medicationRiskAssessment.visibility = View.VISIBLE
                    medicationRiskAssessmentLayout.setOnClickListener {

                    }
                }
            } else {
                binding.medicationRiskAssessment.visibility = View.GONE
            }
        }

        // equipmentRegister
        viewModel.equipmentRegisterData.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                binding.apply {
                    equipmentRegister.visibility = View.VISIBLE
                    equipmentRegisterLayout.setOnClickListener {

                    }
                }
            } else {
                binding.equipmentRegister.visibility = View.GONE
            }
        }

        // financialRiskAssessment
        viewModel.financialRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                binding.apply {
                    financialRiskAssessment.visibility = View.VISIBLE
                    financialRiskAssessmentLayout.setOnClickListener {

                    }
                }
            } else {
                binding.financialRiskAssessment.visibility = View.GONE
            }
        }

        // cOSHHRiskAssessment
        viewModel.cOSHHRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                binding.apply {
                    cOSHHRiskAssessment.visibility = View.VISIBLE
                    cOSHHRiskAssessmentLayout.setOnClickListener {

                    }
                }
            } else {
                binding.cOSHHRiskAssessment.visibility = View.GONE
            }
        }

        // add uv visit
        viewModel.userActualTimeData.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                if(!isRedirect) {
                    isRedirect = AppConstant.TRUE
                    val convertVisit = listOf(
                        VisitListResponse.Data(
                            visitDetailsId = data.visit_details_id,
                            clientAddress = clientData.full_address,
                            clientName = clientData.full_name,
                            plannedEndTime = "",
                            plannedStartTime = "",
                            totalPlannedTime = "",
                            userId = data.user_id,
                            usersRequired = 1,
                            visitDate = data.created_at.substring(0, 10),
                            latitude = 0,
                            longitude = 0,
                            radius = 0,
                            placeId = "",
                            visitStatus = "Unscheduled"
                        )
                    )

                    val action = ClientsDetailsFragmentDirections.actionClientsDetailsFragmentToUnscheduledVisitsDetailsFragmentFragment(Gson().toJson(convertVisit[0]))
                    findNavController().navigate(action)
                }
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun showActivityRiskAssessment(data: List<CarePlanRiskAssList.Data.ActivityRiskAssessmentData>?) {
        val dialog = Dialog(requireContext())
        val binding: DialogCarePlanBinding =
            DialogCarePlanBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Add data
        binding.dialogTitle.text = "Activity Risk Assessment"
        val adapter = ActivityRiskAssessmentAdapter(data!!)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

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