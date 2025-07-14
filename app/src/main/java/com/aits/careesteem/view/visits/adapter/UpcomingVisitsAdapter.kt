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
import com.aits.careesteem.databinding.ItemTravelTimeBinding
import com.aits.careesteem.databinding.ItemUpcomingVisitsBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.unscheduled_visits.model.VisitItem
import com.aits.careesteem.view.visits.model.VisitListResponse

class UpcomingVisitsAdapter(
    private val context: Context,
    private val onItemItemClick: OnItemItemClick,
    private val onDirectionItemItemClick: OnCheckoutItemItemClick,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnItemItemClick {
        fun onItemItemClicked(data: VisitListResponse.Data)
    }

    interface OnCheckoutItemItemClick {
        fun onDirectionItemItemClicked(data: VisitListResponse.Data)
    }

    private var visitItems: List<VisitItem> = emptyList()
    private var upcomingVisitsList = listOf<VisitListResponse.Data>()

    companion object {
        private const val TYPE_VISIT = 0
        private const val TYPE_TRAVEL_TIME = 1
    }

    fun updateList(items: List<VisitItem>) {
        visitItems = items
        notifyDataSetChanged()
    }

    fun updatedUpcomingList(list: List<VisitListResponse.Data>) {
        upcomingVisitsList = list
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (visitItems[position]) {
            is VisitItem.VisitCard -> TYPE_VISIT
            is VisitItem.TravelTimeIndicator -> TYPE_TRAVEL_TIME
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_VISIT -> {
                val binding = ItemUpcomingVisitsBinding.inflate(
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

    override fun getItemCount(): Int = visitItems.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = visitItems[position]) {
            is VisitItem.VisitCard -> (holder as VisitViewHolder).bind(item.visitData)
            is VisitItem.TravelTimeIndicator -> (holder as TravelTimeViewHolder).bind(item.timeText)
        }
    }

    inner class VisitViewHolder(private val binding: ItemUpcomingVisitsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("NewApi", "SetTextI18n")
        fun bind(data: VisitListResponse.Data) {
            binding.apply {
                tvClientName.text = AppConstant.checkClientName(data.clientName)
                tvClientAddress.text = AppConstant.checkNull(data.clientAddress)
                tvClientPostCode.text = "${AppConstant.checkNull(data.clientCity)}, ${AppConstant.checkNull(data.clientPostcode)}"
                tvPlannedStartTime.text = AppConstant.checkNull(data.plannedStartTime)
                tvPlannedEndTime.text = AppConstant.checkNull(data.plannedEndTime)
                tvPlanTime.text = AppConstant.checkNull(data.totalPlannedTime)
                tvUserRequired.text = AppConstant.checkNull(data.usersRequired.toString())

                val today = java.time.LocalDate.now().toString()
                val isToday = data.visitDate == today

                btnCheckIn.isEnabled = isToday && upcomingVisitsList.isEmpty()

                btnCheckIn.setOnClickListener {
                    onItemItemClick.onItemItemClicked(data)
                }
                btnGetDirection.setOnClickListener {
                    onDirectionItemItemClick.onDirectionItemItemClicked(data)
                }
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
