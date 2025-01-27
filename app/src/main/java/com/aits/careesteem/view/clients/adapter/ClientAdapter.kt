/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import com.aits.careesteem.view.clients.model.Client

class ClientAdapter(
    private val clients: List<Client>,
    private val onItemClick: OnItemClick,
) : RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {

    interface OnItemClick {
        fun onItemClicked(item: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_client, parent, false)
        return ClientViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val client = clients[position]
        holder.bind(client)
    }

    override fun getItemCount(): Int {
        return clients.size
    }

    inner class ClientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val clientName: TextView = itemView.findViewById(R.id.clientName)
        private val clientPhone: TextView = itemView.findViewById(R.id.clientPhone)
        private val clientAddress: TextView = itemView.findViewById(R.id.clientAddress)
        private val riskLevel: TextView = itemView.findViewById(R.id.riskLevel)
        private val layout: LinearLayoutCompat = itemView.findViewById(R.id.layout)

        private val part1: View = itemView.findViewById(R.id.part1)
        private val part2: View = itemView.findViewById(R.id.part2)
        private val part3: View = itemView.findViewById(R.id.part3)

        fun bind(client: Client) {
            clientName.text = client.name
            clientPhone.text = client.phone
            clientAddress.text = client.address
            riskLevel.text = client.riskLevel ?: "Normal"

            if(client.riskLevel.isNullOrEmpty()){
                part1.visibility = View.VISIBLE
                part2.visibility = View.INVISIBLE
                part3.visibility = View.INVISIBLE
            } else if(client.riskLevel == "Moderate"){
                part1.visibility = View.VISIBLE
                part2.visibility = View.VISIBLE
                part3.visibility = View.INVISIBLE
            } else if(client.riskLevel == "High"){
                part1.visibility = View.VISIBLE
                part2.visibility = View.VISIBLE
                part3.visibility = View.VISIBLE
            }

            layout.setOnClickListener {
                onItemClick.onItemClicked("")
            }
        }

    }
}