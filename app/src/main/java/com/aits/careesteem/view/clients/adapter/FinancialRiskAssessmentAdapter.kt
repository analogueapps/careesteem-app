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
import com.aits.careesteem.utils.AppConstant
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
                tvRequiresAssistance.text = AppConstant.checkNull(data.requires_assistance)
                tvResponsibleParty.text = AppConstant.checkNull(data.responsible_party)
                tvFamilyMemberName.text = AppConstant.checkNull(data.family_member_name)
                tvAgencyName.text = AppConstant.checkNull(data.agency_name)
                tvLocalAuthorityName.text = AppConstant.checkNull(data.local_authority_name)
                tvSpendingLimit.text = AppConstant.checkNull(data.spending_limit)
                tvSpendingDetails.text = AppConstant.checkNull(data.spending_details)
                tvMoneySpentBy.text = AppConstant.checkNull(data.money_spent_by)
                tvActivitiesFinances.text = AppConstant.checkNull(data.activities_finances)
                tvSafeLocation.text = AppConstant.checkNull(data.safe_location)
                tvProvideDetails.text = AppConstant.checkNull(data.provide_details)
                tvName1.text = AppConstant.checkNull(data.sign_1)
                tvName2.text = AppConstant.checkNull(data.sign_2)
                tvDate1.text = AppConstant.checkNull(data.date_1)
                tvDate2.text = AppConstant.checkNull(data.date_2)
            }
        }
    }
}