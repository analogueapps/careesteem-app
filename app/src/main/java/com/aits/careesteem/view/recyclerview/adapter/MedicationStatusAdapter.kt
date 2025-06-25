package com.aits.careesteem.view.recyclerview.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import com.aits.careesteem.view.visits.model.MedicationStatus

class MedicationStatusAdapter(
    private val list: List<MedicationStatus>,
    private val onSelected: (String) -> Unit
) : RecyclerView.Adapter<MedicationStatusAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val imgView = itemView.findViewById<ImageView>(R.id.imgView)
        val viewDivider = itemView.findViewById<View>(R.id.viewDivider)

        init {
            itemView.setOnClickListener {
                onSelected(list[adapterPosition].status)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medication_dropdown_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    @SuppressLint("SetTextI18n", "DiscouragedApi")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvName.text = item.status
        val resId = holder.itemView.context.resources.getIdentifier(
            item.img, // e.g., "ic_fully_taken"
            "drawable",
            holder.itemView.context.packageName
        )
        holder.imgView.setImageResource(resId)

        // Hide divider for last item
        if (position == list.size - 1) {
            holder.viewDivider.visibility = View.GONE
        } else {
            holder.viewDivider.visibility = View.VISIBLE
        }
    }
}