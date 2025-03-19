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
import com.aits.careesteem.view.visits.model.VisitListResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

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

    fun updatedList(list: List<VisitListResponse.Data>) {
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

                // Start the countdown timer if plannedEndTime is available.
                // (Assumes data.plannedEndTime is an ISO 8601 string)
//                if (data.plannedEndTime.isNotEmpty()) {
//                    timerJob = startCountdownTimer(data.plannedEndTime) { remainingText ->
//                        tvPlanTime.text = remainingText
//                    }
//                }

//                if(data.actualStartTime.isNotEmpty() && data.actualStartTime[0].isNotEmpty()) {
//                    btnCheckout.text = "Check out"
//                } else {
//                    btnCheckout.text = "Check in"
//                }

                if(data.uatId != 0) {
                    btnCheckout.text = "Check out"
                } else {
                    btnCheckout.text = "Check in"
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

    /**
     * Starts a countdown timer until the provided [plannedEndTimeStr].
     *
     * @param plannedEndTimeStr The ISO 8601 string for the planned end time.
     * @param onTick A callback invoked every second with the remaining time formatted
     * as "mm:ss". When the countdown is finished, it will be updated to "Time's up!".
     * @return The Job representing the coroutine timer.
     */
    @SuppressLint("NewApi", "DefaultLocale")
    private fun startCountdownTimer(
        plannedEndTimeStr: String,
        onTick: (String) -> Unit
    ): Job {
        // Parse the planned end time from the ISO string.
        val plannedEndTime = Instant.parse(plannedEndTimeStr)

        // Launch a coroutine on the main thread.
        return CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val now = Instant.now()
                val remaining = Duration.between(now, plannedEndTime)

                if (remaining.isZero || remaining.isNegative) {
                    onTick("Time's up!")
                    break
                }

                val minutes = remaining.toMinutes()
                val seconds = remaining.seconds % 60

                // Format the remaining time as "mm:ss".
                onTick(String.format("%02d:%02d", minutes, seconds))

                delay(1000L)
            }
        }
    }
}