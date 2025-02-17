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
import com.aits.careesteem.databinding.ItemOngoingVisitsBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.visits.model.User
import com.aits.careesteem.view.visits.model.VisitListResponse

class CompleteVisitsAdapter(
    private val context: Context
) : RecyclerView.Adapter<CompleteVisitsAdapter.ViewHolder>() {

//    interface OnItemItemClick {
//        fun onItemItemClicked(data: VisitListResponse.Data)
//    }

    private var visitsList = listOf<VisitListResponse.Data>()

    fun updatedList(list: List<VisitListResponse.Data>) {
        visitsList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCompleteVisitsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val binding: ItemCompleteVisitsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(data: VisitListResponse.Data) {
            binding.apply {
                tvClientName.text = data.clientName
                tvClientAddress.text = data.clientAddress
                tvPlanTime.text = data.totalPlannedTime
                tvUserRequired.text = data.usersRequired.toString()
//                tvPlannedStartTime.text = "Check in time\n${AppConstant.visitListTimer(data.plannedStartTime)}"
//                tvPlannedEndTime.text = "Check out time\n${AppConstant.visitListTimer(data.plannedEndTime)}"
                tvPlannedStartTime.text = "Check in time\n${data.plannedStartTime}"
                tvPlannedEndTime.text = "Check out time\n${data.plannedEndTime}"

                val userList = data.userName.mapIndexed { index, name ->
                    User(name, data.profile_photo.getOrElse(index) { "" })
                }
                val customAdapter = UserAdapter(userList)
                recyclerView.layoutManager = GridLayoutManager(context, 2)
                recyclerView.adapter = customAdapter


//                itemView.setOnClickListener {
//                    onItemItemClick.onItemItemClicked(data)
//                }
            }
        }
    }
}

class UserAdapter(
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
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_even, parent, false)
            EvenViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_odd, parent, false)
            OddViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]
        if (holder is EvenViewHolder) {
            holder.tvTitleEven.text = item.name
            // Convert the Base64 string to a Bitmap
            val bitmap = AppConstant.base64ToBitmap(item.photoUrl)

            // Set the Bitmap to the ImageView (if conversion was successful)
            bitmap?.let {
                holder.imgEven.setImageBitmap(it)
            }
        } else if (holder is OddViewHolder) {
            holder.tvTitleOdd.text = item.name
            // Convert the Base64 string to a Bitmap
            val bitmap = AppConstant.base64ToBitmap(item.photoUrl)

            // Set the Bitmap to the ImageView (if conversion was successful)
            bitmap?.let {
                holder.imgOdd.setImageBitmap(it)
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