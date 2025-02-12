/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.databinding.ItemActivityRiskAssessmentBinding
import com.aits.careesteem.databinding.ItemSelfAdministrationRiskAssessmentBinding
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList

class SelfAdministrationRiskAssessmentAdapter(
    private val adapterList: List<CarePlanRiskAssList.Data.SelfAdministrationRiskAssessmentData>,
) : RecyclerView.Adapter<SelfAdministrationRiskAssessmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSelfAdministrationRiskAssessmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val binding: ItemSelfAdministrationRiskAssessmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CarePlanRiskAssList.Data.SelfAdministrationRiskAssessmentData) {
            binding.apply {
                tvMedicineName.text = data.medicine_name
                tvDose.text = data.dose
                tvRoute.text = data.route
                tvTimeFrequency.text = data.time_frequency
                tvSelfAdministration.text = data.self_administration
                tvSelfAdministerFully.text = data.self_administer_fully
                tvMedicines.text = data.medicines
                tvTimeToTakeMedicines.text = data.time_to_take_medicines
                tvDosageToTake.text = data.dosage
                tvTakeMedicines.text = data.take_medicines
                tvSpecialInstructions.text = data.special_instructions
                tvSideEffects.text = data.side_effects
                tvMissedDose.text = data.missed_dose
                tvDifficultyReadingLabel.text = data.difficulty_reading_label
                tvOpensMedication.text = data.opens_medication
                tvSafeStorage.text = data.safe_storage
                tvAgreesToNotify.text = data.agrees_to_notify
                tvResponsibleForReorder.text = data.responsible_for_reorder
                tvName1.text = data.sign_1
                tvName2.text = data.sign_2
                tvDate1.text = data.date_1
                tvDate2.text = data.date_2
            }
        }
    }
}