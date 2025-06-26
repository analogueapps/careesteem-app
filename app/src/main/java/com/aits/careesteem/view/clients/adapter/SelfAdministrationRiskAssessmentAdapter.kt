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

class SelfAdministrationRiskAssessmentAdapter(
    private val context: Context,
    private val adapterList: List<CarePlanRiskAssList.Data.SelfAdministrationRiskAssessmentData>,
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

        fun bind(data: CarePlanRiskAssList.Data.SelfAdministrationRiskAssessmentData) {
            binding.apply {
                tvMedicineName.text = AppConstant.checkNull(data.medicine_name)
                tvDose.text = AppConstant.checkNull(data.dose)
                tvRoute.text = AppConstant.checkNull(data.route)
                tvTimeFrequency.text = AppConstant.checkNull(data.time_frequency)
                tvSelfAdministration.text = AppConstant.checkNull(data.self_administration)
                tvSelfAdministerFully.text = AppConstant.checkNull(data.self_administer_fully)
                tvMedicines.text = AppConstant.checkNull(data.medicines)
                tvTimeToTakeMedicines.text = AppConstant.checkNull(data.time_to_take_medicines)
                tvDosageToTake.text = AppConstant.checkNull(data.dosage)
                tvTakeMedicines.text = AppConstant.checkNull(data.take_medicines)
                tvSpecialInstructions.text = AppConstant.checkNull(data.special_instructions)
                tvSideEffects.text = AppConstant.checkNull(data.side_effects)
                tvMissedDose.text = AppConstant.checkNull(data.missed_dose)
                tvDifficultyReadingLabel.text = AppConstant.checkNull(data.difficulty_reading_label)
                tvOpensMedication.text = AppConstant.checkNull(data.opens_medication)
                tvSafeStorage.text = AppConstant.checkNull(data.safe_storage)
                tvAgreesToNotify.text = AppConstant.checkNull(data.agrees_to_notify)
                tvResponsibleForReorder.text = AppConstant.checkNull(data.responsible_for_reorder)

                val adapter = SelfAdministrationRiskAssessmentMedicationAdapter(adapterList)
                binding.recyclerView.adapter = adapter
                binding.recyclerView.layoutManager = LinearLayoutManager(context)

                tvName1.text = AppConstant.checkNull(data.sign_1)
                tvName2.text = AppConstant.checkNull(data.sign_2)
                tvDate1.text = AppConstant.checkNull(data.date_1)
                tvDate2.text = AppConstant.checkNull(data.date_2)
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