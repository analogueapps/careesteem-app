package com.aits.careesteem.view.clients.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import com.aits.careesteem.view.clients.model.RiskAssessmentItem

class RiskAssessmentParentAdapter(
    private val context: Context,
    private val items: MutableList<RiskAssessmentItem>
) : RecyclerView.Adapter<RiskAssessmentParentAdapter.ParentViewHolder>() {

//    // Add this function to update the data
//    fun updateData(newItems: List<RiskAssessmentItem>) {
//        val startPosition = items.size
//        items.addAll(newItems)
//        notifyItemRangeInserted(startPosition, newItems.size)
//    }

    fun updateData(newItems: List<RiskAssessmentItem>) {
        items.clear() // Clear the existing list
        items.addAll(newItems) // Add new items
        notifyDataSetChanged() // Notify adapter about full data change
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_risk_section_with_list, parent, false)
        return ParentViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sectionTitle: TextView = itemView.findViewById(R.id.sectionTitle)
        private val innerRecyclerView: RecyclerView = itemView.findViewById(R.id.innerRecyclerView)

        @SuppressLint("SetTextI18n")
        fun bind(item: RiskAssessmentItem) {
            innerRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            when (item) {
                is RiskAssessmentItem.FilteredItem -> {
                    sectionTitle.text = item.title
                    innerRecyclerView.adapter = QuestionAnswerAdapter(item.qaList)
                }
                is RiskAssessmentItem.ActivityItem -> {
                    sectionTitle.text = "Activity Risk Assessment"
                    innerRecyclerView.adapter = ActivityRiskAssessmentAdapter(listOf(item.data))
                }
                is RiskAssessmentItem.BehaviourItem -> {
                    sectionTitle.text = "Behaviour Risk Assessment"
                    innerRecyclerView.adapter = BehaviourRiskAssessmentAdapter(context, listOf(item.data))
                }
                is RiskAssessmentItem.COSHHItem -> {
                    sectionTitle.text = "COSHH Risk Assessment"
                    innerRecyclerView.adapter = COSHHRiskAssessmentAdapter(listOf(item.data))
                }
                is RiskAssessmentItem.EquipmentItem -> {
                    sectionTitle.text = "Equipment Register"
                    innerRecyclerView.adapter = EquipmentRegisterAdapter(listOf(item.data))
                }
                is RiskAssessmentItem.FinancialItem -> {
                    sectionTitle.text = "Financial Risk Assessment"
                    innerRecyclerView.adapter = FinancialRiskAssessmentAdapter(listOf(item.data))
                }
                is RiskAssessmentItem.MedicationItem -> {
                    sectionTitle.text = "Medication Risk Assessment"
                    innerRecyclerView.adapter = MedicationRiskAssessmentAdapter(listOf(item.data))
                }
                is RiskAssessmentItem.SelfAdminItem -> {
                    sectionTitle.text = "Self Administration Risk Assessment"
                    innerRecyclerView.adapter = SelfAdministrationRiskAssessmentAdapter(context, listOf(item.data))
                }
            }
        }
    }
}
