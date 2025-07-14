/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.visits.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.databinding.ItemNotCompleteVisitsBinding
import com.aits.careesteem.databinding.ItemTravelTimeBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.unscheduled_visits.model.VisitItem
import com.aits.careesteem.view.visits.adapter.CompleteVisitsAdapter.OnViewItemItemClick
import com.aits.careesteem.view.visits.model.VisitListResponse

class NotCompleteVisitsAdapter(
    private val context: Context,
    private val onDirectionItemItemClick: OnDirectionItemItemClick,
    private val onViewItemItemClick: OnViewItemItemClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnViewItemItemClick {
        fun onViewItemItemClicked(data: VisitListResponse.Data)
    }

    interface OnDirectionItemItemClick {
        fun onDirectionItemItemClicked(data: VisitListResponse.Data)
    }

    private var visitItems: List<VisitItem> = emptyList()

    companion object {
        private const val TYPE_VISIT = 0
        private const val TYPE_TRAVEL_TIME = 1
    }

    fun updateList(items: List<VisitItem>) {
        visitItems = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        val binding =
//            ItemNotCompleteVisitsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return ViewHolder(binding)
        return when (viewType) {
            TYPE_VISIT -> {
                val binding = ItemNotCompleteVisitsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                VisitViewHolder(binding)
            }

            TYPE_TRAVEL_TIME -> {
                val binding = ItemTravelTimeBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                TravelTimeViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = visitItems[position]) {
            is VisitItem.VisitCard -> (holder as VisitViewHolder).bind(item.visitData)
            is VisitItem.TravelTimeIndicator -> (holder as TravelTimeViewHolder).bind(item.timeText)
        }
    }

    override fun getItemCount(): Int = visitItems.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return when (visitItems[position]) {
            is VisitItem.VisitCard -> TYPE_VISIT
            is VisitItem.TravelTimeIndicator -> TYPE_TRAVEL_TIME
        }
    }

    inner class VisitViewHolder(private val binding: ItemNotCompleteVisitsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(data: VisitListResponse.Data) {
            binding.apply {
                tvClientName.text = AppConstant.checkClientName(data.clientName)
                tvClientAddress.text = AppConstant.checkNull(data.clientAddress)
                tvClientPostCode.text = "${AppConstant.checkNull(data.clientCity)}, ${AppConstant.checkNull(data.clientPostcode)}"
                tvPlanTime.text = AppConstant.checkNull(data.totalPlannedTime)
                tvUserRequired.text = "${data.usersRequired}"
//                tvPlannedStartTime.text = AppConstant.visitListTimer(data.plannedStartTime)
//                tvPlannedEndTime.text = AppConstant.visitListTimer(data.plannedEndTime)
                tvPlannedStartTime.text = AppConstant.checkNull(data.plannedStartTime)
                tvPlannedEndTime.text = AppConstant.checkNull(data.plannedEndTime)

                btnGetDirection.setOnClickListener {
                    onDirectionItemItemClick.onDirectionItemItemClicked(data)
                }

//                layout.setOnClickListener {
//                    onViewItemItemClick.onViewItemItemClicked(data)
//                }
            }
        }
    }

    inner class TravelTimeViewHolder(private val binding: ItemTravelTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(time: String) {
            binding.tvTravelTime.text = time
        }
    }
}