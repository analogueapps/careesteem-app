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
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList

class ActivityRiskAssessmentAdapter(
    private val adapterList: List<CarePlanRiskAssList.Data.ActivityRiskAssessmentData>,
) : RecyclerView.Adapter<ActivityRiskAssessmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemActivityRiskAssessmentBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemActivityRiskAssessmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CarePlanRiskAssList.Data.ActivityRiskAssessmentData) {
            binding.apply {
                yesOrNo.text = AppConstant.checkNull(data.require_support)
                tvActivity.text = AppConstant.checkNull(data.activity)
                tvTaskSupportRequired.text = AppConstant.checkNull(data.task_support_required)
                tvRisk.text = AppConstant.checkNull(data.risk_level)
                tvRiskRange.text = AppConstant.checkNull(data.range)
                tvEquipment.text = AppConstant.checkNull(data.equipment)
                tvActionToBeTaken.text = AppConstant.checkNull(data.action_to_be_taken)
                tvName1.text = AppConstant.checkNull(data.sign_1)
                tvName2.text = AppConstant.checkNull(data.sign_2)
                tvDate1.text = AppConstant.checkNull(data.date_1)
                tvDate2.text = AppConstant.checkNull(data.date_2)
            }
        }
    }
}