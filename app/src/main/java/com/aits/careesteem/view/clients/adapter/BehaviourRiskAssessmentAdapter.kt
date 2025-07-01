/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.databinding.ItemBehaviourRiskAssessmentBinding
import com.aits.careesteem.databinding.ItemBehaviourRiskAssessmentHazardsBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList

class BehaviourRiskAssessmentAdapter(
    private val context: Context,
    private val adapterList: List<CarePlanRiskAssList.Data.BehaviourRiskAssessmentData>,
) : RecyclerView.Adapter<BehaviourRiskAssessmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemBehaviourRiskAssessmentBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemBehaviourRiskAssessmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CarePlanRiskAssList.Data.BehaviourRiskAssessmentData) {
            binding.apply {
                tvFrequencyPotential.text = AppConstant.checkNull(data.frequency_potential)
                tvAffectedByBehaviour.text = AppConstant.checkNull(data.affected_by_behaviour)

//                val adapter = BehaviourRiskAssessmentHazardsAdapter(adapterList)
//                binding.recyclerView.adapter = adapter
//                binding.recyclerView.layoutManager = LinearLayoutManager(context)

                // Convert BehaviourRiskAssessmentData to RiskAssessmentRow list
                val rowList = data.potential_hazards.mapIndexedNotNull { index, hazard ->
                    if (hazard.isNotBlank()) {
                        RiskAssessmentRow(
                            potentialHazard = hazard,
                            levelOfRisk = data.level_of_risk.getOrNull(index).orEmpty()
                                .ifBlank { "N/A" },
                            riskRange = data.risk_range.getOrNull(index).orEmpty()
                                .ifBlank { "N/A" },
                            supportMethod = data.support_methods.getOrNull(index).orEmpty()
                                .ifBlank { "N/A" },
                            controlAdequate = data.controls_adequate.getOrNull(index).orEmpty()
                                .ifBlank { "N/A" },
                            regulatoryMeasure = data.regulatory_measures.getOrNull(index).orEmpty()
                                .ifBlank { "N/A" }
                        )
                    } else null
                }

                val adapter = BehaviourRiskAssessmentHazardsAdapter(rowList)
                binding.recyclerView.layoutManager = LinearLayoutManager(context)
                binding.recyclerView.adapter = adapter


                // Optional: handle others that don’t rely on potential_hazards
                tvPotentialTriggers.text = data.potential_triggers
                    .filter { it.isNotBlank() }
                    .joinToString(", ")

//                tvName1.text = AppConstant.checkNull(data.sign_1)
//                tvName2.text = AppConstant.checkNull(data.sign_2)
//                tvDate1.text = AppConstant.checkNull(data.date_1)
//                tvDate2.text = AppConstant.checkNull(data.date_2)
            }
        }
    }
}

class BehaviourRiskAssessmentHazardsAdapter(
    private val rowList: List<RiskAssessmentRow>
) : RecyclerView.Adapter<BehaviourRiskAssessmentHazardsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBehaviourRiskAssessmentHazardsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(rowList[position])
    }

    override fun getItemCount(): Int = rowList.size

    inner class ViewHolder(private val binding: ItemBehaviourRiskAssessmentHazardsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(row: RiskAssessmentRow) {
            binding.apply {
                hazardName.text = row.potentialHazard
                //tvPotentialHazards.text = row.potentialHazard
                tvLevelOfRisk.text = row.levelOfRisk
                tvRiskRange.text = row.riskRange
                tvSupportMethods.text = row.supportMethod
                tvControlsAdequate.text = row.controlAdequate
                tvRegulatoryMeasures.text = row.regulatoryMeasure
            }
        }
    }
}

data class RiskAssessmentRow(
    val potentialHazard: String,
    val levelOfRisk: String,
    val riskRange: String,
    val supportMethod: String,
    val controlAdequate: String,
    val regulatoryMeasure: String
)