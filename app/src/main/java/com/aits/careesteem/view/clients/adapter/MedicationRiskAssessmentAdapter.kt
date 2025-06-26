/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.databinding.ItemMedicationRiskAssessmentBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList

class MedicationRiskAssessmentAdapter(
    private val adapterList: List<CarePlanRiskAssList.Data.MedicationRiskAssessmentData>,
) : RecyclerView.Adapter<MedicationRiskAssessmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMedicationRiskAssessmentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataItem = adapterList[position]
        holder.bind(dataItem)
    }

    override fun getItemCount(): Int = adapterList.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(private val binding: ItemMedicationRiskAssessmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CarePlanRiskAssList.Data.MedicationRiskAssessmentData) {
            binding.apply {
                tvOrdering.text = AppConstant.checkNull(data.ordering)
                tvOrderingComments.text = AppConstant.checkNull(data.ordering_comments)
                tvCollecting.text = AppConstant.checkNull(data.collecting)
                tvCollectingComments.text = AppConstant.checkNull(data.collecting_comments)
                tvVerbalPrompt.text = AppConstant.checkNull(data.verbal_prompt)
                tvVerbalPromptComments.text = AppConstant.checkNull(data.verbal_prompt_comments)
                tvAssisting.text = AppConstant.checkNull(data.assisting)
                tvAssistingComments.text = AppConstant.checkNull(data.assisting_comments)
                tvAdministering.text = AppConstant.checkNull(data.administering)
                tvAdministeringComments.text = AppConstant.checkNull(data.administering_comments)
                tvSpecializedSupport.text = AppConstant.checkNull(data.specialized_support)
                tvSpecializedSupportComments.text =
                    AppConstant.checkNull(data.specialized_support_comments)
                tvTimeSpecific.text = AppConstant.checkNull(data.time_specific)
                tvTimeSpecificComments.text = AppConstant.checkNull(data.time_specific_comments)
                tvControlledDrugs.text = AppConstant.checkNull(data.controlled_drugs)
                tvControlledDrugsDetails.text = AppConstant.checkNull(data.controlled_drugs_details)
                tvAgencyNotification.text = AppConstant.checkNull(data.agency_notification)
                tvMedicationCollectionDetails.text =
                    AppConstant.checkNull(data.medication_collection_details)
                tvPrnMedication.text = AppConstant.checkNull(data.prn_medication)
                tvSafeStorage.text = AppConstant.checkNull(data.safe_storage)
                tvStorageLocation.text = AppConstant.checkNull(data.storage_location)
                tvName1.text = AppConstant.checkNull(data.sign_1)
                tvName2.text = AppConstant.checkNull(data.sign_2)
                tvDate1.text = AppConstant.checkNull(data.date_1)
                tvDate2.text = AppConstant.checkNull(data.date_2)
            }
        }
    }
}