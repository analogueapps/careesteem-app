/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.visits.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ItemMedicationListBinding
import com.aits.careesteem.databinding.ItemTodoListBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.view.visits.model.MedicationDetailsListResponse
import com.aits.careesteem.view.visits.model.TodoListResponse

class MedicationListAdapter(
    private val context: Context,
    private val onItemItemClick: OnItemItemClick
) : RecyclerView.Adapter<MedicationListAdapter.ViewHolder>() {

    interface OnItemItemClick {
        fun onItemItemClicked(data: MedicationDetailsListResponse.Data)
    }

    private var adapterList = listOf<MedicationDetailsListResponse.Data>()

    fun updateList(list: List<MedicationDetailsListResponse.Data>) {
        adapterList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMedicationListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val binding: ItemMedicationListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(data: MedicationDetailsListResponse.Data) {
            try {
                binding.apply {
                    medicationName.text = data.nhs_medicine_name
                    medicationSupport.text = data.medication_support
                    medicationType.text = data.medication_type

                    if(data.status == "Scheduled" || data.status == "Not Scheduled") {
                        todoStatus.visibility = View.GONE
                        expendIcon.visibility = View.VISIBLE
                    } else {
                        todoStatus.visibility = View.VISIBLE
                        expendIcon.visibility = View.GONE
                    }

                    todoStatus.text = data.status
                    when (data.status) {
                        "Fully Taken" -> todoStatus.apply {
                            background = ContextCompat.getDrawable(context, R.drawable.ic_btn_green_bg)
                            backgroundTintList = ContextCompat.getColorStateList(context, R.color.colorPrimary)
                            //setTextColor(ContextCompat.getColor(context, R.color.white))
                        }
                        else -> todoStatus.apply {
                            background = ContextCompat.getDrawable(context, R.drawable.ic_btn_green_bg)
                            backgroundTintList = ContextCompat.getColorStateList(context, R.color.notCompleteCardCorner)
                            //setTextColor(ContextCompat.getColor(context, R.color.black))
                        }
                    }
                    layout.setOnClickListener {
                        if(data.medication_type != "PRN") {
                            onItemItemClick.onItemItemClicked(data)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }
}