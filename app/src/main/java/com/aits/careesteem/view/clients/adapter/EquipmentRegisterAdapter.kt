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
import com.aits.careesteem.databinding.ItemEquipmentRegisterBinding
import com.aits.careesteem.databinding.ItemEquipmentRegisterEquipementsBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList
import com.aits.careesteem.view.clients.model.ClientsList

class EquipmentRegisterAdapter(
    private val adapterList: List<CarePlanRiskAssList.Data.EquipmentRegisterData>,
    private val  clientData: ClientsList.Data,
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

    override fun getItemCount(): Int = 1

    inner class ViewHolder(private val binding: ItemEquipmentRegisterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(data: CarePlanRiskAssList.Data.EquipmentRegisterData) {
            binding.apply {
//                tvEquipment.text = AppConstant.checkNull(data.equipment)
//                tvEquipmentDescription.text = AppConstant.checkNull(data.equipment_description)
//                tvProvidedBy.text = AppConstant.checkNull(data.provided_by)
//                tvPurpose.text = AppConstant.checkNull(data.purpose)
//                tvNextTest.text = AppConstant.checkNull(data.date_of_next_test)
//                tvTestComplete.text = AppConstant.checkNull(data.test_completed_on)
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

                val adapter = EquipmentListAdapter(adapterList)
                recyclerView.layoutManager = LinearLayoutManager(itemView.context)
                recyclerView.adapter = adapter
            }
        }
    }
}

class EquipmentListAdapter(
    private val adapterList: List<CarePlanRiskAssList.Data.EquipmentRegisterData>,
) : RecyclerView.Adapter<EquipmentListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemEquipmentRegisterEquipementsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataItem = adapterList[position]
        holder.bind(dataItem, position)
    }

    override fun getItemCount(): Int = adapterList.size

    inner class ViewHolder(private val binding: ItemEquipmentRegisterEquipementsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(data: CarePlanRiskAssList.Data.EquipmentRegisterData, position: Int) {
            binding.apply {
                title.text = "Equipment ${position + 1}"
                tvEquipment.text = AppConstant.checkNull(data.equipment)
                tvEquipmentDescription.text = AppConstant.checkNull(data.equipment_description)
                tvProvidedBy.text = AppConstant.checkNull(data.provided_by)
                tvPurpose.text = AppConstant.checkNull(data.purpose)
                tvNextTest.text = AppConstant.checkNull(data.date_of_next_test)
                tvTestComplete.text = AppConstant.checkNull(data.test_completed_on)
            }
        }
    }
}