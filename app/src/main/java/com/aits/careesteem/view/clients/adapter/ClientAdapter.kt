/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ItemClientBinding
import com.aits.careesteem.databinding.ItemOngoingVisitsBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.GooglePlaceHolder
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.visits.model.VisitListResponse

class ClientAdapter(
    private val context: Context,
    private val onItemClick: OnItemClick,
) : RecyclerView.Adapter<ClientAdapter.ViewHolder>() {

    interface OnItemClick {
        fun onItemClicked(data: ClientsList.Data)
    }

    private var clientList = listOf<ClientsList.Data>()

    fun updateList(list: List<ClientsList.Data>) {
        clientList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemClientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataItem = clientList[position]
        holder.bind(dataItem)
    }

    override fun getItemCount(): Int = clientList.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(private val binding: ItemClientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ClientsList.Data) {
            binding.apply {
                clientName.text = AppConstant.checkNull(data.full_name)
                clientPhone.text = AppConstant.checkNull(data.contact_number)
                clientAddress.text = AppConstant.checkNull(data.full_address)
                riskLevel.text = AppConstant.checkNull(data.risk_level)

                when (data.risk_level.lowercase()) {
                    "low" -> {
                        part1.visibility = View.VISIBLE
                        part2.visibility = View.INVISIBLE
                        part3.visibility = View.INVISIBLE
                    }
                    "moderate" -> {
                        part1.visibility = View.VISIBLE
                        part2.visibility = View.VISIBLE
                        part3.visibility = View.INVISIBLE
                    }
                    "high" -> {
                        part1.visibility = View.VISIBLE
                        part2.visibility = View.VISIBLE
                        part3.visibility = View.VISIBLE
                    }
                    else -> {
                        part1.visibility = View.INVISIBLE
                        part2.visibility = View.INVISIBLE
                        part3.visibility = View.INVISIBLE
                    }
                }

                if (data.profile_photo.isNotEmpty()) {
                    // Convert the Base64 string to a Bitmap
                    val bitmap = AppConstant.base64ToBitmap(data.profile_photo)

                    // Set the Bitmap to the ImageView (if conversion was successful)
                    bitmap?.let {
                        clientImage.setImageBitmap(it)
                    }
                } else {
                    val initials = GooglePlaceHolder().getInitialsSingle(data.full_name)
                    val initialsBitmap = GooglePlaceHolder().createInitialsAvatar(context, initials)
                    clientImage.setImageBitmap(initialsBitmap)
                }

                layout.setOnClickListener {
                    onItemClick.onItemClicked(data)
                }
            }
        }
    }


}