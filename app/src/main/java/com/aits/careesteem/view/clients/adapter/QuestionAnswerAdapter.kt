/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.databinding.ItemAssessmentQaBinding
import com.aits.careesteem.databinding.ItemSelfAdministrationRiskAssessmentBinding
import com.aits.careesteem.view.clients.model.CarePlanRiskAssList

class QuestionAnswerAdapter(
    private val items: List<Pair<String, String>>
) : RecyclerView.Adapter<QuestionAnswerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAssessmentQaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    inner class ViewHolder(private val binding: ItemAssessmentQaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(questionAnswer: Pair<String, String>) {
            // Manually set data
            binding.qnsNumber.text = "${position+1}."
            binding.question.text = questionAnswer.first
            binding.answer.text = questionAnswer.second
        }
    }
}