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
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList

class EquipmentRegisterAdapter(
    private val adapterList: List<CarePlanRiskAssList.Data.EquipmentRegisterData>,
) : RecyclerView.Adapter<EquipmentRegisterAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemEquipmentRegisterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val binding: ItemEquipmentRegisterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CarePlanRiskAssList.Data.EquipmentRegisterData) {
            binding.apply {
                tvEquipment.text = AppConstant.checkNull(data.equipment)
                tvEquipmentDescription.text = AppConstant.checkNull(data.equipment_description)
                tvProvidedBy.text = AppConstant.checkNull(data.provided_by)
                tvPurpose.text = AppConstant.checkNull(data.purpose)
                tvNextTest.text = AppConstant.checkNull(data.date_of_next_test)
                tvTestComplete.text = AppConstant.checkNull(data.test_completed_on)
                tvName1.text = AppConstant.checkNull(data.sign_1)
                tvName2.text = AppConstant.checkNull(data.sign_2)
                tvDate1.text = AppConstant.checkNull(data.date_1)
                tvDate2.text = AppConstant.checkNull(data.date_2)
            }
        }
    }
}