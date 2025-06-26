package com.aits.careesteem.view.clients.model

sealed class RiskAssessmentItem {
    data class ActivityItem(val data: CarePlanRiskAssList.Data.ActivityRiskAssessmentData) :
        RiskAssessmentItem()

    data class BehaviourItem(val data: CarePlanRiskAssList.Data.BehaviourRiskAssessmentData) :
        RiskAssessmentItem()

    data class COSHHItem(val data: CarePlanRiskAssList.Data.COSHHRiskAssessmentData) :
        RiskAssessmentItem()

    data class EquipmentItem(val data: CarePlanRiskAssList.Data.EquipmentRegisterData) :
        RiskAssessmentItem()

    data class FinancialItem(val data: CarePlanRiskAssList.Data.FinancialRiskAssessmentData) :
        RiskAssessmentItem()

    data class MedicationItem(val data: CarePlanRiskAssList.Data.MedicationRiskAssessmentData) :
        RiskAssessmentItem()

    data class SelfAdminItem(val data: CarePlanRiskAssList.Data.SelfAdministrationRiskAssessmentData) :
        RiskAssessmentItem()

    // New type for filtered list
    data class FilteredItem(val title: String, val qaList: List<Triple<String, String, String>>) :
        RiskAssessmentItem()
}
