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
import com.aits.careesteem.databinding.ItemVisitNotesListBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.unscheduled_visits.model.UvMedicationListResponse
import com.aits.careesteem.view.unscheduled_visits.model.UvTodoListResponse

class UvMedicationListAdapter(
    private val context: Context,
    private val onItemItemClick: OnItemItemClick
) : RecyclerView.Adapter<UvMedicationListAdapter.ViewHolder>() {

    interface OnItemItemClick {
        fun onItemItemClicked(data: UvMedicationListResponse.Data)
    }

    private var adapterList = listOf<UvMedicationListResponse.Data>()

    fun updateList(list: List<UvMedicationListResponse.Data>) {
        adapterList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemVisitNotesListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val binding: ItemVisitNotesListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(data: UvMedicationListResponse.Data) {
            binding.apply {
                val date: String = when {
                    data.medication_created_at.isNullOrEmpty() && !data.medication_updated_at.isNullOrEmpty() -> data.medication_updated_at
                    !data.medication_created_at.isNullOrEmpty() && data.medication_updated_at.isNullOrEmpty() -> data.medication_created_at
                    !data.medication_created_at.isNullOrEmpty() && !data.medication_updated_at.isNullOrEmpty() -> data.medication_updated_at
                    else -> ""
                }
                updatedAt.text = AppConstant.visitUvNotesListTimer(date)
                view.visibility = View.GONE
                updatedByUserName.visibility = View.GONE
                visitNotes.text = data.medication_notes

                editButton.setOnClickListener {
                    onItemItemClick.onItemItemClicked(data)
                }
            }
        }
    }
}