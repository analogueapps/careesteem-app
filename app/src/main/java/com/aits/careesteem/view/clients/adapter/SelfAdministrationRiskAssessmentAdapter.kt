/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.databinding.ItemSelfAdministrationRiskAssessmentBinding
import com.aits.careesteem.databinding.ItemSelfAdministrationRiskAssessmentMedicationBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList
import com.aits.careesteem.view.clients.model.ClientsList

class SelfAdministrationRiskAssessmentAdapter(
    private val context: Context,
    private val adapterList: List<CarePlanRiskAssList.Data.SelfAdministrationRiskAssessmentData>,
    private val clientData: ClientsList.Data,
) : RecyclerView.Adapter<SelfAdministrationRiskAssessmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSelfAdministrationRiskAssessmentBinding.inflate(
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

    override fun getItemCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(private val binding: ItemSelfAdministrationRiskAssessmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(data: CarePlanRiskAssList.Data.SelfAdministrationRiskAssessmentData) {
            binding.apply {
//                tvMedicineName.text = AppConstant.checkNull(data.medicine_name)
//                tvDose.text = AppConstant.checkNull(data.dose)
//                tvRoute.text = AppConstant.checkNull(data.route)
//                tvTimeFrequency.text = AppConstant.checkNull(data.time_frequency)
//                tvSelfAdministration.text = AppConstant.checkNull(data.self_administration)
                tvSelfAdministerFullyQns.text = "1. Does ${AppConstant.checkClientFirstName(clientData.full_name)} want to self-administer fully or partially?"
                tvSelfAdministerFully.text = AppConstant.checkNull(data.self_administer_fully)
                tvMedicinesQns.text = "2. Does ${AppConstant.checkClientFirstName(clientData.full_name)} know what medicines they are taking and what they are for?"
                tvMedicines.text = AppConstant.checkNull(data.medicines)
                tvTimeToTakeMedicinesQns.text = "3. Does ${AppConstant.checkClientFirstName(clientData.full_name)} know what time to take the medicines?"
                tvTimeToTakeMedicines.text = AppConstant.checkNull(data.time_to_take_medicines)
                tvDosageToTakeQns.text = "4. Does ${AppConstant.checkClientFirstName(clientData.full_name)} know what dosage to take?"
                tvDosageToTake.text = AppConstant.checkNull(data.dosage)
                tvTakeMedicinesQns.text = "5. Does ${AppConstant.checkClientFirstName(clientData.full_name)} know how to take the medicines?"
                tvTakeMedicines.text = AppConstant.checkNull(data.take_medicines)
                tvSpecialInstructionsQns.text = "6. Does ${AppConstant.checkClientFirstName(clientData.full_name)} know about any special instructions?"
                tvSpecialInstructions.text = AppConstant.checkNull(data.special_instructions)
                tvSideEffectsQns.text = "7. Does ${AppConstant.checkClientFirstName(clientData.full_name)} know about common, possible side effects?"
                tvSideEffects.text = AppConstant.checkNull(data.side_effects)
                tvMissedDoseQns.text = "8. Does ${AppConstant.checkClientFirstName(clientData.full_name)} know what to do if they miss a dose?"
                tvMissedDose.text = AppConstant.checkNull(data.missed_dose)
                tvDifficultyReadingLabelQns.text = "9. Does ${AppConstant.checkClientFirstName(clientData.full_name)} have any difficulty in reading the label on the medicines?"
                tvDifficultyReadingLabel.text = AppConstant.checkNull(data.difficulty_reading_label)
                tvOpensMedicationQns.text = "10. Does ${AppConstant.checkClientFirstName(clientData.full_name)} open their medication (blister packs, bottles)?"
                tvOpensMedication.text = AppConstant.checkNull(data.opens_medication)
                tvSafeStorageQns.text = "11. Does ${AppConstant.checkClientFirstName(clientData.full_name)} understand the principles of safe storage for medicines, including their responsibility for safe keeping?"
                tvSafeStorage.text = AppConstant.checkNull(data.safe_storage)
                tvAgreesToNotifyQns.text = "12. Does ${AppConstant.checkClientFirstName(clientData.full_name)} agree to notify staff of any changes to the prescribed medication and/or over-the-counter/homely medications?"
                tvAgreesToNotify.text = AppConstant.checkNull(data.agrees_to_notify)
                tvResponsibleForReorderQns.text = "13. Would ${AppConstant.checkClientFirstName(clientData.full_name)} be responsible for the re-ordering of prescribed medication and its collection?"
                tvResponsibleForReorder.text = AppConstant.checkNull(data.responsible_for_reorder)

                val adapter = SelfAdministrationRiskAssessmentMedicationAdapter(adapterList)
                binding.recyclerView.adapter = adapter
                binding.recyclerView.layoutManager = LinearLayoutManager(context)

//                tvName1.text = AppConstant.checkNull(data.sign_1)
//                tvName2.text = AppConstant.checkNull(data.sign_2)
//                tvDate1.text = AppConstant.checkNull(data.date_1)
//                tvDate2.text = AppConstant.checkNull(data.date_2)
            }
        }
    }
}


class SelfAdministrationRiskAssessmentMedicationAdapter(
    private val adapterList: List<CarePlanRiskAssList.Data.SelfAdministrationRiskAssessmentData>
) : RecyclerView.Adapter<SelfAdministrationRiskAssessmentMedicationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelfAdministrationRiskAssessmentMedicationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataItem = adapterList[position]
        holder.bind(dataItem, position)
    }

    override fun getItemCount(): Int = adapterList.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    inner class ViewHolder(
        private val binding: ItemSelfAdministrationRiskAssessmentMedicationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(
            data: CarePlanRiskAssList.Data.SelfAdministrationRiskAssessmentData,
            position: Int
        ) {
            binding.apply {
                // Show item index starting from 1
                medicationId.text = "Medication ${position + 1}"
                tvMedicineName.text = AppConstant.checkNull(data.medicine_name)
                tvDose.text = AppConstant.checkNull(data.dose)
                tvRoute.text = AppConstant.checkNull(data.route)
                tvTimeFrequency.text = AppConstant.checkNull(data.time_frequency)
                tvSelfAdministration.text = AppConstant.checkNull(data.self_administration)
                tvSelfAdministerFully.text = AppConstant.checkNull(data.self_administer_fully)
            }
        }
    }
}