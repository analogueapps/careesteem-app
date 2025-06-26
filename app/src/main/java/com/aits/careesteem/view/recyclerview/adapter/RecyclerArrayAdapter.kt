package com.aits.careesteem.view.recyclerview.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R

class RecyclerArrayAdapter(
    private val countries: List<String>,
    private val onSelected: (String) -> Unit
) : RecyclerView.Adapter<RecyclerArrayAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val viewDivider = itemView.findViewById<View>(R.id.viewDivider)

        init {
            itemView.setOnClickListener {
                onSelected(countries[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recycle_dropdown, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = countries.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = countries[position]
        holder.tvName.text = item

        // Hide divider for last item
        if (position == countries.size - 1) {
            holder.viewDivider.visibility = View.GONE
        } else {
            holder.viewDivider.visibility = View.VISIBLE
        }
    }
}