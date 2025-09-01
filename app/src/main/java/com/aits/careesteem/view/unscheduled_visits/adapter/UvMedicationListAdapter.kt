/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.unscheduled_visits.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.databinding.ItemMedicationListBinding
import com.aits.careesteem.databinding.ItemPrnMedicationListBinding
import com.aits.careesteem.view.visits.model.MedicationDetailsListResponse

class UvMedicationListAdapter(
    private val context: Context,
    private val onPnrItemItemClick: OnPnrItemItemClick
) : RecyclerView.Adapter<UvMedicationListAdapter.ViewHolder>() {

    interface OnPnrItemItemClick {
        fun onPnrItemItemClicked(data: MedicationDetailsListResponse.Data)
    }

    private var adapterList = listOf<MedicationDetailsListResponse.Data>()

    fun updateList(list: List<MedicationDetailsListResponse.Data>) {
        adapterList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemPrnMedicationListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val binding: ItemPrnMedicationListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(data: MedicationDetailsListResponse.Data) {
            try {
                binding.apply {
                    medicationName.text = data.nhs_medicine_name
                    medicationSupport.text = data.medication_support
                    medicationType.text = data.medication_type
                    medicationSession.visibility = View.VISIBLE
                    medicationSession.text = "${data.dose_per} Doses per ${data.doses} ${data.time_frame}"
                    layout.setOnClickListener {
                        onPnrItemItemClick.onPnrItemItemClicked(data)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }
}