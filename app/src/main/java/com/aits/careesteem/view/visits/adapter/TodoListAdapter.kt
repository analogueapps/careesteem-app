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
import com.aits.careesteem.databinding.ItemTodoListBinding
import com.aits.careesteem.utils.AppConstant.setTextWithColoredStar
import com.aits.careesteem.view.visits.model.TodoListResponse

class TodoListAdapter(
    private val context: Context,
    private val onItemItemClick: OnItemItemClick
) : RecyclerView.Adapter<TodoListAdapter.ViewHolder>() {

    interface OnItemItemClick {
        fun onItemItemClicked(data: TodoListResponse.Data)
    }

//    private var todoList = listOf<TodoListResponse.Data>()
//
//    fun updateList(list: List<TodoListResponse.Data>) {
//        todoList = list
//        notifyDataSetChanged()
//    }

    private var fullAdapterList = listOf<TodoListResponse.Data>()
    private var todoList = listOf<TodoListResponse.Data>()

    fun updateList(list: List<TodoListResponse.Data>) {
        fullAdapterList = list
        todoList = list
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        todoList = if (query.isEmpty()) {
            fullAdapterList
        } else {
            fullAdapterList.filter {
                it.todoName.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemTodoListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataItem = todoList[position]
        holder.bind(dataItem)
    }

    override fun getItemCount(): Int = todoList.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(private val binding: ItemTodoListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(data: TodoListResponse.Data) {
            binding.apply {
                // Set todoStatus icon
                when (data.todoOutcome) {
                    "Not Completed" -> todoStatus.setImageResource(R.drawable.cross)
                    "Completed" -> todoStatus.setImageResource(R.drawable.tick)
                }

//                // Append * with color, always
//                val originalText = data.todoName.trimEnd()
//                val finalText = "$originalText sdfjsdgfhjdsgfjhdsjfgsdhgfsdgfgdshjfghsd *"
//                val spannable = SpannableString(finalText)
//
//                spannable.setSpan(
//                    ForegroundColorSpan(ContextCompat.getColor(root.context, R.color.colorPrimary)),
//                    finalText.length - 1,
//                    finalText.length,
//                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                )
//
//                todoName.text = spannable

//                todoName.post {
//                    val maxWidth = todoName.width
//                    AppConstant.applyTextWithColoredAsterisk(todoName, data.todoName+"jdjshfhgjdsjfhgdsjhfgdsjhfgdsjhgfjhdsgf", maxWidth, binding.root.context)
//                }

                if (data.todoEssential) {
                    todoName.setTextWithColoredStar(
                        text = data.todoName,
                        starColor = ContextCompat.getColor(context, R.color.colorPrimary)
                    )
                } else {
                    todoName.text = data.todoName
                }

                layout.setOnClickListener {
                    onItemItemClick.onItemItemClicked(data)
                }
            }
        }
    }
}