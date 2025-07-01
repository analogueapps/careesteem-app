package com.aits.careesteem.view.clients.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.databinding.FragmentCarePlanBinding
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.clients.adapter.QuestionAnswerAdapter
import com.aits.careesteem.view.clients.adapter.RiskAssessmentParentAdapter
import com.aits.careesteem.view.clients.helper.FilterQuestionAndAnswers
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.clients.model.RiskAssessmentItem
import com.aits.careesteem.view.clients.viewmodel.ClientDetailsViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CarePlanFragment : Fragment() {
    private var _binding: FragmentCarePlanBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: ClientDetailsViewModel by viewModels()

    // Adapter
    private lateinit var adapter: RiskAssessmentParentAdapter
    private lateinit var questionAnswerAdapter: QuestionAnswerAdapter

    val finalList = mutableListOf<RiskAssessmentItem>()

    private var stringData: String? = null

    // Selected client object
    private lateinit var clientData: ClientsList.Data

    private var isAnyAssessmentVisible = false

    private fun showIfAnyAssessmentVisible() {
        if (!isAnyAssessmentVisible) {
            isAnyAssessmentVisible = true
            //binding.assessmentContainer.visibility = View.VISIBLE
        }
    }

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
            CarePlanFragment().apply {
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
            viewModel.getClientCarePlanAss(requireActivity(), clientData.id.toString())
            viewModel.getClientCarePlanRiskAss(requireActivity(), clientData.id)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCarePlanBinding.inflate(inflater, container, false)
        setupAdapter()
        setupViewModel()
        return binding.root
    }

    private fun setupAdapter() {
        adapter = RiskAssessmentParentAdapter(requireContext(), mutableListOf(), clientData)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        questionAnswerAdapter = QuestionAnswerAdapter()
        binding.rvAssessment.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAssessment.adapter = questionAnswerAdapter
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

        // carePlan
        viewModel.activityAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersActivityAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Activity Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.environmentAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersEnvironmentAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Environment Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.financialAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersFinancialAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Financial Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.mentalHealthAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersMentalHealthAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Mental Health Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.communicationAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersCommunicationAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Communication Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.personalHygieneAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersPersonalHygieneAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Personal Hygiene Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.medicationAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersMedicationAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Medication Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.clinicalAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersClinicalAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Clinical Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.culturalSpiritualSocialRelationshipsAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersCulturalSpiritualSocialRelationshipsAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer(
                        "Cultural, Spiritual & Social Relationships Assessment",
                        filteredList
                    )
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.behaviourAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersBehaviourAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Behaviour Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.oralCareAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersOralCareAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Oral Care Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.breathingAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersBreathingAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Breathing Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.continenceAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersContinenceAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Continence Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.domesticAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersDomesticAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Domestic Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.equipmentAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersEquipmentAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Equipment Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.movingHandlingAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersMovingHandlingAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Moving Handling Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.painAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersPainAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Pain Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.sleepingAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersSleepingAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Sleeping Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.skinAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersSkinAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer("Skin Assessment", filteredList)
                    showIfAnyAssessmentVisible()
                }
            }
        }

        viewModel.nutritionHydrationAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersNutritionHydrationAssessmentData(
                        clientData,
                        data
                    )
                if (filteredList.isNotEmpty()) {
                    showAssessmentQuestionAndAnswer(
                        "Nutrition & Hydration Assessment",
                        filteredList
                    )
                    showIfAnyAssessmentVisible()
                }
            }
        }

        // Activity Risk Assessment
        viewModel.activityRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            data.takeIf { it.isNotEmpty() }?.let {
                finalList.add(RiskAssessmentItem.ActivityItem(it))
                updateAdapterData()
            }
        }

// Behaviour Risk Assessment
        viewModel.behaviourRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            data.takeIf { it.isNotEmpty() }?.let {
                finalList.add(RiskAssessmentItem.BehaviourItem(it))
                updateAdapterData()
            }
        }

// Self Administration Risk Assessment
        viewModel.selfAdministrationRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            data.takeIf { it.isNotEmpty() }?.let {
                finalList.add(RiskAssessmentItem.SelfAdminItem(it))
                updateAdapterData()
            }
        }

// Medication Risk Assessment
        viewModel.medicationRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            data.takeIf { it.isNotEmpty() }?.let {
                finalList.add(RiskAssessmentItem.MedicationItem(it))
                updateAdapterData()
            }
        }

// Equipment Register
        viewModel.equipmentRegisterData.observe(viewLifecycleOwner) { data ->
            data.takeIf { it.isNotEmpty() }?.let {
                finalList.add(RiskAssessmentItem.EquipmentItem(it))
                updateAdapterData()
            }
        }

// Financial Risk Assessment
        viewModel.financialRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            data.takeIf { it.isNotEmpty() }?.let {
                finalList.add(RiskAssessmentItem.FinancialItem(it))
                updateAdapterData()
            }
        }

// COSHH Risk Assessment
        viewModel.cOSHHRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            data.takeIf { it.isNotEmpty() }?.let {
                finalList.add(RiskAssessmentItem.COSHHItem(it))
                updateAdapterData()
            }
        }


//        val adapter = RiskAssessmentParentAdapter(requireContext(), finalList)
//        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        binding.recyclerView.adapter = adapter

    }

    private fun showAssessmentQuestionAndAnswer(
        title: String,
        filteredList: List<Triple<String, String, String>>
    ) {
        questionAnswerAdapter.updateData(title, filteredList)
    }

    private fun updateAdapterData() {
        adapter.updateData(finalList)
    }

}
