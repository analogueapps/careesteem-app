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
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ItemCompleteVisitsBinding
import com.aits.careesteem.databinding.ItemTravelTimeBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.utils.GooglePlaceHolder
import com.aits.careesteem.view.unscheduled_visits.model.VisitItem
import com.aits.careesteem.view.visits.model.User
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.bumptech.glide.Glide

class CompleteVisitsAdapter(
    private val context: Context,
    private val onViewItemItemClick: OnViewItemItemClick
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface OnViewItemItemClick {
        fun onViewItemItemClicked(data: VisitListResponse.Data)
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
        return when (viewType) {
            TYPE_VISIT -> {
                val binding = ItemCompleteVisitsBinding.inflate(
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

    inner class VisitViewHolder(private val binding: ItemCompleteVisitsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(data: VisitListResponse.Data) {
            try {
                binding.apply {
                    tvClientName.text = AppConstant.checkClientName(data.clientName)
                    tvClientAddress.text = AppConstant.checkNull(data.clientAddress)
                    tvClientPostCode.text = "${AppConstant.checkNull(data.clientCity)}, ${AppConstant.checkNull(data.clientPostcode)}"
                    tvPlanTime.text = AppConstant.checkNull(data.TotalActualTimeDiff[0])
                    tvUserRequired.text =
                        if (data.usersRequired == 0) "1" else "${data.usersRequired}"


                    val userList = data.userName.mapIndexed { index, name ->
                        val photoUrl = data.profile_photo_name.getOrNull(index).orEmpty()
                        User(name, photoUrl)
                    }
                    val customAdapter = UserAdapter(context, userList)
                    recyclerView.layoutManager = GridLayoutManager(context, 2)
                    recyclerView.adapter = customAdapter

                    layout.setOnClickListener {
                        onViewItemItemClick.onViewItemItemClicked(data)
                    }

//                tvPlannedStartTime.text = "Check in time\n${AppConstant.visitListTimer(data.plannedStartTime)}"
//                tvPlannedEndTime.text = "Check out time\n${AppConstant.visitListTimer(data.plannedEndTime)}"
                    tvPlannedStartTime.text =
                        "Check in time\n${DateTimeUtils.convertTime(data.actualStartTime[0])}"
                    tvPlannedEndTime.text =
                        "Check out time\n${DateTimeUtils.convertTime(data.actualEndTime[0])}"

                    if (data?.plannedStartTime!!.isEmpty() && data?.plannedEndTime!!.isEmpty()) {
                        tvUnscheduledIndicator.visibility = View.VISIBLE
                    } else {
                        tvUnscheduledIndicator.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AlertUtils.showLog("CompleteVisitsAdapter", "" + e.printStackTrace())
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

class UserAdapter(
    private val context: Context,
    private val itemList: List<User>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val ODD_ITEM = 1
        const val EVEN_ITEM = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) EVEN_ITEM else ODD_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == EVEN_ITEM) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_even, parent, false)
            EvenViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_odd, parent, false)
            OddViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]
        if (holder is EvenViewHolder) {
            try {
                holder.tvTitleEven.text = AppConstant.checkNull(item.name)
                if (item.photoUrl.isNotEmpty()) {
//                // Convert the Base64 string to a Bitmap
//                val bitmap = AppConstant.base64ToBitmap(item.photoUrl)
//
//                // Set the Bitmap to the ImageView (if conversion was successful)
//                bitmap?.let {
//                    holder.imgEven.setImageBitmap(it)
//                }
                    Glide.with(context)
                        .load(AppConstant.checkNull(item.photoUrl))
                        .override(400, 300)
                        
                        .error(R.drawable.logo_preview)
                        .circleCrop()
                        .into(holder.imgEven)
                } else {
                    val initials =
                        GooglePlaceHolder().getInitialsSingle(AppConstant.checkNull(item.name))
                    val initialsBitmap = GooglePlaceHolder().createInitialsAvatar(context, initials)
                    holder.imgEven.setImageBitmap(initialsBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AlertUtils.showLog("CompleteVisitsAdapter", "" + e.printStackTrace())
            }

        } else if (holder is OddViewHolder) {
            try {
                holder.tvTitleOdd.text = AppConstant.checkNull(item.name)
                if (item.photoUrl.isNotEmpty()) {
//                // Convert the Base64 string to a Bitmap
//                val bitmap = AppConstant.base64ToBitmap(item.photoUrl)
//
//                // Set the Bitmap to the ImageView (if conversion was successful)
//                bitmap?.let {
//                    holder.imgOdd.setImageBitmap(it)
//                }
                    Glide.with(context)
                        .load(AppConstant.checkNull(item.photoUrl))
                        .override(400, 300)
                        
                        .error(R.drawable.logo_preview)
                        .circleCrop()
                        .into(holder.imgOdd)
                } else {
                    val initials =
                        GooglePlaceHolder().getInitialsSingle(AppConstant.checkNull(item.name))
                    val initialsBitmap = GooglePlaceHolder().createInitialsAvatar(context, initials)
                    holder.imgOdd.setImageBitmap(initialsBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AlertUtils.showLog("CompleteVisitsAdapter", "" + e.printStackTrace())
            }
        }
    }

    override fun getItemCount(): Int = itemList.size

    class EvenViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitleEven: TextView = view.findViewById(R.id.tvTitleEven)
        val imgEven: ImageView = view.findViewById(R.id.imgEven)
    }

    class OddViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitleOdd: TextView = view.findViewById(R.id.tvTitleOdd)
        val imgOdd: ImageView = view.findViewById(R.id.imgOdd)
    }
}