/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.visits.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.databinding.ItemNotCompleteVisitsBinding
import com.aits.careesteem.databinding.ItemOngoingVisitsBinding
import com.aits.careesteem.databinding.ItemUpcomingVisitsBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.visits.model.VisitListResponse

class NotCompleteVisitsAdapter(
    private val context: Context,
    private val onDirectionItemItemClick: OnDirectionItemItemClick,
) : RecyclerView.Adapter<NotCompleteVisitsAdapter.ViewHolder>() {

    interface OnDirectionItemItemClick {
        fun onDirectionItemItemClicked(data: VisitListResponse.Data)
    }

    private var visitsList = listOf<VisitListResponse.Data>()
    private var upcomingVisitsList = listOf<VisitListResponse.Data>()

    fun updatedList(list: List<VisitListResponse.Data>) {
        visitsList = list
        notifyDataSetChanged()
    }

    fun updatedUpcomingList(list: List<VisitListResponse.Data>) {
        upcomingVisitsList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemNotCompleteVisitsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataItem = visitsList[position]
        holder.bind(dataItem)
    }

    override fun getItemCount(): Int = visitsList.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(private val binding: ItemNotCompleteVisitsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: VisitListResponse.Data) {
            binding.apply {
                tvClientName.text = data.clientName
                tvClientAddress.text = data.clientAddress
                tvPlanTime.text = data.totalPlannedTime
                tvUserRequired.text = "${data.usersRequired}"
//                tvPlannedStartTime.text = AppConstant.visitListTimer(data.plannedStartTime)
//                tvPlannedEndTime.text = AppConstant.visitListTimer(data.plannedEndTime)
                tvPlannedStartTime.text = data.plannedStartTime
                tvPlannedEndTime.text = data.plannedEndTime

                btnGetDirection.setOnClickListener {
                    onDirectionItemItemClick.onDirectionItemItemClicked(data)
                }

            }
        }
    }
}