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
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.databinding.ItemVisitNotesListBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.visits.model.ClientVisitNotesDetails

class VisitNotesAdapter(
    private val context: Context,
    private val onItemItemClick: OnItemItemClick,
    private val showOnly: Boolean,
    private val isChanges: Boolean,
) : RecyclerView.Adapter<VisitNotesAdapter.ViewHolder>() {

    interface OnItemItemClick {
        fun onItemItemClicked(data: ClientVisitNotesDetails.Data)
    }

    private var visitNotesList = listOf<ClientVisitNotesDetails.Data>()

    fun updateList(list: List<ClientVisitNotesDetails.Data>) {
        visitNotesList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemVisitNotesListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataItem = visitNotesList[position]
        holder.bind(dataItem)
    }

    override fun getItemCount(): Int = visitNotesList.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(private val binding: ItemVisitNotesListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(data: ClientVisitNotesDetails.Data) {
            binding.apply {
                val date: String = if (data.createdAt.isEmpty()) data.updatedAt
                else if (data.createdAt.isEmpty() && data.updatedAt.isNotEmpty()) data.updatedAt
                else if (data.createdAt.isNotEmpty() && data.updatedAt.isEmpty()) data.createdAt
                else if (data.createdAt.isNotEmpty() && data.updatedAt.isNotEmpty()) data.updatedAt
                else data.createdAt
                updatedAt.text = AppConstant.visitNotesListTimer(date)
                updatedByUserName.text = "Updated by: " + AppConstant.checkUserLastName(data.updatedByUserName)
                visitNotes.text = data.visitNotes

                if(isChanges) {
                    if(showOnly) {
                        editButton.visibility = ViewGroup.GONE
                    } else {
                        editButton.visibility = ViewGroup.VISIBLE
                    }
                   // editButton.visibility = View.VISIBLE
                } else {
                    editButton.visibility = View.GONE
                }

                editButton.setOnClickListener {
                    onItemItemClick.onItemItemClicked(data)
                }
            }
        }
    }
}