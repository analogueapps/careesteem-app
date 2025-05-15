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
import com.aits.careesteem.databinding.ItemOngoingVisitsBinding
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.view.visits.model.VisitListResponse
import kotlinx.coroutines.Job

class OngoingVisitsAdapter(
    private val context: Context,
    private val ongoingItemItemClick: OngoingItemItemClick,
    private val ongoingCheckoutItemItemClick: OngoingCheckoutItemItemClick,
) : RecyclerView.Adapter<OngoingVisitsAdapter.ViewHolder>() {

    interface OngoingItemItemClick {
        fun ongoingItemItemClicked(data: VisitListResponse.Data)
    }

    interface OngoingCheckoutItemItemClick {
        fun ongoingCheckoutItemItemClicked(data: VisitListResponse.Data)
    }

    private var visitsList = listOf<VisitListResponse.Data>()

    fun updateList(list: List<VisitListResponse.Data>) {
        visitsList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemOngoingVisitsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val binding: ItemOngoingVisitsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Hold a reference to the coroutine Job for cancellation, if needed.
        private var timerJob: Job? = null

        @SuppressLint("SetTextI18n")
        fun bind(data: VisitListResponse.Data) {
            binding.apply {
                tvClientName.text = data.clientName
                tvClientAddress.text = data.clientAddress
                // You may have another field in your data representing the total planned time.
                // Here, we start a countdown using the planned end time.
                tvPlanTime.text = data.totalPlannedTime

                // Cancel any previous timer if this view is recycled
                timerJob?.cancel()

                if(data.actualStartTime.isNotEmpty() && data.actualStartTime[0].isNotEmpty()) {
                    btnCheckout.text = "Check out"
                    timerJob = DateTimeUtils.startCountdownTimer(data.visitDate, data.actualStartTime[0]) { remainingTime ->
                        println("Remaining Time: $remainingTime")
                        tvPlanTime.text = remainingTime
                    }
                } else {
                    btnCheckout.text = "Check in"
                    tvPlanTime.text = "00:00"
                }

                layout.setOnClickListener {
                    ongoingItemItemClick.ongoingItemItemClicked(data)
                }

                btnCheckout.setOnClickListener {
                    ongoingCheckoutItemItemClick.ongoingCheckoutItemItemClicked(data)
                }
            }
        }
    }

}