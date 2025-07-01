/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.databinding.ItemActivityRiskAssessmentActivitiesBinding
import com.aits.careesteem.databinding.ItemActivityRiskAssessmentBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList
import com.aits.careesteem.view.clients.model.ClientsList

class ActivityRiskAssessmentAdapter(
    private val adapterList: List<CarePlanRiskAssList.Data.ActivityRiskAssessmentData>,
    private val clientData: ClientsList.Data,
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

    override fun getItemCount(): Int = 1

    inner class ViewHolder(private val binding: ItemActivityRiskAssessmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(data: CarePlanRiskAssList.Data.ActivityRiskAssessmentData) {
            binding.apply {
//                yesOrNo.text = AppConstant.checkNull(data.require_support)
//                tvActivity.text = AppConstant.checkNull(data.activity)
//                tvTaskSupportRequired.text = AppConstant.checkNull(data.task_support_required)
//                tvRisk.text = AppConstant.checkNull(data.risk_level)
//                tvRiskRange.text = AppConstant.checkNull(data.range)
//                tvEquipment.text = AppConstant.checkNull(data.equipment)
//                tvActionToBeTaken.text = AppConstant.checkNull(data.action_to_be_taken)
//                tvName1.text = AppConstant.checkNull(data.sign_1)
//                tvName2.text = AppConstant.checkNull(data.sign_2)
//                tvDate1.text = AppConstant.checkNull(data.date_1)
//                tvDate2.text = AppConstant.checkNull(data.date_2)

                val requireSupport = AppConstant.checkNull(data.require_support)
                question.text = "1. Does ${AppConstant.checkClientFirstName(clientData.full_name)} require support with activities?"
                answer.text = requireSupport
                questionOne.text = "2. What risks there are and what we are doing to reduce the risks?"
                answerOne.text = AppConstant.checkNull(data.reduced_risks)
                questionTwo.text = "3. What Outcomes would ${AppConstant.checkClientFirstName(clientData.full_name)} like to achieve?"
                answerTwo.text = AppConstant.checkNull(data.outcome_achieve)
                questionThree.text = "4. Who will support ${AppConstant.checkClientFirstName(clientData.full_name)} to achieve these goals?"
                answerThree.text = AppConstant.checkNull(data.support_achieve)
                questionFour.text = "5. What is needed to help ${AppConstant.checkClientFirstName(clientData.full_name)} achieve these goals?"
                answerFour.text = AppConstant.checkNull(data.help_user_achieve_goals)
                questionFive.text = "6. When would the service user like to achieve these goals by?"
                answerFive.text = AppConstant.checkNull(data.user_achieve_goals_by)
                questionSix.text = "7. Why is this important to ${AppConstant.checkClientFirstName(clientData.full_name)}?"
                answerSix.text = AppConstant.checkNull(data.important_user)

                if(requireSupport.lowercase() == "yes"){
                    recyclerView.visibility = RecyclerView.VISIBLE
                    val adapter = ActivityRiskListAssessmentAdapter(adapterList)
                    recyclerView.layoutManager = LinearLayoutManager(itemView.context)
                    recyclerView.adapter = adapter
                } else {
                    recyclerView.visibility = RecyclerView.GONE
                }
            }
        }
    }
}


class ActivityRiskListAssessmentAdapter(
    private val adapterList: List<CarePlanRiskAssList.Data.ActivityRiskAssessmentData>
) : RecyclerView.Adapter<ActivityRiskListAssessmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemActivityRiskAssessmentActivitiesBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemActivityRiskAssessmentActivitiesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(data: CarePlanRiskAssList.Data.ActivityRiskAssessmentData, position: Int) {
            binding.apply {
                title.text = "Activity ${position + 1}"
                tvActivity.text = AppConstant.checkNull(data.activity)
                tvTaskSupportRequired.text = AppConstant.checkNull(data.task_support_required)
                tvRisk.text = AppConstant.checkNull(data.risk_level)
                tvRiskRange.text = AppConstant.checkNull(data.range)
                tvEquipment.text = AppConstant.checkNull(data.equipment)
                tvActionToBeTaken.text = AppConstant.checkNull(data.action_to_be_taken)
            }
        }
    }
}