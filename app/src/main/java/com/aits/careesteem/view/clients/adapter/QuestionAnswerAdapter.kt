/*
 * Copyright (c) 2025 ANALOGUE IT SOLUTIONS. All rights reserved.
 * Use of this source code is governed by a MIT-style license that can be
 * found in the LICENSE file.
 */

package com.aits.careesteem.view.clients.adapter

import android.annotation.SuppressLint
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ItemAssessmentQaBinding

//class QuestionAnswerAdapter(
//    private val itemTitle: String,
//    private val items: List<Triple<String, String, String>>
//) : RecyclerView.Adapter<QuestionAnswerAdapter.ViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val binding = ItemAssessmentQaBinding.inflate(
//            LayoutInflater.from(parent.context), parent, false
//        )
//        return ViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bind(items[position])
//    }
//
//    override fun getItemCount(): Int = items.size
//
//    override fun getItemId(position: Int): Long = position.toLong()
//
//    override fun getItemViewType(position: Int): Int = position
//
//    inner class ViewHolder(private val binding: ItemAssessmentQaBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        @SuppressLint("SetTextI18n")
//        fun bind(questionAnswer: Triple<String, String, String>) {
//            if (adapterPosition == 0) {
//                // Show title for first item
//                binding.title.text = itemTitle
//                binding.title.visibility = View.VISIBLE
//            } else {
//                // Hide title for other items
//                binding.title.visibility = View.GONE
//            }
//            // Manually set data
//            binding.question.text = "${position + 1}.${questionAnswer.first}"
//            binding.answer.text = questionAnswer.second
//            if(questionAnswer.third.isNotEmpty() && questionAnswer.third != "N/A") {
//                binding.comment.text = questionAnswer.third
//                binding.comment.visibility = View.VISIBLE
//            } else {
//                binding.comment.visibility = View.GONE
//            }
//        }
//    }
//}

//class QuestionAnswerAdapter : RecyclerView.Adapter<QuestionAnswerAdapter.ViewHolder>() {
//
//    private val items = mutableListOf<Pair<String, Triple<String, String, String>>>()
//    // Pair<Title, Triple<Question, Answer, Comment>>
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val binding = ItemAssessmentQaBinding.inflate(
//            LayoutInflater.from(parent.context), parent, false
//        )
//        return ViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bind(items[position], position)
//    }
//
//    override fun getItemCount(): Int = items.size
//
//    fun updateData(title: String, newItems: List<Triple<String, String, String>>) {
//        // Each item will be paired with its section title
//        if (newItems.isNotEmpty()) {
//            val indexToInsert = items.size
//            val sectionItems = newItems.mapIndexed { index, item ->
//                title to item
//            }
//            items.addAll(sectionItems)
//            notifyItemRangeInserted(indexToInsert, sectionItems.size)
//        }
//    }
//
//    inner class ViewHolder(private val binding: ItemAssessmentQaBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        @SuppressLint("SetTextI18n")
//        fun bind(data: Pair<String, Triple<String, String, String>>, position: Int) {
//            val (title, questionData) = data
//            val (question, answer, comment) = questionData
//
//            // Show title only for the first item of that section
//            val showTitle = position == 0 || items[position - 1].first != title
//            binding.title.text = title
//            binding.title.visibility = if (showTitle) View.VISIBLE else View.GONE
//
//            binding.question.text = "${position + 1}. $question"
//            binding.answer.text = answer
//
//            if (comment.isNotEmpty() && comment != "N/A") {
//                binding.comment.text = comment
//                binding.comment.visibility = View.VISIBLE
//            } else {
//                binding.comment.visibility = View.GONE
//            }
//        }
//    }
//}

class QuestionAnswerAdapter : RecyclerView.Adapter<QuestionAnswerAdapter.ViewHolder>() {

    private val items = mutableListOf<SectionedItem>()

    data class SectionedItem(
        val title: String,
        val data: Triple<String, String, String>,
        val indexInSection: Int, // 1-based index in section
        val isFirstInSection: Boolean
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAssessmentQaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    fun updateData(title: String, newItems: List<Triple<String, String, String>>) {
        if (newItems.isNotEmpty()) {
            val indexToInsert = items.size
            val sectionedItems = newItems.mapIndexed { index, item ->
                SectionedItem(
                    title = title,
                    data = item,
                    indexInSection = index + 1,
                    isFirstInSection = index == 0
                )
            }
            items.addAll(sectionedItems)
            notifyItemRangeInserted(indexToInsert, sectionedItems.size)
        }
    }

    inner class ViewHolder(private val binding: ItemAssessmentQaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: SectionedItem, position: Int) = with(binding) {
            sectionHeader.visibility = if (position == 0) View.VISIBLE else View.GONE

            // Title
            title.text = item.title
            title.visibility = if (item.isFirstInSection) View.VISIBLE else View.GONE

            // Spacing between sections
            space.visibility = if (item.isFirstInSection && adapterPosition != 0) View.VISIBLE else View.GONE

            // Question and answer
            question.text = "${item.indexInSection}. ${item.data.first}"
            answer.text = item.data.second

            // Comment
            if (item.data.third.isNotEmpty() && item.data.third != "N/A") {
                comment.text = item.data.third
                comment.visibility = View.VISIBLE
            } else {
                comment.visibility = View.GONE
            }

            // Apply background to the content layout
            rootLayout.background = ContextCompat.getDrawable(
                itemView.context,
                R.drawable.assessment_bg
            )

            // Set margins to create separation
            (rootLayout.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                topMargin = if (position == 0) 0 else (-1).dpToPx() // Overlap the divider
                bottomMargin = 0
            }
        }
    }

    private fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}

