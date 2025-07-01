package com.aits.careesteem.view.clients.model

sealed class RiskAssessmentItem {
    data class ActivityItem(val data: List<CarePlanRiskAssList.Data.ActivityRiskAssessmentData>) :
        RiskAssessmentItem()

    data class BehaviourItem(val data: List<CarePlanRiskAssList.Data.BehaviourRiskAssessmentData>) :
        RiskAssessmentItem()

    data class COSHHItem(val data: List<CarePlanRiskAssList.Data.COSHHRiskAssessmentData>) :
        RiskAssessmentItem()

    data class EquipmentItem(val data: List<CarePlanRiskAssList.Data.EquipmentRegisterData>) :
        RiskAssessmentItem()

    data class FinancialItem(val data: List<CarePlanRiskAssList.Data.FinancialRiskAssessmentData>) :
        RiskAssessmentItem()

    data class MedicationItem(val data: List<CarePlanRiskAssList.Data.MedicationRiskAssessmentData>) :
        RiskAssessmentItem()

    data class SelfAdminItem(val data: List<CarePlanRiskAssList.Data.SelfAdministrationRiskAssessmentData>) :
        RiskAssessmentItem()

    // New type for filtered list
    data class FilteredItem(val title: String, val qaList: List<Triple<String, String, String>>) :
        RiskAssessmentItem()
}
