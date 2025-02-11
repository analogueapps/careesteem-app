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
import com.aits.careesteem.databinding.ItemBehaviourRiskAssessmentBinding
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList

class BehaviourRiskAssessmentAdapter(
    private val adapterList: List<CarePlanRiskAssList.Data.BehaviourRiskAssessmentData>,
) : RecyclerView.Adapter<BehaviourRiskAssessmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemBehaviourRiskAssessmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val binding: ItemBehaviourRiskAssessmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CarePlanRiskAssList.Data.BehaviourRiskAssessmentData) {
            binding.apply {
                tvFrequencyPotential.text = data.frequency_potential
                tvAffectedByBehaviour.text = data.affected_by_behaviour
                tvPotentialTriggers.text = data.potential_triggers
                tvPotentialHazards.text = data.potential_hazards
                tvLevelOfRisk.text = data.level_of_risk
                tvRiskRange.text = data.risk_range
                tvSupportMethods.text = data.support_methods
                tvControlsAdequate.text = data.controls_adequate
                tvRegulatoryMeasures.text = data.regulatory_measures
                tvName1.text = data.sign_1
                tvName2.text = data.sign_2
                tvDate1.text = data.date_1
                tvDate2.text = data.date_2
            }
        }
    }
}