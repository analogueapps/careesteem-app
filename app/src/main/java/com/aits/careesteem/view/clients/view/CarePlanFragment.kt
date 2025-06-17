package com.aits.careesteem.view.clients.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentAboutMeBinding
import com.aits.careesteem.databinding.FragmentCarePlanBinding
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.clients.adapter.CareNetworkAdapter
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

    val finalList = mutableListOf<RiskAssessmentItem>()

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
        adapter = RiskAssessmentParentAdapter(requireContext(), mutableListOf())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
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
                //showAssessmentQuestionAndAnswer("Activity Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Activity Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.activityAssessment.visibility = View.GONE
            }
        }

        viewModel.environmentAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersEnvironmentAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Environment Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Environment Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.environmentAssessment.visibility = View.GONE
            }
        }

        viewModel.financialAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersFinancialAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Financial Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Financial Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.financialAssessment.visibility = View.GONE
            }
        }

        viewModel.mentalHealthAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersMentalHealthAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Mental Health Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Mental Health Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.mentalHealthAssessment.visibility = View.GONE
            }
        }

        viewModel.communicationAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersCommunicationAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Communication Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Communication Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.communicationAssessment.visibility = View.GONE
            }
        }

        viewModel.personalHygieneAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersPersonalHygieneAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Personal Hygiene Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Personal Hygiene Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.personalHygieneAssessment.visibility = View.GONE
            }
        }

        viewModel.medicationAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersMedicationAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Medication Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Medication Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.medicationAssessment.visibility = View.GONE
            }
        }

        viewModel.clinicalAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersClinicalAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Clinical Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Clinical Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.clinicalAssessment.visibility = View.GONE
            }
        }

        viewModel.culturalSpiritualSocialRelationshipsAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersCulturalSpiritualSocialRelationshipsAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Cultural, Spiritual & Social Relationships Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Cultural, Spiritual & Social Relationships Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.socialRelationshipsAssessment.visibility = View.GONE
            }
        }

        viewModel.behaviourAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersBehaviourAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Behaviour Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Behaviour Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.behaviourAssessment.visibility = View.GONE
            }
        }

        viewModel.oralCareAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersOralCareAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Oral Care Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Oral Care Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.oralCareAssessment.visibility = View.GONE
            }
        }

        viewModel.breathingAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersBreathingAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Breathing Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Breathing Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.breathingAssessment.visibility = View.GONE
            }
        }

        viewModel.continenceAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersContinenceAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Continence Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Continence Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.continenceAssessment.visibility = View.GONE
            }
        }

        viewModel.domesticAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersDomesticAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Domestic Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Domestic Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.domesticAssessment.visibility = View.GONE
            }
        }

        viewModel.equipmentAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersEquipmentAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Equipment Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Equipment Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.equipmentAssessment.visibility = View.GONE
            }
        }

        viewModel.movingHandlingAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersMovingHandlingAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Moving Handling Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Moving Handling Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.movingHandlingAssessment.visibility = View.GONE
            }
        }

        viewModel.painAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersPainAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Pain Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Pain Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.painAssessment.visibility = View.GONE
            }
        }

        viewModel.sleepingAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersSleepingAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Sleeping Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Sleeping Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.sleepingAssessment.visibility = View.GONE
            }
        }

        viewModel.skinAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersSkinAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Skin Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Skin Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.skinAssessment.visibility = View.GONE
            }
        }

        viewModel.nutritionHydrationAssessmentData.observe(viewLifecycleOwner) { data ->
            if (data != null && data.consent) {
                val filteredList =
                    FilterQuestionAndAnswers.filterQuestionsAndAnswersNutritionHydrationAssessmentData(
                        clientData,
                        data
                    )
                //showAssessmentQuestionAndAnswer("Nutrition & Hydration Assessment", filteredList)
                finalList.add(RiskAssessmentItem.FilteredItem("Nutrition & Hydration Assessment", filteredList))
                updateAdapterData()
            } else {
                //binding.nutritionHydrationAssessment.visibility = View.GONE
            }
        }

        // activityRiskAssessment
        viewModel.activityRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            data.forEach {
                finalList.add(RiskAssessmentItem.ActivityItem(it))
            }
            updateAdapterData()
        }

        // behaviourRiskAssessment
        viewModel.behaviourRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            data.forEach { finalList.add(RiskAssessmentItem.BehaviourItem(it)) }
            updateAdapterData()
        }

        // selfAdministrationRiskAssessment
        viewModel.selfAdministrationRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            data.forEach { finalList.add(RiskAssessmentItem.SelfAdminItem(it)) }
            updateAdapterData()
        }

        // medicationRiskAssessment
        viewModel.medicationRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            data.forEach { finalList.add(RiskAssessmentItem.MedicationItem(it)) }
            updateAdapterData()
        }

        // equipmentRegister
        viewModel.equipmentRegisterData.observe(viewLifecycleOwner) { data ->
            data.forEach { finalList.add(RiskAssessmentItem.EquipmentItem(it)) }
            updateAdapterData()
        }

        // financialRiskAssessment
        viewModel.financialRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            data.forEach { finalList.add(RiskAssessmentItem.FinancialItem(it)) }
            updateAdapterData()
        }

        // cOSHHRiskAssessment
        viewModel.cOSHHRiskAssessmentData.observe(viewLifecycleOwner) { data ->
            data.forEach { finalList.add(RiskAssessmentItem.COSHHItem(it)) }
            updateAdapterData()
        }

//        val adapter = RiskAssessmentParentAdapter(requireContext(), finalList)
//        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        binding.recyclerView.adapter = adapter

    }

    private fun updateAdapterData() {
        adapter.updateData(finalList)
    }

}
