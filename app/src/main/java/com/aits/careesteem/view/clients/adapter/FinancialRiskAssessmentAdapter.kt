/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.databinding.ItemEquipmentRegisterBinding
import com.aits.careesteem.databinding.ItemFinancialRiskAssessmentBinding
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList

class FinancialRiskAssessmentAdapter(
    private val adapterList: List<CarePlanRiskAssList.Data.FinancialRiskAssessmentData>,
) : RecyclerView.Adapter<FinancialRiskAssessmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemFinancialRiskAssessmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val binding: ItemFinancialRiskAssessmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CarePlanRiskAssList.Data.FinancialRiskAssessmentData) {
            binding.apply {
                tvRequiresAssistance.text = data.requires_assistance
                tvResponsibleParty.text = data.responsible_party
                tvFamilyMemberName.text = data.family_member_name
                tvAgencyName.text = data.agency_name
                tvLocalAuthorityName.text = data.local_authority_name
                tvSpendingLimit.text = data.spending_limit
                tvSpendingDetails.text = data.spending_details
                tvMoneySpentBy.text = data.money_spent_by
                tvActivitiesFinances.text = data.activities_finances
                tvSafeLocation.text = data.safe_location
                tvProvideDetails.text = data.provide_details
                tvName1.text = data.sign_1
                tvName2.text = data.sign_2
                tvDate1.text = data.date_1
                tvDate2.text = data.date_2
            }
        }
    }
}