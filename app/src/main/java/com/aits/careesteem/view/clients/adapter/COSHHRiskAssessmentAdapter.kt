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
import com.aits.careesteem.databinding.ItemCoshhRiskAssessmentBinding
import com.aits.careesteem.databinding.ItemCoshhRiskAssessmentProductBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList
import com.aits.careesteem.view.clients.model.ClientsList

class COSHHRiskAssessmentAdapter(
    private val adapterList: List<CarePlanRiskAssList.Data.COSHHRiskAssessmentData>,
    private val clientData: ClientsList.Data,
) : RecyclerView.Adapter<COSHHRiskAssessmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCoshhRiskAssessmentBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemCoshhRiskAssessmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(data: CarePlanRiskAssList.Data.COSHHRiskAssessmentData) {
            binding.apply {
//                tvNameOfProduct.text = AppConstant.checkNull(data.name_of_product)
//                tvTypeOfHarm.text = AppConstant.checkNull(data.type_of_harm)
//                tvDescriptionSubstance.text = AppConstant.checkNull(data.description_substance)
//                tvColor.text = AppConstant.checkNull(data.color)
//                tvDetailsSubstance.text = AppConstant.checkNull(data.details_substance)
//                tvContactSubstance.text = AppConstant.checkNull(data.contact_substance)
//                tvName1.text = AppConstant.checkNull(data.sign_1)
//                tvName2.text = AppConstant.checkNull(data.sign_2)
//                tvDate1.text = AppConstant.checkNull(data.date_1)
//                tvDate2.text = AppConstant.checkNull(data.date_2)

                questionOne.text = "1. What risks there are and what we are doing to reduce the risks?"
                answerOne.text = AppConstant.checkNull(data.reduced_risks)
                questionTwo.text = "2. What Outcomes would ${AppConstant.checkClientFirstName(clientData.full_name)} like to achieve?"
                answerTwo.text = AppConstant.checkNull(data.outcome_achieve)
                questionThree.text = "3. Who will support ${AppConstant.checkClientFirstName(clientData.full_name)} to achieve these goals?"
                answerThree.text = AppConstant.checkNull(data.support_achieve)
                questionFour.text = "4. What is needed to help ${AppConstant.checkClientFirstName(clientData.full_name)} achieve these goals?"
                answerFour.text = AppConstant.checkNull(data.help_user_achieve_goals)
                questionFive.text = "5. When would the service user like to achieve these goals by?"
                answerFive.text = AppConstant.checkNull(data.user_achieve_goals_by)
                questionSix.text = "6. Why is this important to ${AppConstant.checkClientFirstName(clientData.full_name)}?"
                answerSix.text = AppConstant.checkNull(data.important_user)

                val adapter = COSHHRiskAssessmentProductAdapter(adapterList)
                recyclerView.layoutManager = LinearLayoutManager(itemView.context)
                recyclerView.adapter = adapter
            }
        }
    }
}

class COSHHRiskAssessmentProductAdapter(
    private val adapterList: List<CarePlanRiskAssList.Data.COSHHRiskAssessmentData>,
) : RecyclerView.Adapter<COSHHRiskAssessmentProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCoshhRiskAssessmentProductBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemCoshhRiskAssessmentProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CarePlanRiskAssList.Data.COSHHRiskAssessmentData) {
            binding.apply {
                tvNameOfProduct.text = AppConstant.checkNull(data.name_of_product)
                tvTypeOfHarm.text = AppConstant.checkNull(data.type_of_harm)
                tvDescriptionSubstance.text = AppConstant.checkNull(data.description_substance)
                tvColor.text = AppConstant.checkNull(data.color)
                tvDetailsSubstance.text = AppConstant.checkNull(data.details_substance)
                tvContactSubstance.text = AppConstant.checkNull(data.contact_substance)
//                tvName1.text = AppConstant.checkNull(data.sign_1)
//                tvName2.text = AppConstant.checkNull(data.sign_2)
//                tvDate1.text = AppConstant.checkNull(data.date_1)
//                tvDate2.text = AppConstant.checkNull(data.date_2)
            }
        }
    }
}