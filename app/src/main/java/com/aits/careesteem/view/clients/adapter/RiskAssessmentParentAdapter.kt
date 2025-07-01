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
import com.aits.careesteem.view.clients.model.ClientsList
import com.aits.careesteem.view.clients.model.RiskAssessmentItem

class RiskAssessmentParentAdapter(
    private val context: Context,
    private val items: MutableList<RiskAssessmentItem>,
    private val clientData: ClientsList.Data
) : RecyclerView.Adapter<RiskAssessmentParentAdapter.ParentViewHolder>() {

    fun updateData(newItems: List<RiskAssessmentItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_risk_section_with_list, parent, false)
        return ParentViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    inner class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sectionTitle: TextView = itemView.findViewById(R.id.sectionTitle)
        private val sectionHeader: TextView = itemView.findViewById(R.id.sectionTitleee)
        private val innerRecyclerView: RecyclerView = itemView.findViewById(R.id.innerRecyclerView)

        @SuppressLint("SetTextI18n")
        fun bind(item: RiskAssessmentItem, position: Int) {
            // Only show header for first section
            sectionHeader.visibility = if (position == 0) View.VISIBLE else View.GONE

            innerRecyclerView.layoutManager = LinearLayoutManager(itemView.context)

            when (item) {
                is RiskAssessmentItem.FilteredItem -> {
                    sectionTitle.text = item.title
                    // Set adapter if needed
                    // innerRecyclerView.adapter = QuestionAnswerAdapter(item.title, item.qaList)
                }

                is RiskAssessmentItem.ActivityItem -> {
                    sectionTitle.text = "Activity Risk Assessment"
                    innerRecyclerView.adapter = ActivityRiskAssessmentAdapter(item.data, clientData)
                }

                is RiskAssessmentItem.BehaviourItem -> {
                    sectionTitle.text = "Behaviour Risk Assessment"
                    innerRecyclerView.adapter = BehaviourRiskAssessmentAdapter(context, item.data)
                }

                is RiskAssessmentItem.COSHHItem -> {
                    sectionTitle.text = "COSHH Risk Assessment"
                    innerRecyclerView.adapter = COSHHRiskAssessmentAdapter(item.data, clientData)
                }

                is RiskAssessmentItem.EquipmentItem -> {
                    sectionTitle.text = "Equipment Register"
                    innerRecyclerView.adapter = EquipmentRegisterAdapter(item.data, clientData)
                }

                is RiskAssessmentItem.FinancialItem -> {
                    sectionTitle.text = "Financial Risk Assessment"
                    innerRecyclerView.adapter = FinancialRiskAssessmentAdapter(item.data, clientData)
                }

                is RiskAssessmentItem.MedicationItem -> {
                    sectionTitle.text = "Medication Risk Assessment"
                    innerRecyclerView.adapter = MedicationRiskAssessmentAdapter(item.data)
                }

                is RiskAssessmentItem.SelfAdminItem -> {
                    sectionTitle.text = "Self Administration Risk Assessment"
                    innerRecyclerView.adapter = SelfAdministrationRiskAssessmentAdapter(context, item.data, clientData)
                }
            }
        }
    }
}
