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
import com.aits.careesteem.view.visits.model.TodoListResponse

class TodoListAdapter(
    private val context: Context,
    private val onItemItemClick: OnItemItemClick
) : RecyclerView.Adapter<TodoListAdapter.ViewHolder>() {

    interface OnItemItemClick {
        fun onItemItemClicked(data: TodoListResponse.Data)
    }

    private var todoList = listOf<TodoListResponse.Data>()

    fun updatedList(list: List<TodoListResponse.Data>) {
        todoList = list
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
                todoName.text = data.todoName
                todoStatus.text = data.todoOutcome

                when (data.todoOutcome) {
                    "Not Completed" -> todoStatus.apply {
                        background = ContextCompat.getDrawable(context, R.drawable.ic_btn_red_bg)
                        backgroundTintList = ContextCompat.getColorStateList(context, R.color.red)
                    }
                    "Completed" -> todoStatus.apply {
                        background = ContextCompat.getDrawable(context, R.drawable.ic_btn_green_bg)
                        backgroundTintList = ContextCompat.getColorStateList(context, R.color.colorPrimary)
                    }
//                    else -> todoStatus.apply {
//                        background = ContextCompat.getDrawable(context, R.drawable.ic_btn_green_bg)
//                        backgroundTintList = ContextCompat.getColorStateList(context, R.color.colorPrimary)
//                    }
                }

                layout.setOnClickListener {
                    onItemItemClick.onItemItemClicked(data)
                }
            }
        }
    }
}