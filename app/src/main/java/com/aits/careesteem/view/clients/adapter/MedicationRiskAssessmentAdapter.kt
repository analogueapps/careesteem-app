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
import com.aits.careesteem.databinding.ItemSelfAdministrationRiskAssessmentBinding
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList

class MedicationRiskAssessmentAdapter(
    private val adapterList: List<CarePlanRiskAssList.Data.MedicationRiskAssessmentData>,
) : RecyclerView.Adapter<MedicationRiskAssessmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMedicationRiskAssessmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
                tvOrdering.text = data.ordering
                tvOrderingComments.text = data.ordering_comments
                tvCollecting.text = data.collecting
                tvCollectingComments.text = data.collecting_comments
                tvVerbalPrompt.text = data.verbal_prompt
                tvVerbalPromptComments.text = data.verbal_prompt_comments
                tvAssisting.text = data.assisting
                tvAssistingComments.text = data.assisting_comments
                tvAdministering.text = data.administering
                tvAdministeringComments.text = data.administering_comments
                tvSpecializedSupport.text = data.specialized_support
                tvSpecializedSupportComments.text = data.specialized_support_comments
                tvTimeSpecific.text = data.time_specific
                tvTimeSpecificComments.text = data.time_specific_comments
                tvControlledDrugs.text = data.controlled_drugs
                tvControlledDrugsDetails.text = data.controlled_drugs_details
                tvAgencyNotification.text = data.agency_notification
                tvMedicationCollectionDetails.text = data.medication_collection_details
                tvPrnMedication.text = data.prn_medication
                tvSafeStorage.text = data.safe_storage
                tvStorageLocation.text = data.storage_location
                tvName1.text = data.sign_1
                tvName2.text = data.sign_2
                tvDate1.text = data.date_1
                tvDate2.text = data.date_2
            }
        }
    }
}