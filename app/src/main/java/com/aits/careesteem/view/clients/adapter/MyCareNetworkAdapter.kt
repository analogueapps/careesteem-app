/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.databinding.ItemMyCareNetworkBinding
import com.aits.careesteem.view.clients.model.ClientDetailsResponse

class MyCareNetworkAdapter(
    private val context: Context,
    private val onMyCareNetworkItemClick: OnMyCareNetworkItemClick
) : RecyclerView.Adapter<MyCareNetworkAdapter.ViewHolder>() {

    interface OnMyCareNetworkItemClick {
        fun onMyCareNetworkItemClicked(data: ClientDetailsResponse.Data.MyCareNetworkData)
    }

    private var adapterList = listOf<ClientDetailsResponse.Data.MyCareNetworkData>()

    fun updateList(list: List<ClientDetailsResponse.Data.MyCareNetworkData>) {
        adapterList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemMyCareNetworkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val binding: ItemMyCareNetworkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(data: ClientDetailsResponse.Data.MyCareNetworkData) {
            binding.apply {
                occupationType.text = data.occupation_type

                layout.setOnClickListener {
                    onMyCareNetworkItemClick.onMyCareNetworkItemClicked(data)
                }
            }
        }
    }
}