/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.visits.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ItemMedicationListBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.visits.model.MedicationDetailsListResponse
import com.aits.careesteem.view.visits.model.TodoListResponse

class MedicationListAdapter(
    private val context: Context,
    private val onItemItemClick: OnItemItemClick
) : RecyclerView.Adapter<MedicationListAdapter.ViewHolder>() {

    interface OnItemItemClick {
        fun onItemItemClicked(data: MedicationDetailsListResponse.Data)
    }

    private var fullAdapterList = listOf<MedicationDetailsListResponse.Data>()
    private var adapterList = listOf<MedicationDetailsListResponse.Data>()

    fun updateList(list: List<MedicationDetailsListResponse.Data>) {
        fullAdapterList = list
        adapterList = list
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        adapterList = if (query.isEmpty()) {
            fullAdapterList
        } else {
            fullAdapterList.filter {
                it.nhs_medicine_name.contains(query, ignoreCase = true)
            }
        }
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
                    medicationName.text = AppConstant.checkNull(data.nhs_medicine_name)
                    medicationSupport.text = AppConstant.checkNull(data.medication_support)
                    medicationType.text = AppConstant.checkNull(data.medication_type)

                    if (data.status == "Scheduled" || data.status == "Not Scheduled") {
                        todoStatus.visibility = View.GONE
                        expendIcon.visibility = View.VISIBLE
                    } else {
                        todoStatus.visibility = View.VISIBLE
                        expendIcon.visibility = View.GONE
                    }

                    if (data.medication_type == "Scheduled") {
                        medicationSession.visibility = View.VISIBLE
                        if(data.select_preference.lowercase() == "by exact time") {
                            medicationSession.text = "@ ${AppConstant.checkNull(data.by_exact_time)}"
                        } else if(data.select_preference.lowercase() == "by session") {
                            medicationSession.text = "@ ${AppConstant.checkNull(data.session_type)}"
                        } else {
                            medicationSession.text = "@ N/A"
                        }
                    } else if (data.medication_type == "PRN") {
                        medicationSession.visibility = View.VISIBLE
                        medicationSession.text = "${data.dose_per} Doses per ${data.doses} ${data.time_frame}"
                    } else {
                        medicationSession.visibility = View.GONE
                    }

                    todoStatus.text = data.status
                    when (data.status) {
                        "Fully Taken" -> todoStatus.apply {
                            background =
                                ContextCompat.getDrawable(context, R.drawable.ic_btn_green_bg)
                            backgroundTintList =
                                ContextCompat.getColorStateList(context, R.color.colorPrimary)
                            //setTextColor(ContextCompat.getColor(context, R.color.white))
                        }

                        else -> todoStatus.apply {
                            background =
                                ContextCompat.getDrawable(context, R.drawable.ic_btn_green_bg)
                            backgroundTintList = ContextCompat.getColorStateList(
                                context,
                                R.color.notCompleteCardCorner
                            )
                            //setTextColor(ContextCompat.getColor(context, R.color.black))
                        }
                    }
                    layout.setOnClickListener {
                        onItemItemClick.onItemItemClicked(data)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }
}