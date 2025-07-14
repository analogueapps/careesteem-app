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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ItemOngoingVisitsBinding
import com.aits.careesteem.databinding.ItemTravelTimeBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.view.unscheduled_visits.model.VisitItem
import com.aits.careesteem.view.visits.model.VisitListResponse
import kotlinx.coroutines.Job

class OngoingVisitsAdapter(
    private val context: Context,
    private val ongoingItemItemClick: OngoingItemItemClick,
    private val ongoingCheckoutItemItemClick: OngoingCheckoutItemItemClick,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OngoingItemItemClick {
        fun ongoingItemItemClicked(data: VisitListResponse.Data)
    }

    interface OngoingCheckoutItemItemClick {
        fun ongoingCheckoutItemItemClicked(data: VisitListResponse.Data)
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
//            ItemOngoingVisitsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return ViewHolder(binding)
        return when (viewType) {
            TYPE_VISIT -> {
                val binding = ItemOngoingVisitsBinding.inflate(
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

    inner class VisitViewHolder(private val binding: ItemOngoingVisitsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Hold a reference to the coroutine Job for cancellation, if needed.
        private var timerJob: Job? = null

        @SuppressLint("SetTextI18n")
        fun bind(data: VisitListResponse.Data) {
            binding.apply {
                tvClientName.text = AppConstant.checkClientName(data.clientName)
                tvClientAddress.text = AppConstant.checkNull(data.clientAddress)
                tvClientPostCode.text = "${AppConstant.checkNull(data.clientCity)}, ${AppConstant.checkNull(data.clientPostcode)}"
                // You may have another field in your data representing the total planned time.
                // Here, we start a countdown using the planned end time.
                tvPlanTime.text = AppConstant.checkNull(data.totalPlannedTime)

                if (data?.plannedStartTime!!.isEmpty() && data?.plannedEndTime!!.isEmpty()) {
                    plannedTime.text = "Unscheduled Visit"
                } else {
                    plannedTime.text = "${data?.plannedStartTime} - ${data?.plannedEndTime}"
                }

                // Cancel any previous timer if this view is recycled
                timerJob?.cancel()

                if (data.actualStartTime.isNotEmpty() && data.actualStartTime[0].isNotEmpty()) {
                    btnCheckout.text = "Check out"
                    btnCheckout.backgroundTintList =
                        ContextCompat.getColorStateList(context, R.color.dialogTextColor)
                    btnCheckout.setTextColor(
                        ContextCompat.getColorStateList(
                            context,
                            R.color.black
                        )
                    )
                    timerJob = DateTimeUtils.startCountdownTimer(
                        data.visitDate,
                        data.actualStartTime[0]
                    ) { remainingTime ->
                        //println("Remaining Time: $remainingTime")
                        tvPlanTime.text = remainingTime
                        btnCheckout.isEnabled = false
                        val hasPassed = AppConstant.isMoreThanTwoMinutesPassed(
                            data.visitDate,
                            data.actualStartTime[0]
                        )
                        //println("Has more than 2 minutes passed? $hasPassed")
                        if (hasPassed) {
                            btnCheckout.text = "Check out"
                            btnCheckout.isEnabled = true
                            btnCheckout.backgroundTintList = ContextCompat.getColorStateList(
                                context,
                                R.color.notCompleteCardCorner
                            )
                            btnCheckout.setTextColor(
                                ContextCompat.getColorStateList(
                                    context,
                                    R.color.white
                                )
                            )
                        }
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

    inner class TravelTimeViewHolder(private val binding: ItemTravelTimeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(time: String) {
            binding.tvTravelTime.text = time
        }
    }

}