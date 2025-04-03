package com.aits.careesteem.view.clients.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.SharedPreferences
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
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.view.auth.model.OtpVerifyResponse
import com.aits.careesteem.view.clients.adapter.ActivityRiskAssessmentAdapter
import com.aits.careesteem.view.clients.adapter.BehaviourRiskAssessmentAdapter
import com.aits.careesteem.view.clients.adapter.COSHHRiskAssessmentAdapter
import com.aits.careesteem.view.clients.adapter.EquipmentRegisterAdapter
import com.aits.careesteem.view.clients.adapter.FinancialRiskAssessmentAdapter
import com.aits.careesteem.view.clients.adapter.MedicationRiskAssessmentAdapter
import com.aits.careesteem.view.clients.adapter.MyCareNetworkAdapter
import com.aits.careesteem.view.clients.adapter.QuestionAnswerAdapter
import com.aits.careesteem.view.clients.adapter.SelfAdministrationRiskAssessmentAdapter
import com.aits.careesteem.view.clients.helper.FilterQuestionAndAnswers
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList
import com.aits.careesteem.view.clients.model.ClientCarePlanAssessment
import com.aits.careesteem.view.clients.model.ClientDetailsResponse
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.clients.viewmodel.ClientDetailsViewModel
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.reflect.full.memberProperties

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

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gson = Gson()
        clientData = gson.fromJson(args.clientData, ClientsList.Data::class.java)
        viewModel.getClientDetails(requireActivity(), clientData.id)
        viewModel.getClientCarePlanAss(requireActivity(), clientData.id)
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

            myCarePlanLayout.setOnClickListener {
                if(myCarePlanName.tag == "Invisible") {
                    myCarePlanName.tag = "Visible"
                    myCarePlanName.setCompoundDrawablesWithIntrinsicBounds(null, null, requireContext().getDrawable(R.drawable.ic_keyboard_arrow_up), null)
                    carePlanLayout.visibility = View.VISIBLE
                } else {
                    myCarePlanName.tag = "Invisible"
                    myCarePlanName.setCompoundDrawablesWithIntrinsicBounds(null, null, requireContext().getDrawable(R.drawable.ic_keyboard_arrow_down), null)
                    carePlanLayout.visibility = View.GONE
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
                //viewModel.createUnscheduledVisit(requireActivity(), clientData.id)

//                val gson = Gson()
//                val dataString = sharedPreferences.getString(SharedPrefConstant.USER_DATA, null)
//                val userData = gson.fromJson(dataString, OtpVerifyResponse.Data::class.java)
//
//
//                val convertVisit = listOf(
//                    VisitListResponse.Data(
//                        clientId = clientData.id,
//                        visitDetailsId = -1,
//                        clientAddress = clientData.full_address,
//                        clientName = clientData.full_name,
//                        plannedEndTime = "",
//                        plannedStartTime = "",
//                        totalPlannedTime = "",
//                        userId = "[${userData.id}]",
//                        usersRequired = 1,
//                        visitDate = "",
//                        latitude = 0,
//                        longitude = 0,
//                        radius = 0,
//                        placeId = clientData.place_id,
//                        visitStatus = "Unscheduled"
//                    )
//                )
                val gson = Gson()
                val dataString = gson.toJson(clientData)
                val action = ClientsDetailsFragmentDirections.actionClientsDetailsFragmentToUvCheckInFragment(
                    clinetData = dataString
                )
                findNavController().navigate(action)
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

        // carePlan
        viewModel.activityAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersActivityAssessmentData(clientData, data)
                binding.apply {
                    activityAssessment.visibility = View.VISIBLE
                    activityAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Activity Assessment", filteredList)
                    }
                }
            } else {
                binding.activityAssessment.visibility = View.GONE
            }
        }

        viewModel.environmentAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersEnvironmentAssessmentData(clientData, data)
                binding.apply {
                    environmentAssessment.visibility = View.VISIBLE
                    environmentAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Environment Assessment", filteredList)
                    }
                }
            } else {
                binding.environmentAssessment.visibility = View.GONE
            }
        }

        viewModel.financialAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersFinancialAssessmentData(clientData, data)
                binding.apply {
                    financialAssessment.visibility = View.VISIBLE
                    financialAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Financial Assessment", filteredList)
                    }
                }
            } else {
                binding.financialAssessment.visibility = View.GONE
            }
        }

        viewModel.mentalHealthAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersMentalHealthAssessmentData(clientData, data)
                binding.apply {
                    mentalHealthAssessment.visibility = View.VISIBLE
                    mentalHealthAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Mental Health Assessment", filteredList)
                    }
                }
            } else {
                binding.mentalHealthAssessment.visibility = View.GONE
            }
        }

        viewModel.communicationAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersCommunicationAssessmentData(clientData, data)
                binding.apply {
                    communicationAssessment.visibility = View.VISIBLE
                    communicationAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Communication Assessment", filteredList)
                    }
                }
            } else {
                binding.communicationAssessment.visibility = View.GONE
            }
        }

        viewModel.personalHygieneAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersPersonalHygieneAssessmentData(clientData, data)
                binding.apply {
                    personalHygieneAssessment.visibility = View.VISIBLE
                    personalHygieneAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Personal Hygiene Assessment", filteredList)
                    }
                }
            } else {
                binding.personalHygieneAssessment.visibility = View.GONE
            }
        }

        viewModel.medicationAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersMedicationAssessmentData(clientData, data)
                binding.apply {
                    medicationAssessment.visibility = View.VISIBLE
                    medicationAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Medication Assessment", filteredList)
                    }
                }
            } else {
                binding.medicationAssessment.visibility = View.GONE
            }
        }

        viewModel.clinicalAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersClinicalAssessmentData(clientData, data)
                binding.apply {
                    clinicalAssessment.visibility = View.VISIBLE
                    clinicalAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Clinical Assessment", filteredList)
                    }
                }
            } else {
                binding.clinicalAssessment.visibility = View.GONE
            }
        }

        viewModel.culturalSpiritualSocialRelationshipsAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersCulturalSpiritualSocialRelationshipsAssessmentData(clientData, data)
                binding.apply {
                    socialRelationshipsAssessment.visibility = View.VISIBLE
                    socialRelationshipsAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Cultural, Spiritual & Social Relationships Assessment", filteredList)
                    }
                }
            } else {
                binding.socialRelationshipsAssessment.visibility = View.GONE
            }
        }

        viewModel.behaviourAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersBehaviourAssessmentData(clientData, data)
                binding.apply {
                    behaviourAssessment.visibility = View.VISIBLE
                    behaviourAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Behaviour Assessment", filteredList)
                    }
                }
            } else {
                binding.behaviourAssessment.visibility = View.GONE
            }
        }

        viewModel.oralCareAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersOralCareAssessmentData(clientData, data)
                binding.apply {
                    oralCareAssessment.visibility = View.VISIBLE
                    oralCareAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Oral Care Assessment", filteredList)
                    }
                }
            } else {
                binding.oralCareAssessment.visibility = View.GONE
            }
        }

        viewModel.breathingAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersBreathingAssessmentData(clientData, data)
                binding.apply {
                    breathingAssessment.visibility = View.VISIBLE
                    breathingAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Breathing Assessment", filteredList)
                    }
                }
            } else {
                binding.breathingAssessment.visibility = View.GONE
            }
        }

        viewModel.continenceAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersContinenceAssessmentData(clientData, data)
                binding.apply {
                    continenceAssessment.visibility = View.VISIBLE
                    continenceAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Continence Assessment", filteredList)
                    }
                }
            } else {
                binding.continenceAssessment.visibility = View.GONE
            }
        }

        viewModel.domesticAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersDomesticAssessmentData(clientData, data)
                binding.apply {
                    domesticAssessment.visibility = View.VISIBLE
                    domesticAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Domestic Assessment", filteredList)
                    }
                }
            } else {
                binding.domesticAssessment.visibility = View.GONE
            }
        }

        viewModel.equipmentAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersEquipmentAssessmentData(clientData, data)
                binding.apply {
                    equipmentAssessment.visibility = View.VISIBLE
                    equipmentAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Equipment Assessment", filteredList)
                    }
                }
            } else {
                binding.equipmentAssessment.visibility = View.GONE
            }
        }

        viewModel.movingHandlingAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersMovingHandlingAssessmentData(clientData, data)
                binding.apply {
                    movingHandlingAssessment.visibility = View.VISIBLE
                    movingHandlingAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Moving Handling Assessment", filteredList)
                    }
                }
            } else {
                binding.movingHandlingAssessment.visibility = View.GONE
            }
        }

        viewModel.painAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersPainAssessmentData(clientData, data)
                binding.apply {
                    painAssessment.visibility = View.VISIBLE
                    painAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Pain Assessment", filteredList)
                    }
                }
            } else {
                binding.painAssessment.visibility = View.GONE
            }
        }

        viewModel.sleepingAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersSleepingAssessmentData(clientData, data)
                binding.apply {
                    sleepingAssessment.visibility = View.VISIBLE
                    sleepingAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Sleeping Assessment", filteredList)
                    }
                }
            } else {
                binding.sleepingAssessment.visibility = View.GONE
            }
        }

        viewModel.skinAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersSkinAssessmentData(clientData, data)
                binding.apply {
                    skinAssessment.visibility = View.VISIBLE
                    skinAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Skin Assessment", filteredList)
                    }
                }
            } else {
                binding.skinAssessment.visibility = View.GONE
            }
        }

        viewModel.nutritionHydrationAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList = FilterQuestionAndAnswers.filterQuestionsAndAnswersNutritionHydrationAssessmentData(clientData, data)
                binding.apply {
                    nutritionHydrationAssessment.visibility = View.VISIBLE
                    nutritionHydrationAssessmentLayout.setOnClickListener {
                        showAssessmentQuestionAndAnswer("Nutrition & Hydration Assessment", filteredList)
                    }
                }
            } else {
                binding.nutritionHydrationAssessment.visibility = View.GONE
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
                        showBehaviourRiskAssessment(data)
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
                        showSelfAdministrationRiskAssessmentData(data)
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
                        showMedicationRiskAssessment(data)
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
                        showEquipmentRegister(data)
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
                        showFinancialRiskAssessment(data)
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
                        showCOSHHRiskAssessment(data)
                    }
                }
            } else {
                binding.cOSHHRiskAssessment.visibility = View.GONE
            }
        }

