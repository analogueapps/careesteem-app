/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.databinding.ItemCoshhRiskAssessmentBinding
import com.aits.careesteem.databinding.ItemMedicationRiskAssessmentBinding
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList

class COSHHRiskAssessmentAdapter(
    private val adapterList: List<CarePlanRiskAssList.Data.COSHHRiskAssessmentData>,
) : RecyclerView.Adapter<COSHHRiskAssessmentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCoshhRiskAssessmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val binding: ItemCoshhRiskAssessmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CarePlanRiskAssList.Data.COSHHRiskAssessmentData) {
            binding.apply {
                tvNameOfProduct.text = data.name_of_product
                tvTypeOfHarm.text = data.type_of_harm
                tvDescriptionSubstance.text = data.description_substance
                tvColor.text = data.color
                tvDetailsSubstance.text = data.details_substance
                tvContactSubstance.text = data.contact_substance
                tvName1.text = data.sign_1
                tvName2.text = data.sign_2
                tvDate1.text = data.date_1
                tvDate2.text = data.date_2
            }
        }
    }
}