//        // add uv visit
//        viewModel.userActualTimeData.observe(viewLifecycleOwner) { data ->
//            if (data != null) {
//                if(!isRedirect) {
//                    isRedirect = AppConstant.TRUE
//                    val convertVisit = listOf(
//                        VisitListResponse.Data(
//                            clientId = clientData.id,
//                            visitDetailsId = data.visit_details_id,
//                            clientAddress = clientData.full_address,
//                            clientName = clientData.full_name,
//                            plannedEndTime = "",
//                            plannedStartTime = "",
//                            totalPlannedTime = "",
//                            userId = "[${data.user_id}]",
//                            usersRequired = 1,
//                            visitDate = data.created_at.substring(0, 10),
//                            latitude = 0,
//                            longitude = 0,
//                            radius = 0,
//                            placeId = "",
//                            visitStatus = "Unscheduled"
//                        )
//                    )
//
//                    val action = ClientsDetailsFragmentDirections.actionClientsDetailsFragmentToUnscheduledVisitsDetailsFragmentFragment(Gson().toJson(convertVisit[0]))
//                    findNavController().navigate(action)
//                }
//            }
//        }

    }

    @SuppressLint("SetTextI18n")
    private fun showAssessmentQuestionAndAnswer(title: String, filteredList: List<Pair<String, String>>) {
        val dialog = Dialog(requireContext())
        val binding: DialogCarePlanBinding =
            DialogCarePlanBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Add data
        binding.dialogTitle.text = title
        val adapter = QuestionAnswerAdapter(filteredList)
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

    @SuppressLint("SetTextI18n")
    private fun showCOSHHRiskAssessment(data: List<CarePlanRiskAssList.Data.COSHHRiskAssessmentData>?) {
        val dialog = Dialog(requireContext())
        val binding: DialogCarePlanBinding =
            DialogCarePlanBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Add data
        binding.dialogTitle.text = "COSHH Risk Assessment"
        val adapter = COSHHRiskAssessmentAdapter(data!!)
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

    @SuppressLint("SetTextI18n")
    private fun showFinancialRiskAssessment(data: List<CarePlanRiskAssList.Data.FinancialRiskAssessmentData>?) {
        val dialog = Dialog(requireContext())
        val binding: DialogCarePlanBinding =
            DialogCarePlanBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Add data
        binding.dialogTitle.text = "Financial Risk Assessment"
        val adapter = FinancialRiskAssessmentAdapter(data!!)
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

    @SuppressLint("SetTextI18n")
    private fun showEquipmentRegister(data: List<CarePlanRiskAssList.Data.EquipmentRegisterData>?) {
        val dialog = Dialog(requireContext())
        val binding: DialogCarePlanBinding =
            DialogCarePlanBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Add data
        binding.dialogTitle.text = "Equipment Register"
        val adapter = EquipmentRegisterAdapter(data!!)
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

    @SuppressLint("SetTextI18n")
    private fun showMedicationRiskAssessment(data: List<CarePlanRiskAssList.Data.MedicationRiskAssessmentData>?) {
        val dialog = Dialog(requireContext())
        val binding: DialogCarePlanBinding =
            DialogCarePlanBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Add data
        binding.dialogTitle.text = "Medication Risk Assessment"
        val adapter = MedicationRiskAssessmentAdapter(data!!)
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

    @SuppressLint("SetTextI18n")
    private fun showSelfAdministrationRiskAssessmentData(data: List<CarePlanRiskAssList.Data.SelfAdministrationRiskAssessmentData>?) {
        val dialog = Dialog(requireContext())
        val binding: DialogCarePlanBinding =
            DialogCarePlanBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Add data
        binding.dialogTitle.text = "Self Administration Risk Assessment"
        val adapter = SelfAdministrationRiskAssessmentAdapter(data!!)
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

    @SuppressLint("SetTextI18n")
    private fun showBehaviourRiskAssessment(data: List<CarePlanRiskAssList.Data.BehaviourRiskAssessmentData>?) {
        val dialog = Dialog(requireContext())
        val binding: DialogCarePlanBinding =
            DialogCarePlanBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Add data
        binding.dialogTitle.text = "Behaviour Risk Assessment"
        val adapter = BehaviourRiskAssessmentAdapter(data!!)
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